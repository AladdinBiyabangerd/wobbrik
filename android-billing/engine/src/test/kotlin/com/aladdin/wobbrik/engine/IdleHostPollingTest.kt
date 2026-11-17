package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.JournalFile
import org.junit.Test
import org.junit.Assert.*
import java.lang.management.ManagementFactory
import java.util.Collections
import java.util.IdentityHashMap

class IdleHostPollingTest {
    @Test fun idleMenuPollingAllocationSample() {
        val online = AcceptedEngineHost()
        val local = LocalGameHost(PendingGameResults { object : JournalFile {
            override fun read(): ByteArray? = null
            override fun replace(bytes: ByteArray) = Unit
        } })
        repeat(10000) { online.onFrame(it.toLong()); online.enforceLeave(null); online.snapshot(); local.frame(it.toLong()); local.snapshot() }
        val bean = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
        val thread = Thread.currentThread().threadId()
        val onlineImages = Collections.newSetFromMap(IdentityHashMap<AcceptedEngineHost.Snapshot, Boolean>())
        val localImages = Collections.newSetFromMap(IdentityHashMap<AcceptedEngineHost.Snapshot, Boolean>())
        val before = bean.getThreadAllocatedBytes(thread)
        val start = System.nanoTime()
        repeat(3600) { frame ->
            online.onFrame(frame.toLong()); online.enforceLeave(null); onlineImages.add(online.snapshot())
            local.frame(frame.toLong()); localImages.add(local.snapshot())
        }
        val elapsed = System.nanoTime() - start
        val bytes = bean.getThreadAllocatedBytes(thread) - before
        assertEquals(1, onlineImages.size)
        assertEquals(1, localImages.size)
        println("IDLE-POLL frames=3600 onlineUnique=${onlineImages.size} localUnique=${localImages.size} allocatedBytes=$bytes elapsedUs=${elapsed/1000}")
    }
}
