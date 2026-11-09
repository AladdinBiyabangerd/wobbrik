package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException
import java.io.SyncFailedException

/**
 * JVM crash/fault matrix for [DiskAttemptJournal] over in-memory models of `android.util.AtomicFile`
 * (API 24–29 `.bak` protocol and API 30+ `.new` + rename protocol, transcribed from AOSP
 * android10-release / android11-release) and of the `androidAttemptJournal` adapter.
 * Model evidence only: it does not prove kernel, filesystem or flash durability on a device.
 */
class AtomicFileCrashMatrixTest {
    /** Process death. An Error, so production `catch (Exception)` cannot swallow it. */
    private class Crash : Error("simulated process death")

    /** Namespace operations are modelled as durable once done; file data is durable only after sync. */
    private class Fs {
        private val files = linkedMapOf<String, ByteArray>()
        private val unsynced = mutableSetOf<String>()
        var ops = 0
        var crashAt = -1
        private var dead = false
        var renameFails: (String, String) -> Boolean = { _, _ -> false }
        var deleteFails: (String) -> Boolean = { false }
        var writeFailsAfter: Int? = null
        var syncFails = false
        var readDenied = false
        private fun step() {
            if (dead) throw Crash()
            if (ops++ == crashAt) { dead = true; throw Crash() }
        }
        fun exists(name: String): Boolean { step(); return name in files }
        fun read(name: String): ByteArray? {
            step()
            if (readDenied && name in files) throw FileNotFoundException("$name (Permission denied)")
            return files[name]?.copyOf()
        }
        fun delete(name: String): Boolean {
            step()
            if (deleteFails(name)) return false
            unsynced -= name
            return files.remove(name) != null
        }
        fun rename(source: String, target: String): Boolean {
            step()
            if (renameFails(source, target) || source !in files) return false
            files[target] = files.remove(source)!!
            if (unsynced.remove(source)) unsynced += target else unsynced -= target
            return true
        }
        fun create(name: String) { step(); files[name] = ByteArray(0); unsynced += name }
        /** Two chunks so a crash can leave a partial file. */
        fun write(name: String, bytes: ByteArray) {
            val half = bytes.size / 2
            for (chunk in listOf(bytes.copyOfRange(0, half), bytes.copyOfRange(half, bytes.size))) {
                step()
                val current = files.getValue(name)
                val limit = writeFailsAfter
                unsynced += name
                if (limit != null && current.size + chunk.size > limit) {
                    files[name] = current + chunk.copyOf(maxOf(0, limit - current.size))
                    throw IOException("No space left on device")
                }
                files[name] = current + chunk
            }
        }
        fun sync(name: String): Boolean { step(); if (syncFails) return false; unsynced -= name; return true }
        /** New process; with [keep], power loss keeps only that many bytes of never-synced data. */
        fun restart(keep: ((Int) -> Int)? = null) {
            dead = false; crashAt = -1; ops = 0
            if (keep != null) for (name in unsynced) files[name] = files.getValue(name).copyOf(keep(files.getValue(name).size))
            unsynced.clear()
        }
        fun clearFaults() { renameFails = { _, _ -> false }; deleteFails = { false }; writeFailsAfter = null; syncFails = false; readDenied = false }
        fun snapshot(): Map<String, List<Byte>> = files.mapValues { it.value.toList() }
        fun names(): Set<String> = files.keys.toSet()
        fun raw(name: String): ByteArray? = files[name]?.copyOf()
        fun put(name: String, bytes: ByteArray) { files[name] = bytes.copyOf() }
    }

    private interface Atomic {
        val base: String
        fun startWrite(): String
        fun finishWrite(out: String)
        fun failWrite(out: String)
        fun openRead(): ByteArray
    }

    /** API 30+: write `.new`, rename into place. Sync, rename and delete failures are only logged. */
    private class ModernAtomic(private val fs: Fs, override val base: String) : Atomic {
        private val next = "$base.new"
        private val legacy = "$base.bak"
        override fun startWrite(): String { if (fs.exists(legacy)) fs.rename(legacy, base); fs.create(next); return next }
        override fun finishWrite(out: String) { fs.sync(out); fs.rename(next, base) }
        override fun failWrite(out: String) { fs.sync(out); fs.delete(next) }
        override fun openRead(): ByteArray {
            if (fs.exists(legacy)) fs.rename(legacy, base)
            if (fs.exists(next) && fs.exists(base)) fs.delete(next)
            return fs.read(base) ?: throw FileNotFoundException(base)
        }
    }

    /** API 24–29: move base to `.bak`, rewrite base, delete `.bak`. Rename/delete results are ignored. */
    private class LegacyAtomic(private val fs: Fs, override val base: String) : Atomic {
        private val backup = "$base.bak"
        override fun startWrite(): String {
            if (fs.exists(base)) { if (!fs.exists(backup)) fs.rename(base, backup) else fs.delete(base) }
            fs.create(base); return base
        }
        override fun finishWrite(out: String) { fs.sync(base); fs.delete(backup) }
        override fun failWrite(out: String) { fs.sync(base); fs.delete(base); fs.rename(backup, base) }
        override fun openRead(): ByteArray {
            if (fs.exists(backup)) { fs.delete(base); fs.rename(backup, base) }
            return fs.read(base) ?: throw FileNotFoundException(base)
        }
    }

    private enum class Api { LEGACY_24_29, MODERN_30 }
    private enum class Loss(val keep: ((Int) -> Int)?) { PROCESS_DEATH(null), POWER_LOSS_EMPTY({ 0 }), POWER_LOSS_HALF({ it / 2 }) }

    /** Mirrors the JournalFile adapter in android/.../AndroidAttemptJournal.kt. */
    private fun journal(fs: Fs, api: Api) = DiskAttemptJournal { key ->
        val file = if (api == Api.MODERN_30) ModernAtomic(fs, "$key.journal") else LegacyAtomic(fs, "$key.journal")
        object : JournalFile {
            override fun read(): ByteArray? = try { file.openRead() } catch (e: FileNotFoundException) {
                if (fs.exists(file.base) || fs.exists("${file.base}.bak")) throw e
                null
            }
            override fun replace(bytes: ByteArray) {
                val out = file.startWrite()
                try {
                    fs.write(out, bytes)
                    if (!fs.sync(out)) throw SyncFailedException("sync failed")
                    file.finishWrite(out)
                } catch (failure: Throwable) { file.failWrite(out); throw failure }
            }
        }
    }

    private val alice = "alice"
    private val bob = "bob"
    private val start = GameplayWire.start("op-start", 3)
    private val checkpoint = GameplayWire.checkpoint("op-checkpoint", 4, "attempt-1", 2, 2, 1, 0,
        """{"note":"şəhər ✓ — ${"x".repeat(9000)}","floors":[1,2]}""")
    private val finish = GameplayWire.finish("op-finish", 5, "attempt-1", 2,
        mapOf("wall" to "w1", "window" to "n1", "entrance" to "e1", "roof" to "r1", "lighting" to "l1"))
    private val bobCommand = GameplayWire.start("op-bob", 9)
    private val next = GameplayWire.start("op-next", 6)

    private class Case(val name: String, val setup: (DiskAttemptJournal) -> Unit,
                       val action: (DiskAttemptJournal) -> Unit, val before: AttemptCommand?, val after: AttemptCommand?)
    private val cases = listOf(
        Case("first save", {}, { it.save(alice, checkpoint) }, null, checkpoint),
        Case("save over tombstone", { it.save(alice, start); it.remove(alice, start) }, { it.save(alice, finish) }, null, finish),
        Case("remove to tombstone", { it.save(alice, checkpoint) }, { it.remove(alice, checkpoint) }, checkpoint, null),
    )

    private data class Outcome(val pending: AttemptCommand?, val unreadable: Boolean)
    private fun recover(fs: Fs, api: Api): Outcome =
        try { Outcome(journal(fs, api).pending(alice), false) } catch (_: UnreadableAttemptJournal) { Outcome(null, true) }

    private fun prepared(api: Api, case: Case): Fs {
        val fs = Fs()
        val journal = journal(fs, api)
        journal.save(bob, bobCommand)
        case.setup(journal)
        fs.restart()
        return fs
    }

    private fun assertSameCommand(label: String, expected: AttemptCommand?, actual: AttemptCommand?) {
        assertEquals(label, expected, actual)
        assertEquals(label, expected?.envelope, actual?.envelope)
    }

    /** After restart: exact old/new state (or a preserved unreadable file), stable on repeated recovery, bob untouched. */
    private fun assertRecovered(label: String, fs: Fs, api: Api, case: Case, completed: Boolean, allowUnreadable: Boolean): Outcome {
        val first = recover(fs, api)
        when {
            completed -> assertEquals(label, Outcome(case.after, false), first)
            first.unreadable -> assertTrue("$label unexpectedly unreadable", allowUnreadable)
            else -> assertTrue("$label recovered ${first.pending?.operationId}", first.pending == case.before || first.pending == case.after)
        }
        first.pending?.let { assertSameCommand(label, if (it == case.after) case.after else case.before, it) }
        val stable = fs.snapshot()
        repeat(2) {
            assertEquals(label, first, recover(fs, api))
            assertEquals("$label repeated recovery mutated files", stable, fs.snapshot())
        }
        assertSameCommand("$label bob", bobCommand, journal(fs, api).pending(bob))
        if (first.unreadable) {
            assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, api).save(alice, next) }
            assertEquals("$label unreadable bytes must be preserved", stable, fs.snapshot())
        } else {
            val journal = journal(fs, api)
            first.pending?.let { journal.remove(alice, it) }
            journal.save(alice, next)
            assertSameCommand("$label continuation", next, journal(fs, api).pending(alice))
        }
        return first
    }

    @Test fun crashAtEveryStepRecoversExactOldOrNewState() {
        val findings = mutableListOf<String>()
        var scenarios = 0
        for (api in Api.entries) for (case in cases) {
            val total = prepared(api, case).let { fs -> case.action(journal(fs, api)); fs.ops }
            for (crashAt in 0..total) for (loss in Loss.entries) {
                val fs = prepared(api, case)
                fs.crashAt = crashAt
                val completed = try { case.action(journal(fs, api)); true } catch (_: Crash) { false }
                assertEquals(crashAt == total, completed)
                fs.restart(loss.keep)
                val label = "$api/${case.name}/crash@$crashAt/$loss"
                // Known API 24–29 limitation: the very first write has no backup to roll back to.
                val outcome = assertRecovered(label, fs, api, case, completed, api == Api.LEGACY_24_29 && case.name == "first save")
                if (outcome.unreadable) findings += label
                scenarios++
            }
        }
        println("crash matrix scenarios=$scenarios legacyFirstWriteUnreadable=${findings.size}: $findings")
        assertTrue(findings.isNotEmpty()) // documents the limitation; see review
        assertTrue(findings.none { it.startsWith("MODERN_30") })
    }

    @Test fun uncommittedWriteIsNeverReportedAsDurable() {
        // API 30+ finishWrite only logs a failed `.new` -> base rename. API 24–29 finishWrite ignores a
        // failed `.bak` delete, so the next read rolls back to the backup. Neither throws to the caller.
        for (api in Api.entries) for (case in cases) {
            val fs = prepared(api, case)
            if (api == Api.MODERN_30) fs.renameFails = { source, _ -> source.endsWith(".new") }
            else fs.deleteFails = { it.endsWith(".bak") }
            val label = "$api/${case.name}"
            val error = runCatching { case.action(journal(fs, api)) }.exceptionOrNull()
            fs.clearFaults(); fs.restart()
            val durable = recover(fs, api)
            assertFalse(label, durable.unreadable)
            if (error == null) assertSameCommand("$label reported success", case.after, durable.pending)
            else {
                assertTrue("$label: $error", error is IllegalStateException)
                assertSameCommand(label, case.before, durable.pending)
                case.action(journal(fs, api))
                assertSameCommand("$label retry", case.after, journal(fs, api).pending(alice))
            }
            assertSameCommand("$label bob", bobCommand, journal(fs, api).pending(bob))
        }
    }

    @Test fun legacyFailedBackupRenameLosesRollbackButNeverInventsState() {
        // API 24–29 startWrite only logs a failed base -> `.bak` rename and then rewrites base in place.
        val findings = mutableListOf<String>()
        for (case in cases.drop(1)) {
            val total = prepared(Api.LEGACY_24_29, case).let { fs ->
                fs.renameFails = { _, target -> target.endsWith(".bak") }; case.action(journal(fs, Api.LEGACY_24_29)); fs.ops }
            for (crashAt in 0 until total) for (loss in Loss.entries) {
                val fs = prepared(Api.LEGACY_24_29, case)
                fs.renameFails = { _, target -> target.endsWith(".bak") }
                fs.crashAt = crashAt
                assertThrows(Crash::class.java) { case.action(journal(fs, Api.LEGACY_24_29)) }
                fs.clearFaults(); fs.restart(loss.keep)
                val label = "LEGACY_24_29/${case.name}/no-backup/crash@$crashAt/$loss"
                if (assertRecovered(label, fs, Api.LEGACY_24_29, case, false, true).unreadable) findings += label
            }
        }
        println("legacy no-backup unreadable=${findings.size}: $findings")
        assertTrue(findings.isNotEmpty())
    }

    @Test fun failedWriteOrSyncRollsBackAndRetrySucceeds() {
        for (api in Api.entries) for (case in cases) for (fault in listOf("disk full", "sync")) {
            val fs = prepared(api, case)
            if (fault == "disk full") fs.writeFailsAfter = 7 else fs.syncFails = true
            val label = "$api/${case.name}/$fault"
            val error = runCatching { case.action(journal(fs, api)) }.exceptionOrNull()
            assertTrue("$label: $error", error is IOException)
            fs.clearFaults(); fs.restart()
            assertEquals(label, Outcome(case.before, false), recover(fs, api))
            assertTrue("$label leftover temp ${fs.names()}", fs.names().none { it.endsWith(".new") || it.endsWith(".bak") })
            case.action(journal(fs, api))
            assertSameCommand(label, case.after, journal(fs, api).pending(alice))
            assertSameCommand(label, bobCommand, journal(fs, api).pending(bob))
        }
    }

    @Test fun staleBackupAndTempFilesResolveToCommittedState() {
        for (api in Api.entries) {
            val fs = Fs()
            journal(fs, api).save(alice, checkpoint)
            val base = "${key(alice)}.journal"
            val committed = fs.raw(base)!!
            // Legacy crash leftover (also seen by API 30+ after an OS upgrade): backup is authoritative.
            fs.put("$base.bak", committed); fs.put(base, committed.copyOf(5)); fs.restart()
            assertSameCommand("$api bak", checkpoint, journal(fs, api).pending(alice))
            assertFalse(fs.names().contains("$base.bak")); assertArrayEquals(committed, fs.raw(base))
            // An uncommitted complete `.new` never wins over the base file.
            val other = Fs().also { journal(it, api).save(alice, finish) }.raw(base)!!
            fs.put("$base.new", other); fs.restart()
            assertSameCommand("$api new", checkpoint, journal(fs, api).pending(alice))
            if (api == Api.MODERN_30) assertFalse(fs.names().contains("$base.new"))
        }
        // API 30+: `.new` without base is an uncommitted first write, i.e. empty.
        val fs = Fs()
        val base = "${key(alice)}.journal"
        fs.put("$base.new", byteArrayOf(0, 0, 0, 1, 0)); fs.restart()
        assertNull(journal(fs, Api.MODERN_30).pending(alice))
        journal(fs, Api.MODERN_30).save(alice, start)
        assertSameCommand("modern after orphan new", start, journal(fs, Api.MODERN_30).pending(alice))
    }

    @Test fun unreadableExistingFileIsNeverTreatedAsEmpty() {
        for (api in Api.entries) {
            val fs = Fs()
            journal(fs, api).save(alice, start)
            val before = fs.snapshot()
            fs.readDenied = true
            repeat(2) { assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, api).pending(alice) } }
            assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, api).save(alice, next) }
            assertEquals(before, fs.snapshot())
        }
    }

    private fun key(uid: String) = java.security.MessageDigest.getInstance("SHA-256")
        .digest(uid.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
