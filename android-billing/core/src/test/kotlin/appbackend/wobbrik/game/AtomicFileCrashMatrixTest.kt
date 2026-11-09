package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * JVM crash/fault matrix for [DiskAttemptJournal] over the production [DurableJournalFile] with a
 * fault-injecting in-memory [JournalFileSystem]. Models of the previous `android.util.AtomicFile`
 * adapter (API 24–29 `.bak` and API 30+ `.new` protocols, transcribed from AOSP android10-release /
 * android11-release) only produce leftover files for upgrade checks.
 * Model evidence only: it does not prove kernel, filesystem or flash durability on a device.
 */
class AtomicFileCrashMatrixTest {
    /** Process death. An Error, so production `catch (Exception)` cannot swallow it. */
    private class Crash : Error("simulated process death")

    /** File data is durable only after sync; renames/deletes only after a directory sync (or not, per loss mode). */
    private class Fs : JournalFileSystem {
        private var files = linkedMapOf<String, ByteArray>()
        private var durableNamespace = linkedMapOf<String, ByteArray>()
        private val unsynced = mutableSetOf<String>()
        var ops = 0
        var crashAt = -1
        private var dead = false
        var renameFails: (String, String) -> Boolean = { _, _ -> false }
        var renameLies = false
        var deleteFails: (String) -> Boolean = { false }
        var writeFailsAfter: Int? = null
        var syncFails = false
        var directorySyncFails = false
        var readDenied = false
        var directorySyncs = 0
        private fun step() {
            if (dead) throw Crash()
            if (ops++ == crashAt) { dead = true; throw Crash() }
        }
        override fun exists(file: File): Boolean = has(file.path)
        override fun read(file: File, limit: Int): ByteArray =
            readOrNull(file.path)?.also { if (it.size > limit) throw IOException("Journal too large") } ?: throw FileNotFoundException(file.path)
        override fun writeSynced(file: File, bytes: ByteArray) {
            create(file.path); write(file.path, bytes)
            if (!sync(file.path)) throw IOException("fsync failed")
        }
        override fun rename(from: File, to: File): Boolean = rename(from.path, to.path)
        override fun delete(file: File): Boolean = delete(file.path)
        override fun syncDirectory(directory: File) {
            step()
            if (directorySyncFails) throw IOException("directory fsync failed")
            directorySyncs++
            durableNamespace = LinkedHashMap(files)
        }

        fun has(name: String): Boolean { step(); return name in files }
        fun readOrNull(name: String): ByteArray? {
            step()
            if (readDenied && name in files) throw FileNotFoundException("$name (Permission denied)")
            return files[name]?.copyOf()
        }
        fun delete(name: String): Boolean {
            step()
            if (deleteFails(name)) return false
            unsynced -= name
            files.remove(name)
            return true
        }
        fun rename(source: String, target: String): Boolean {
            step()
            if (renameFails(source, target) || source !in files) return false
            if (renameLies) return true
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

        /** New process. Power loss truncates never-synced data and may revert to the last synced directory. */
        fun restart(loss: Loss = Loss.PROCESS_DEATH) {
            dead = false; crashAt = -1; ops = 0
            if (loss.revertDirectory) files = LinkedHashMap(durableNamespace)
            loss.keep?.let { keep -> for (name in unsynced) files[name]?.let { files[name] = it.copyOf(keep(it.size)) } }
            unsynced.clear()
            durableNamespace = LinkedHashMap(files)
        }
        fun clearFaults() {
            renameFails = { _, _ -> false }; renameLies = false; deleteFails = { false }
            writeFailsAfter = null; syncFails = false; directorySyncFails = false; readDenied = false
        }
        fun copy(): Fs = Fs().also { it.files = LinkedHashMap(files); it.durableNamespace = LinkedHashMap(files) }
        fun snapshot(): Map<String, List<Byte>> = files.mapValues { it.value.toList() }
        fun names(): Set<String> = files.keys.toSet()
        fun raw(name: String): ByteArray? = files[name]?.copyOf()
        fun put(name: String, bytes: ByteArray) { files[name] = bytes.copyOf(); durableNamespace = LinkedHashMap(files) }
    }

    private interface OldAtomic {
        val base: String
        fun startWrite(): String
        fun finishWrite(out: String)
        fun failWrite(out: String)
        fun openRead(): ByteArray
    }

    /** Previous adapter on API 30+: android.util.AtomicFile `.new` + rename; failures only logged. */
    private class OldModernAtomic(private val fs: Fs, override val base: String) : OldAtomic {
        private val next = "$base.new"
        private val legacy = "$base.bak"
        override fun startWrite(): String { if (fs.has(legacy)) fs.rename(legacy, base); fs.create(next); return next }
        override fun finishWrite(out: String) { fs.sync(out); fs.rename(next, base) }
        override fun failWrite(out: String) { fs.sync(out); fs.delete(next) }
        override fun openRead(): ByteArray {
            if (fs.has(legacy)) fs.rename(legacy, base)
            if (fs.has(next) && fs.has(base)) fs.delete(next)
            return fs.readOrNull(base) ?: throw FileNotFoundException(base)
        }
    }

    /** Previous adapter on API 24–29: android.util.AtomicFile `.bak` protocol, base rewritten in place. */
    private class OldLegacyAtomic(private val fs: Fs, override val base: String) : OldAtomic {
        private val backup = "$base.bak"
        override fun startWrite(): String {
            if (fs.has(base)) { if (!fs.has(backup)) fs.rename(base, backup) else fs.delete(base) }
            fs.create(base); return base
        }
        override fun finishWrite(out: String) { fs.sync(base); fs.delete(backup) }
        override fun failWrite(out: String) { fs.sync(base); fs.delete(base); fs.rename(backup, base) }
        override fun openRead(): ByteArray {
            if (fs.has(backup)) { fs.delete(base); fs.rename(backup, base) }
            return fs.readOrNull(base) ?: throw FileNotFoundException(base)
        }
    }

    private enum class Layout { DURABLE, OLD_ATOMICFILE_24_29, OLD_ATOMICFILE_30 }
    enum class Loss(val keep: ((Int) -> Int)?, val revertDirectory: Boolean) {
        PROCESS_DEATH(null, false), POWER_LOSS_EMPTY({ 0 }, false), POWER_LOSS_HALF({ it / 2 }, false),
        POWER_LOSS_EMPTY_UNSYNCED_DIRECTORY({ 0 }, true), POWER_LOSS_HALF_UNSYNCED_DIRECTORY({ it / 2 }, true),
    }
    /** In-place rewrites by the old AtomicFile cannot be modelled by directory reverts. */
    private val oldLosses = listOf(Loss.PROCESS_DEATH, Loss.POWER_LOSS_EMPTY, Loss.POWER_LOSS_HALF)

    private val directory = File("/journal")
    private fun journal(fs: Fs, layout: Layout) = DiskAttemptJournal { key ->
        when (layout) {
            Layout.DURABLE -> DurableJournalFile(directory, "$key.journal", fs)
            else -> oldAdapter(fs, if (layout == Layout.OLD_ATOMICFILE_30) OldModernAtomic(fs, File(directory, "$key.journal").path)
                                  else OldLegacyAtomic(fs, File(directory, "$key.journal").path))
        }
    }
    /** The adapter that shipped before this branch (AndroidAttemptJournal over android.util.AtomicFile). */
    private fun oldAdapter(fs: Fs, file: OldAtomic) = object : JournalFile {
        override fun read(): ByteArray? = try { file.openRead() } catch (e: FileNotFoundException) {
            if (fs.has(file.base) || fs.has("${file.base}.bak")) throw e
            null
        }
        override fun replace(bytes: ByteArray) {
            val out = file.startWrite()
            try {
                fs.write(out, bytes)
                if (!fs.sync(out)) throw IOException("sync failed")
                file.finishWrite(out)
            } catch (failure: Throwable) { file.failWrite(out); throw failure }
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
    private fun recover(fs: Fs, layout: Layout): Outcome =
        try { Outcome(journal(fs, layout).pending(alice), false) } catch (_: UnreadableAttemptJournal) { Outcome(null, true) }

    private fun prepared(layout: Layout, case: Case): Fs {
        val fs = Fs()
        val journal = journal(fs, layout)
        journal.save(bob, bobCommand)
        case.setup(journal)
        fs.restart()
        return fs
    }

    private fun base(uid: String) = File(directory, "${key(uid)}.journal").path

    private fun assertSameCommand(label: String, expected: AttemptCommand?, actual: AttemptCommand?) {
        assertEquals(label, expected, actual)
        assertEquals(label, expected?.envelope, actual?.envelope)
    }

    /** Recovery through the production adapter: exact old/new state (or preserved unreadable bytes), stable, bob untouched. */
    private fun assertRecovered(label: String, fs: Fs, case: Case, completed: Boolean, allowUnreadable: Boolean): Outcome {
        val first = recover(fs, Layout.DURABLE)
        when {
            completed -> assertEquals(label, Outcome(case.after, false), first)
            first.unreadable -> assertTrue("$label unexpectedly unreadable", allowUnreadable)
            else -> assertTrue("$label recovered ${first.pending?.operationId}", first.pending == case.before || first.pending == case.after)
        }
        first.pending?.let { assertSameCommand(label, if (it == case.after) case.after else case.before, it) }
        val stable = fs.snapshot()
        if (!first.unreadable) assertTrue("$label leftovers ${fs.names()}", fs.names().none { it.endsWith(".new") || it.endsWith(".bak") })
        repeat(2) {
            assertEquals(label, first, recover(fs, Layout.DURABLE))
            assertEquals("$label repeated recovery mutated files", stable, fs.snapshot())
        }
        assertSameCommand("$label bob", bobCommand, journal(fs, Layout.DURABLE).pending(bob))
        if (first.unreadable) {
            assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, Layout.DURABLE).save(alice, next) }
            assertEquals("$label unreadable bytes must be preserved", stable, fs.snapshot())
        } else {
            val journal = journal(fs, Layout.DURABLE)
            first.pending?.let { journal.remove(alice, it) }
            journal.save(alice, next)
            assertSameCommand("$label continuation", next, journal(fs, Layout.DURABLE).pending(alice))
        }
        return first
    }

    @Test fun durableWriteSurvivesCrashAtEveryStepWithoutUnreadableState() {
        var scenarios = 0
        for (case in cases) {
            val total = prepared(Layout.DURABLE, case).let { fs -> case.action(journal(fs, Layout.DURABLE)); fs.ops }
            for (crashAt in 0..total) for (loss in Loss.entries) {
                val fs = prepared(Layout.DURABLE, case)
                fs.crashAt = crashAt
                val completed = try { case.action(journal(fs, Layout.DURABLE)); true } catch (_: Crash) { false }
                assertEquals(crashAt == total, completed)
                fs.restart(loss)
                // Same code on API 24–29 and 30+: no "unreadable although nothing was sent" outcome is allowed.
                assertRecovered("DURABLE/${case.name}/crash@$crashAt/$loss", fs, case, completed, allowUnreadable = false)
                scenarios++
            }
        }
        println("durable crash matrix scenarios=$scenarios unreadable=0")
    }

    @Test fun upgradeReadsOldAtomicFileJournalsAndCrashLeftovers() {
        val preexisting = mutableListOf<String>()
        var scenarios = 0
        for (layout in listOf(Layout.OLD_ATOMICFILE_24_29, Layout.OLD_ATOMICFILE_30)) for (case in cases) {
            val total = prepared(layout, case).let { fs -> case.action(journal(fs, layout)); fs.ops }
            for (crashAt in 0..total) for (loss in oldLosses) {
                val fs = prepared(layout, case)
                fs.crashAt = crashAt
                val completed = try { case.action(journal(fs, layout)); true } catch (_: Crash) { false }
                fs.restart(loss)
                val label = "$layout→DURABLE/${case.name}/crash@$crashAt/$loss"
                val old = recover(fs.copy(), layout)
                // Damage the old API 24–29 adapter already wrote before the upgrade cannot be undone.
                val outcome = assertRecovered(label, fs, case, completed, allowUnreadable = old.unreadable)
                assertEquals("$label must read what the old adapter read", old, outcome)
                if (outcome.unreadable) preexisting += label
                scenarios++
            }
        }
        println("upgrade scenarios=$scenarios preexistingOldLegacyDamage=${preexisting.size}: $preexisting")
        assertTrue(preexisting.all { it.startsWith("OLD_ATOMICFILE_24_29→DURABLE/first save/") })
    }

    @Test fun failedOrUncommittedWritesAreReportedAndRetrySucceeds() {
        val faults = listOf<Pair<String, (Fs) -> Unit>>(
            "disk full" to { it.writeFailsAfter = 7 },
            "file fsync" to { it.syncFails = true },
            "rename refused" to { fs -> fs.renameFails = { source, _ -> source.endsWith(".new") } },
            "rename reports success but did nothing" to { it.renameLies = true },
            "directory fsync" to { it.directorySyncFails = true },
        )
        for (case in cases) for ((fault, inject) in faults) {
            val fs = prepared(Layout.DURABLE, case)
            inject(fs)
            val label = "${case.name}/$fault"
            val error = runCatching { case.action(journal(fs, Layout.DURABLE)) }.exceptionOrNull()
            assertTrue("$label: $error", error is IOException || error is IllegalStateException)
            fs.clearFaults(); fs.restart()
            val durable = recover(fs, Layout.DURABLE)
            assertFalse(label, durable.unreadable)
            // A directory fsync failure happens after the atomic rename: either state is safe to replay.
            if (fault == "directory fsync") assertTrue(label, durable.pending == case.before || durable.pending == case.after)
            else assertSameCommand(label, case.before, durable.pending)
            assertTrue("$label leftovers ${fs.names()}", fs.names().none { it.endsWith(".new") || it.endsWith(".bak") })
            if (durable.pending == case.before) case.action(journal(fs, Layout.DURABLE))
            assertSameCommand("$label retry", case.after, journal(fs, Layout.DURABLE).pending(alice))
            assertSameCommand("$label bob", bobCommand, journal(fs, Layout.DURABLE).pending(bob))
        }
    }

    @Test fun staleBackupAndTempFilesResolveToCommittedState() {
        val fs = Fs()
        journal(fs, Layout.DURABLE).save(alice, checkpoint)
        val base = base(alice)
        val committed = fs.raw(base)!!
        // Old API 24–29 crash leftover: backup is the committed state, base is a partial rewrite.
        fs.put("$base.bak", committed); fs.put(base, committed.copyOf(5))
        assertSameCommand("bak", checkpoint, journal(fs, Layout.DURABLE).pending(alice))
        assertEquals(setOf(base, base(bob)).filter { it == base }.toSet(), fs.names())
        assertArrayEquals(committed, fs.raw(base))
        // `.bak` without base (old adapter crashed between rename and create).
        fs.put("$base.bak", committed); fs.put(base, ByteArray(0)); fs.restart()
        assertSameCommand("bak only", checkpoint, journal(fs, Layout.DURABLE).pending(alice))
        // A complete but uncommitted `.new` never wins.
        val other = Fs().also { journal(it, Layout.DURABLE).save(alice, finish) }.raw(base)!!
        fs.put("$base.new", other)
        assertSameCommand("new", checkpoint, journal(fs, Layout.DURABLE).pending(alice))
        assertEquals(setOf(base), fs.names())
        // `.new` without base is an uncommitted first write, i.e. empty.
        val orphan = Fs()
        orphan.put("$base.new", byteArrayOf(0, 0, 0, 1, 0))
        assertNull(journal(orphan, Layout.DURABLE).pending(alice))
        assertTrue(orphan.names().isEmpty())
        journal(orphan, Layout.DURABLE).save(alice, start)
        assertSameCommand("after orphan new", start, journal(orphan, Layout.DURABLE).pending(alice))
        assertTrue(orphan.directorySyncs > 0)
    }

    @Test fun unreadableOrUnresolvableFileIsNeverTreatedAsEmpty() {
        val fs = Fs()
        journal(fs, Layout.DURABLE).save(alice, start)
        val before = fs.snapshot()
        fs.readDenied = true
        repeat(2) { assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, Layout.DURABLE).pending(alice) } }
        assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, Layout.DURABLE).save(alice, next) }
        assertEquals(before, fs.snapshot())
        fs.clearFaults()
        // A backup that cannot be restored blocks instead of exposing the partial base.
        val base = base(alice)
        fs.put("$base.bak", fs.raw(base)!!); fs.put(base, byteArrayOf(1))
        fs.renameFails = { source, _ -> source.endsWith(".bak") }
        val stuck = fs.snapshot()
        repeat(2) { assertThrows(UnreadableAttemptJournal::class.java) { journal(fs, Layout.DURABLE).pending(alice) } }
        assertEquals(stuck, fs.snapshot())
        fs.clearFaults()
        assertSameCommand("after restore", start, journal(fs, Layout.DURABLE).pending(alice))
    }

    private fun key(uid: String) = java.security.MessageDigest.getInstance("SHA-256")
        .digest(uid.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
