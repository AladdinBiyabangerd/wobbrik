package appbackend.wobbrik.game

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/** File primitives for [DurableJournalFile]; failures must throw or return false, never pretend success. */
interface JournalFileSystem {
    fun exists(file: File): Boolean
    fun read(file: File, limit: Int): ByteArray
    /** Create or truncate, write everything and fsync before returning. */
    fun writeSynced(file: File, bytes: ByteArray)
    /** Atomically replaces [to] (POSIX rename). */
    fun rename(from: File, to: File): Boolean
    fun delete(file: File): Boolean
    /** Makes completed renames/deletes in [directory] durable. */
    fun syncDirectory(directory: File)
}

/**
 * Crash-safe single journal file on every API level: write `<name>.new`, fsync, rename over `<name>`,
 * fsync the directory. The base file only ever holds a complete, synced write, so a crash leaves the
 * previous state or the new one, never an empty/partial base.
 *
 * Reads stay compatible with files left by `android.util.AtomicFile`: a legacy `.bak` (API 24–29) is
 * the committed state and is restored, as AtomicFile.openRead did; `.new` is never committed and is
 * deleted. Not thread-safe; [DiskAttemptJournal] serialises access and must be the only writer.
 */
class DurableJournalFile(private val directory: File, name: String, private val fs: JournalFileSystem) : JournalFile {
    private val base = File(directory, name)
    private val next = File(directory, "$name.new")
    private val backup = File(directory, "$name.bak")

    override fun read(): ByteArray? {
        resolveLeftovers()
        return if (fs.exists(base)) fs.read(base, MaxBytes) else null
    }

    override fun replace(bytes: ByteArray) {
        require(bytes.size <= MaxBytes) { "Journal too large" }
        resolveLeftovers()
        try {
            fs.writeSynced(next, bytes)
        } catch (failure: Exception) {
            fs.delete(next)
            throw failure
        }
        if (!fs.rename(next, base)) {
            fs.delete(next)
            throw IOException("Could not commit journal $base")
        }
        fs.syncDirectory(directory)
    }

    private fun resolveLeftovers() {
        if (fs.exists(backup) && !fs.rename(backup, base)) throw IOException("Could not restore journal backup $backup")
        if (fs.exists(next) && !fs.delete(next)) throw IOException("Could not delete uncommitted journal $next")
    }

    companion object { const val MaxBytes = 140000 }
}

/** java.io only (API 24 has no java.nio.file). Directory sync is platform-specific and injected. */
class IoJournalFileSystem(private val directorySync: (File) -> Unit) : JournalFileSystem {
    override fun exists(file: File) = file.exists()
    override fun read(file: File, limit: Int): ByteArray = FileInputStream(file).use { it.readBounded(limit) }
    override fun writeSynced(file: File, bytes: ByteArray) {
        FileOutputStream(file).use { it.write(bytes); it.fd.sync() }
    }
    override fun rename(from: File, to: File) = from.renameTo(to)
    override fun delete(file: File) = file.delete() || !file.exists()
    override fun syncDirectory(directory: File) = directorySync(directory)

    private fun InputStream.readBounded(limit: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (out.size() <= limit) {
            val count = read(buffer, 0, minOf(buffer.size, limit + 1 - out.size()))
            if (count < 0) break
            out.write(buffer, 0, count)
        }
        if (out.size() > limit) throw IOException("Journal too large")
        return out.toByteArray()
    }
}
