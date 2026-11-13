package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.game.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class LocalGameHostTest {
    private class Fixture {
        val bytes = mutableMapOf<String, ByteArray>()
        var fail = false
        val files: (String) -> JournalFile = { key -> object : JournalFile {
            override fun read() = bytes[key]
            override fun replace(bytes: ByteArray) { if (fail) throw IOException("disk"); this@Fixture.bytes[key] = bytes.clone() }
        } }
        val results = PendingGameResults(files)
        var next = 0
        val host = LocalGameHost(results, id = { "local-${++next}" })
        val session = BillingSession("alice", "epoch1")
        val account = ReadOnlyAccount("alice", GameSnapshot("wobbrik-v1", 1, 12, 0, null),
            RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", FinishCosmetics.categories.map {
                CatalogItem("base_$it", it, it, "free", 0, null, null)
            }, emptyList()))
        var now = 0L
        init { host.bind(session); host.foreground(true) }
        fun start() { assertTrue(host.start(session, account)); assertTrue(host.resume()) }
        fun frames(n: Int) { repeat(n) { now += 16_666_667; host.frame(now) } }
        fun lose() { frames(90); assertTrue(host.drop()); frames(80) }
    }
    @Test fun offlineSceneUsesRealPhysicsAndDurablyQueuesMultipleResults() {
        val f = Fixture()
        repeat(2) { f.start(); f.lose(); assertFalse(f.host.snapshot().playing) }
        val reopened = PendingGameResults(f.files).pending("alice")
        assertEquals(listOf("local-1", "local-2"), reopened.map { it.id })
        assertTrue(reopened.all { !it.started && it.terminal.state.phase == Phase.FINISHED && it.terminal.state.paused })
        assertEquals(12L, f.account.snapshot.coins)
        assertTrue(f.results.pending("bob").isEmpty())
    }
    @Test fun diskFailureRetainsTerminalAcrossLeaveAndAuthEpochUntilExplicitRetry() {
        val f = Fixture(); f.start(); f.fail = true; f.lose()
        assertTrue(f.host.snapshot().finishStorageError)
        val terminal = f.host.snapshot().scene
        f.host.leave(); assertEquals(terminal, f.host.snapshot().scene)
        f.host.bind(BillingSession("bob", "epoch2"))
        assertNull(f.host.snapshot().scene); assertFalse(f.host.drop())
        val relogin = BillingSession("alice", "epoch3")
        f.host.bind(relogin)
        assertFalse(f.host.start(f.session, f.account))
        assertTrue(f.host.start(relogin, f.account))
        assertEquals(terminal, f.host.snapshot().scene)
        assertFalse(f.host.resume()); assertFalse(f.host.retryTerminal())
        f.fail = false; assertTrue(f.host.retryTerminal())
        assertEquals(1, f.results.pending("alice").size)
    }
    @Test fun lifecycleDoesNotAdvanceBackgroundTimeAndOpenLeaveDiscardsOnlyOpenScene() {
        val f = Fixture(); f.start(); f.frames(10)
        f.host.foreground(false); val paused = f.host.snapshot().scene
        f.now += 100_000_000_000; f.host.frame(f.now)
        assertEquals(paused, f.host.snapshot().scene)
        f.host.foreground(true); assertFalse(f.host.snapshot().inputEnabled)
        assertTrue(f.host.resume()); f.host.frame(f.now)
        assertEquals(paused?.copy(paused = false), f.host.snapshot().scene)
        f.host.leave(); assertFalse(f.host.snapshot().playing); assertTrue(f.results.pending("alice").isEmpty())
    }
    @Test fun sameUidNewEpochRevokesOpenSceneAndCorruptQueueNeverGetsOverwritten() {
        val f = Fixture(); f.start(); f.host.bind(BillingSession("alice", "new"))
        assertFalse(f.host.drop()); assertFalse(f.host.start(f.session, f.account))
        f.host.bind(f.session)
        f.bytes["unused"] = byteArrayOf() // Force the real queue row to exist first.
        f.start(); f.lose()
        val key = f.bytes.keys.single { it != "unused" }
        f.bytes[key] = "corrupt".toByteArray()
        assertTrue(runCatching { f.host.start(f.session, f.account) }.isFailure)
        assertEquals("corrupt", f.bytes[key]!!.toString(Charsets.UTF_8))
    }
}
