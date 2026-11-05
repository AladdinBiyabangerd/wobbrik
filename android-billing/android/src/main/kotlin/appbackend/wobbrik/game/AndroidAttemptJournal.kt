package appbackend.wobbrik.game

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException

/** Keep one instance in the application/session owner. noBackupFilesDir excludes cloud/device backup. */
fun androidAttemptJournal(context: Context): AttemptJournal {
    val directory = File(context.applicationContext.noBackupFilesDir, "game-attempts")
    check(directory.isDirectory || directory.mkdirs())
    return DiskAttemptJournal { key ->
        val atomic = AtomicFile(File(directory, "$key.journal"))
        object : JournalFile {
            override fun read(): ByteArray? = try {
                atomic.openRead().use { input ->
                    val bytes = input.readBytesBounded()
                    bytes
                }
            } catch (e: FileNotFoundException) {
                // Permission/corruption errors must not masquerade as an empty journal.
                if (atomic.baseFile.exists() || File(atomic.baseFile.path + ".bak").exists()) throw e
                null
            }
            override fun replace(bytes: ByteArray) {
                val output = atomic.startWrite()
                try { output.write(bytes); output.fd.sync(); atomic.finishWrite(output) }
                catch (failure: Throwable) { atomic.failWrite(output); throw failure }
            }
        }
    }
}
private fun java.io.InputStream.readBytesBounded(): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(4096)
    while (out.size() <= 140000) {
        val count = read(buffer, 0, minOf(buffer.size, 140001 - out.size()))
        if (count < 0) break
        out.write(buffer, 0, count)
    }
    require(out.size() <= 140000) { "Journal too large" }
    return out.toByteArray()
}
