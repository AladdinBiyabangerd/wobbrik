package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/** Production [DurableJournalFile] + [IoJournalFileSystem] on the host's real filesystem (not Android). */
class DurableJournalFileTest {
    private var directorySyncs = 0
    private val fs = IoJournalFileSystem { dir ->
        directorySyncs++
        FileChannel.open(dir.toPath(), StandardOpenOption.READ).use { it.force(true) }
    }

    private fun <T> withDirectory(block: (File) -> T): T {
        val dir = Files.createTempDirectory("durable-journal").toFile()
        try { return block(dir) } finally { dir.deleteRecursively() }
    }
    private fun journal(dir: File) = DiskAttemptJournal { key -> DurableJournalFile(dir, "$key.journal", fs) }

    @Test fun roundTripAtomicReplaceAndLegacyLeftovers() = withDirectory { dir ->
        val command = GameplayWire.start("op", 2)
        journal(dir).save("alice", command)
        assertEquals(command, journal(dir).pending("alice"))
        assertNull(journal(dir).pending("bob"))
        val base = dir.listFiles()!!.single()
        assertTrue(base.name.endsWith(".journal"))
        assertTrue(directorySyncs > 0)
        // Old android.util.AtomicFile leftovers: `.bak` committed, partial base, stale `.new`.
        val committed = base.readBytes()
        File(dir, "${base.name}.bak").writeBytes(committed)
        base.writeBytes(committed.copyOf(4))
        File(dir, "${base.name}.new").writeBytes(byteArrayOf(9, 9))
        assertEquals(command, journal(dir).pending("alice"))
        assertEquals(listOf(base.name), dir.list()!!.toList())
        journal(dir).remove("alice", command)
        assertNull(journal(dir).pending("alice"))
        assertEquals(listOf(base.name), dir.list()!!.toList())
    }

    @Test fun oversizedOrCorruptFileIsUnreadableAndPreserved() = withDirectory { dir ->
        journal(dir).save("alice", GameplayWire.start("op", 2))
        val base = dir.listFiles()!!.single()
        for (bytes in listOf(ByteArray(DurableJournalFile.MaxBytes + 1), ByteArray(0), byteArrayOf(0, 0, 0, 1))) {
            base.writeBytes(bytes)
            assertThrows(UnreadableAttemptJournal::class.java) { journal(dir).pending("alice") }
            assertThrows(UnreadableAttemptJournal::class.java) { journal(dir).save("alice", GameplayWire.start("other", 3)) }
            assertArrayEquals(bytes, base.readBytes())
        }
    }
}
