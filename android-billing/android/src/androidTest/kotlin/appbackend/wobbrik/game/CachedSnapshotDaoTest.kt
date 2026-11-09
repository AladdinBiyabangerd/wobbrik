package appbackend.wobbrik.game

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CachedSnapshotDaoTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var file: File
    private lateinit var db: SnapshotCacheDatabase

    @Before fun open() {
        file = File(context.cacheDir, "snapshot-cache-test-${System.nanoTime()}.db")
        db = build()
    }

    @After fun close() {
        db.close()
        context.deleteDatabase(file.path)
    }

    private fun build() = Room.databaseBuilder(context, SnapshotCacheDatabase::class.java, file.path).build()

    @Test fun upsertReplacesOnlyTheSameAppAndUid() = runBlocking {
        val dao = db.snapshots()
        dao.upsert(row("wobbrik", "alice", 1, 10))
        dao.upsert(row("wobbrik", "bob", 1, 20))
        dao.upsert(row("other", "alice", 1, 30))
        dao.upsert(row("wobbrik", "alice", 2, 11))
        assertEquals(row("wobbrik", "alice", 2, 11), dao.read("wobbrik", "alice"))
        assertEquals(20L, coins(dao.read("wobbrik", "bob")!!))
        assertEquals(30L, coins(dao.read("other", "alice")!!))
        assertNull(dao.read("wobbrik", "carol"))
        dao.delete("wobbrik", "alice")
        assertNull(dao.read("wobbrik", "alice"))
        assertNotNull(dao.read("other", "alice"))
    }

    @Test fun rowsSurviveReopenAndCacheRulesRunOverRoom() = runBlocking {
        val cache = SnapshotCache(RoomSnapshotCacheStore(db.snapshots()))
        cache.accepted("alice", GameplayWire.snapshot(body(4, 40)))
        cache.accepted("alice", GameplayWire.snapshot(body(4, 540)))
        cache.accepted("alice", GameplayWire.snapshot(body(3, 30)))
        db.close()
        db = build()
        val reopened = SnapshotCache(RoomSnapshotCacheStore(db.snapshots()))
        val last = reopened.lastKnown("alice")!!
        assertEquals(4L, last.revision)
        assertEquals(540L, last.coins)
        db.snapshots().upsert(CachedSnapshotEntity("wobbrik", "bob", "wobbrik-v2", 9, body(9, 90)))
        assertNull(reopened.lastKnown("bob"))
        assertNull(db.snapshots().read("wobbrik", "bob"))
    }

    private fun row(app: String, uid: String, revision: Long, coins: Long) =
        CachedSnapshotEntity(app, uid, "wobbrik-v1", revision, body(revision, coins))

    private fun coins(row: CachedSnapshotEntity) = GameplayWire.snapshot(row.body).coins

    private fun body(revision: Long, coins: Long) =
        """{"rules_version":"wobbrik-v1","revision":$revision,"coins":$coins,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
}
