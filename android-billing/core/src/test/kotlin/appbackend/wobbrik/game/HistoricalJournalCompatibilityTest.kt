package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.billing.Sessions
import appbackend.wobbrik.game.historical.DiskAttemptJournal as OldJournal
import appbackend.wobbrik.game.historical.GameplayWire as OldWire
import appbackend.wobbrik.game.historical.JournalFile as OldFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class HistoricalJournalCompatibilityTest {
    private fun current(root: File) = DiskAttemptJournal { key -> object : JournalFile {
        val file = File(root, key)
        override fun read() = file.takeIf { it.exists() }?.readBytes()
        override fun replace(bytes: ByteArray) { file.writeBytes(bytes) }
    } }
    private fun old(root: File) = OldJournal { key -> object : OldFile {
        val file = File(root, key)
        override fun read() = file.takeIf { it.exists() }?.readBytes()
        override fun replace(bytes: ByteArray) { file.writeBytes(bytes) }
    } }
    private val sessions = object : Sessions {
        override fun current() = BillingSession("alice", "epoch")
        override suspend fun idToken(session: BillingSession): String = error("No real credentials")
    }
    private fun commands() = listOf(
        OldWire.start("old-start", 7),
        OldWire.checkpoint("old-checkpoint", 8, "attempt", 1, 1, 1, 0, "{\"legacy\":true}"),
        OldWire.finish("old-finish", 9, "attempt", 1,
            setOf("wall", "window", "entrance", "roof", "lighting").associateWith { "free_$it" }),
    )
    @Test fun historicalCommandsReplayVerbatimAndDrainOnlyAfterSync() = runBlocking {
        for (expected in commands()) {
            val root = Files.createTempDirectory("old-journal").toFile()
            try {
                old(root).save("alice", expected)
                val file = root.listFiles()!!.single(); val bytes = file.readBytes()
                assertEquals(expected, current(root).pending("alice"))
                assertNull(current(root).pending("bob"))
                assertArrayEquals(bytes, file.readBytes())
                var sends = 0
                val backend = object : AttemptBackend {
                    override suspend fun submit(session: BillingSession, command: AttemptCommand) {
                        sends++; assertEquals(expected, command); assertArrayEquals(bytes, file.readBytes())
                    }
                    override suspend fun sync(session: BillingSession): GameSnapshot {
                        assertArrayEquals(bytes, file.readBytes())
                        return GameSnapshot("wobbrik-v1", 10, 0, 0, null)
                    }
                }
                val coordinator = AttemptCoordinator(sessions, backend, current(root))
                coordinator.recover()
                assertEquals(1, sends); assertTrue(coordinator.ready())
                assertNull(current(root).pending("alice")); assertNull(old(root).pending("alice"))
            } finally { root.deleteRecursively() }
        }
    }
    @Test fun unsupportedVersionsPreserveBytesAndSendNothing() = runBlocking {
        // Deliberately invalid mutations, not claimed historical formats.
        for (variant in listOf("storage", "rules", "engine", "truncated", "identity")) {
            val root = Files.createTempDirectory("invalid-journal").toFile()
            try {
                old(root).save("alice", commands().first())
                val file = root.listFiles()!!.single(); val original = file.readBytes()
                val invalid = when (variant) {
                    "storage" -> original.copyOf().also { it[3] = 2 }
                    "rules" -> String(original, Charsets.ISO_8859_1).replace("wobbrik-v1", "wobbrik-v9").toByteArray(Charsets.ISO_8859_1)
                    "engine" -> String(original, Charsets.ISO_8859_1).replace("wobbrik-structural-v1", "wobbrik-structural-v9").toByteArray(Charsets.ISO_8859_1)
                    "identity" -> String(original, Charsets.ISO_8859_1).replace("alice", "other").toByteArray(Charsets.ISO_8859_1)
                    else -> original.copyOf(3)
                }
                assertFalse(original.contentEquals(invalid)); file.writeBytes(invalid)
                var calls = 0
                val backend = object : AttemptBackend {
                    override suspend fun submit(session: BillingSession, command: AttemptCommand) { calls++; error("Unexpected send") }
                    override suspend fun sync(session: BillingSession): GameSnapshot { calls++; error("Unexpected sync") }
                }
                val coordinator = AttemptCoordinator(sessions, backend, current(root))
                repeat(2) {
                    try { coordinator.recover(); fail("Accepted $variant") } catch (_: UnreadableAttemptJournal) { }
                    assertFalse(coordinator.ready()); assertEquals(0, calls)
                    assertArrayEquals(invalid, file.readBytes())
                }
                assertFalse(coordinator.ready()); assertEquals(0, calls)
                assertArrayEquals(invalid, file.readBytes())
                assertThrows(UnreadableAttemptJournal::class.java) { current(root).save("alice", GameplayWire.start("replacement", 7)) }
                assertArrayEquals(invalid, file.readBytes())
            } finally { root.deleteRecursively() }
        }
    }
}
