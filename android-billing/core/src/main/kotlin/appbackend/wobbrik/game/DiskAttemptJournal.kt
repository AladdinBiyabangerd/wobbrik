package appbackend.wobbrik.game

import java.io.*
import java.security.MessageDigest
import java.util.concurrent.CancellationException

/** Atomic storage must fsync writes before replacement and retain old data on failed writes. */
interface JournalFile {
    fun read(): ByteArray?
    fun replace(bytes: ByteArray)
}

/** Local storage/format failure; retry must preserve the original bytes and never dispatch. */
class UnreadableAttemptJournal(cause: Exception) : IllegalArgumentException("Saved attempt journal cannot be read by this version", cause)

/** One application-scoped instance; never open a second writer for the same directory. */
class DiskAttemptJournal(private val file: (String) -> JournalFile) : AttemptJournal {
    @Synchronized override fun pending(uid: String): AttemptCommand? = read(uid)
    @Synchronized override fun save(uid: String, command: AttemptCommand) {
        GameplayWire.validate(command)
        check(read(uid) == null) { "Pending command already exists" }
        write(uid, command)
    }
    @Synchronized override fun remove(uid: String, command: AttemptCommand) {
        check(read(uid) == command) { "Pending command changed" }
        // A durable tombstone avoids a delete resurrecting a backup after process death.
        write(uid, null)
    }
    private fun key(uid: String): String {
        require(uid.isNotBlank())
        return MessageDigest.getInstance("SHA-256").digest(uid.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
    private fun read(uid: String): AttemptCommand? = try {
        decode(uid)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        throw UnreadableAttemptJournal(failure)
    }
    private fun decode(uid: String): AttemptCommand? {
        val bytes = file(key(uid)).read() ?: return null
        require(bytes.size <= 140000) { "Journal too large" }
        return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == 1 && input.readUTF() == uid) { "Invalid journal identity/version" }
            val command = if (input.readBoolean()) AttemptCommand(input.readUTF(), input.readLong(), AttemptCommand.Route.valueOf(input.readUTF()), input.readUTF()) else null
            require(input.available() == 0)
            command?.also(GameplayWire::validate)
        }
    }
    private fun write(uid: String, command: AttemptCommand?) {
        val out = ByteArrayOutputStream()
        DataOutputStream(out).use {
            it.writeInt(1); it.writeUTF(uid); it.writeBoolean(command != null)
            command?.let { c -> it.writeUTF(c.operationId); it.writeLong(c.expectedRevision); it.writeUTF(c.route.name); it.writeUTF(c.envelope) }
        }
        file(key(uid)).replace(out.toByteArray())
        // android.util.AtomicFile only logs some rename/delete failures; never report an uncommitted write.
        check(read(uid) == command) { "Attempt journal write was not committed" }
    }
}
