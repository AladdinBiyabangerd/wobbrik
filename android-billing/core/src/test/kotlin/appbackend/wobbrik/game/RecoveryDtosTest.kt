package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test

class RecoveryDtosTest {
    private val body = """{"rules_version":"wobbrik-v1","revision":4,"coins":8,"xp":2,
      "server_time":"2026-11-05T01:00:00Z","first_online_at":"2026-11-04T00:00:00Z",
      "grace_until":"2026-11-04T20:00:00Z","level":1,"rescues":1,
      "achievements":["a"],"unlocks":["part"],"land":["base"],
      "part_progress":[{"item_id":"reward_wall_1","completed":1,"required":3,"unlocked":false}],
      "attempt":{"attempt_id":"a","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1",
      "sequence":2,"floors":3,"precise":2,"rescues":1,"status":"active","continued":true,"snapshot":{"v":1}}}"""
    @Test fun preservesFullRecoveryAndHistoricalReceipts() {
        val s = GameplayWire.snapshot(body)
        assertEquals(3L, s.attempt!!.floors); assertTrue(s.attempt.continued)
        assertEquals(listOf("base"), s.account!!.land); assertEquals(1L, s.account.partProgress.single().completed)
        assertFalse(GameplayWire.snapshot(body.replace("\"active\"", "\"finished\"")).attempt!!.active)
        val receipt = GameplayWire.receipt("""{"revision":1,"accepted_at":"2026-11-05T00:00:00Z","result":{"attempt_id":"a"}}""")
        assertEquals(1L, receipt.revision); assertTrue(receipt.result.containsKey("attempt_id"))
    }
    @Test fun failsClosedOnInvalidRecovery() {
        listOf(body.replace("\"precise\":2", "\"precise\":4"), body.replace("\"continued\":true", "\"continued\":\"true\""),
            body.replace("wobbrik-structural-v1", "future"), body.replace("2026-11-05T01:00:00Z", "yesterday"),
            body.replace("\"completed\":1", "\"completed\":4")).forEach {
            assertThrows(Exception::class.java) { GameplayWire.snapshot(it) }
        }
    }
    @Test fun typedCheckpointAndLegacyFinishReceipts() {
        fun receipt(result: String) = """{"revision":2,"accepted_at":"2026-11-05T00:00:00Z","result":$result}"""
        val cp = RecoveryDtos.attemptReceipt(receipt("""{"xp":10,"level":1,"rescues":2,"granted":["a"],"coins":5}"""), AttemptCommand.Route.CHECKPOINT)
        assertEquals(5L, (cp.result as AttemptResult.Checkpointed).coins)
        val finish = RecoveryDtos.attemptReceipt(receipt("""{"attempt_id":"a","floors":2,"coins":0,"residents":0,"base_value":0,"max_price":0}"""), AttemptCommand.Route.FINISH)
        assertNull((finish.result as AttemptResult.Finished).resultId)
        assertThrows(Exception::class.java) { RecoveryDtos.attemptReceipt(receipt("""{"attempt_id":"a"}"""), AttemptCommand.Route.CHECKPOINT) }
        assertThrows(IllegalArgumentException::class.java) { GameplayWire.checkpoint("c", 1, "a", 1, 0, 0, 0, "{}") }
        assertThrows(IllegalArgumentException::class.java) { GameplayWire.checkpoint("c", 1, "a", 1, 1, 0, 2, "{}") }
    }
    @Test fun catalogPreservesOptionalZeroOmittedValues() {
        val c = RecoveryDtos.catalog("""{"rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1",
          "items":[{"id":"wall","name":"Wall","category":"wall","method":"free","weight":1}],
          "land_offers":[{"id":"land_1","required_xp":10,"price":100}]}""")
        assertNull(c.items.single().price); assertEquals(100L, c.landOffers.single().price)
    }
}
