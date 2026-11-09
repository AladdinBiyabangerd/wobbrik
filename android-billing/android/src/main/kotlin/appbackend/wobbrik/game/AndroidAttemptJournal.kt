package appbackend.wobbrik.game

import android.content.Context
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import java.io.File
import java.io.IOException

/** Keep one instance in the application/session owner. noBackupFilesDir excludes cloud/device backup. */
fun androidAttemptJournal(context: Context): AttemptJournal {
    val directory = File(context.applicationContext.noBackupFilesDir, "game-attempts")
    check(directory.isDirectory || directory.mkdirs())
    // Same durable temp+rename path on every API level; android.util.AtomicFile on API 24–29 rewrote
    // the base file in place for a first write, leaving it empty/partial after a crash.
    val fs = IoJournalFileSystem(::syncDirectory)
    return DiskAttemptJournal { key -> DurableJournalFile(directory, "$key.journal", fs) }
}

private fun syncDirectory(directory: File) {
    val fd = try { Os.open(directory.path, OsConstants.O_RDONLY, 0) } catch (e: ErrnoException) { throw IOException("Cannot open $directory", e) }
    try {
        Os.fsync(fd)
    } catch (e: ErrnoException) {
        // Some filesystems do not support fsync on a directory; the rename itself is still atomic.
        if (e.errno != OsConstants.EINVAL) throw IOException("Directory sync failed for $directory", e)
    } finally {
        try { Os.close(fd) } catch (_: ErrnoException) { }
    }
}
