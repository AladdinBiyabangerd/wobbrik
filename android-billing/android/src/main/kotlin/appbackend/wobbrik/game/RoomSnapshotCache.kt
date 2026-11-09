package appbackend.wobbrik.game

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import java.io.File

/** One accepted server snapshot per app + uid; display cache only, never wallet authority. */
@Entity(tableName = "cached_snapshot", primaryKeys = ["app_id", "uid"])
internal data class CachedSnapshotEntity(
    @ColumnInfo(name = "app_id") val appId: String,
    @ColumnInfo(name = "uid") val uid: String,
    @ColumnInfo(name = "rules_version") val rulesVersion: String,
    @ColumnInfo(name = "revision") val revision: Long,
    @ColumnInfo(name = "body") val body: String,
)

@Dao
internal interface CachedSnapshotDao {
    @Query("SELECT * FROM cached_snapshot WHERE app_id = :appId AND uid = :uid")
    suspend fun read(appId: String, uid: String): CachedSnapshotEntity?

    @Upsert
    suspend fun upsert(row: CachedSnapshotEntity)

    @Query("DELETE FROM cached_snapshot WHERE app_id = :appId AND uid = :uid")
    suspend fun delete(appId: String, uid: String)
}

@Database(entities = [CachedSnapshotEntity::class], version = 1, exportSchema = false)
internal abstract class SnapshotCacheDatabase : RoomDatabase() {
    abstract fun snapshots(): CachedSnapshotDao
}

internal class RoomSnapshotCacheStore(private val dao: CachedSnapshotDao) : SnapshotCacheStore {
    override suspend fun read(appId: String, uid: String) =
        dao.read(appId, uid)?.let { CachedSnapshotRow(it.appId, it.uid, it.rulesVersion, it.revision, it.body) }
    override suspend fun replace(row: CachedSnapshotRow) =
        dao.upsert(CachedSnapshotEntity(row.appId, row.uid, row.rulesVersion, row.revision, row.body))
    override suspend fun delete(appId: String, uid: String) = dao.delete(appId, uid)
}

/** Cache file lives outside auto-backup; losing it only loses last-known display values. */
fun androidSnapshotCache(context: Context): SnapshotCache {
    val app = context.applicationContext
    val database = Room.databaseBuilder(app, SnapshotCacheDatabase::class.java, File(app.noBackupFilesDir, "snapshot-cache.db").path)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    return SnapshotCache(RoomSnapshotCacheStore(database.snapshots()))
}
