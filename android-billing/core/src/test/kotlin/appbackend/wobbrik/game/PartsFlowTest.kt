package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

/** Real wire/HTTP, recovery owner, journal and inventory reader; no local unlock or value grants. */
class PartsFlowTest {
    private val free = FinishCosmetics.categories.associateWith { "free_$it" }
    private val catalog = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1",
        free.map { (category, id) -> CatalogItem(id, id, category, "free", 0, null, null) } +
            CatalogItem("coins_wall_1", "Wall", "wall", "coins", 4, 100, null) +
            CatalogItem("reward_roof_1", "Roof", "roof", "reward", 4, null, 2) +
            CatalogItem("coins_roof_1", "Expensive roof", "roof", "coins", 4, 2000, null), emptyList())

    @Test fun purchaseUnlockApplyAndFreshDetailUseOnlyAcceptedServerState() = runBlocking {
        val sessions = AccountSessions { "token" }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var revision = 0L
        var unlocked = false
        var edited = false
        var pending: AttemptCommand? = null
        val commands = mutableListOf<AttemptCommand>()
        var syncGate: CompletableDeferred<Unit>? = null
        val journal = object : AttemptJournal {
            override fun pending(uid: String) = pending
            override fun save(uid: String, command: AttemptCommand) { check(pending == null); pending = command }
            override fun remove(uid: String, command: AttemptCommand) { assertEquals(pending, command); pending = null }
        }
        fun snapshot() = """{"rules_version":"wobbrik-v1","revision":$revision,"coins":${if (unlocked) 900 else 1000},"xp":0,"attempt":null,"server_time":"2026-11-13T00:00:00Z","first_online_at":"2026-11-13T00:00:00Z","grace_until":"2026-11-13T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":${if (unlocked) "[\"coins_wall_1\"]" else "[]"},"land":[],"part_progress":[]}"""
        val http = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
            when (path) {
                "sync" -> { syncGate?.await(); GameplayResponse(200, snapshot()) }
                "shop/buy", "buildings/edit" -> {
                    val command = requireNotNull(pending)
                    assertEquals(command.envelope, body)
                    commands += command
                    assertEquals(revision, command.expectedRevision)
                    revision++
                    val result = if (path == "shop/buy") {
                        unlocked = true
                        """{"item_id":"coins_wall_1","price":100}"""
                    } else {
                        check(unlocked)
                        val request = Json.parseToJsonElement(requireNotNull(body)).jsonObject.getValue("request").jsonObject
                        assertEquals(2, request.getValue("building_revision").jsonPrimitive.int)
                        assertEquals("coins_wall_1", request.getValue("parts").jsonObject.getValue("wall").jsonPrimitive.content)
                        edited = true
                        """{"building_id":"building","building_revision":3,"price":0,"fee":0}"""
                    }
                    GameplayResponse(200, """{"revision":$revision,"accepted_at":"2026-11-13T00:00:00Z","result":$result}""")
                }
                "buildings/building" -> {
                    val parts = if (edited) free + ("wall" to "coins_wall_1") else free
                    val encoded = JsonObject(parts.mapValues { JsonPrimitive(it.value) })
                    GameplayResponse(200, """{"id":"building","revision":${if (edited) 3 else 2},"floors":10,"residents":40,"base_value":${if (edited) 24 else 20},"max_price":${if (edited) 28 else 24},"parts":$encoded,"traded":false}""")
                }
                else -> error("Unexpected route $path")
            }
        })
        val backend = object : AttemptBackend by http {
            override suspend fun catalog(session: appbackend.wobbrik.billing.BillingSession) = catalog
        }
        try {
            val owner = AuthenticatedRecoveryOwner(sessions, backend, journal, scope)
            owner.onAuthChanged("alice")
            val initial = owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            val repository = InventoryRepository(http)
            val building = repository.building(requireNotNull(sessions.current()), "building")
            val mutations = PartsMutations(owner) { "op-${commands.size}" }
            val changed = free + ("wall" to "coins_wall_1")
            assertFalse(mutations.apply(initial, building, changed)) // locked
            assertFalse(mutations.apply(initial, building, free)) // no-op
            assertFalse(mutations.buy(initial, "free_wall"))
            assertFalse(mutations.buy(initial, "reward_roof_1"))
            assertFalse(mutations.buy(initial, "unknown"))
            assertFalse(mutations.buy(initial, "coins_roof_1")) // insufficient balance
            assertFalse(mutations.apply(initial, building, free - "roof"))
            assertFalse(mutations.apply(initial, building, free + ("wall" to "free_roof")))
            assertTrue(commands.isEmpty())

            syncGate = CompletableDeferred()
            assertTrue(mutations.buy(initial, "coins_wall_1"))
            assertTrue(owner.state.value is AuthenticatedRecoveryOwner.State.Recovering)
            assertNotNull(pending) // receipt alone cannot enable edit or clear recovery
            assertFalse(FinishCosmetics.available(catalog.items.first { it.id == "coins_wall_1" }, initial.snapshot))
            assertFalse(mutations.buy(initial, "coins_wall_1"))
            assertFalse(mutations.apply(initial, building, changed))
            syncGate!!.complete(Unit); yield()
            val bought = owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            assertEquals(900L, bought.snapshot.coins)
            assertTrue(FinishCosmetics.valid(catalog, bought.snapshot, changed))
            assertFalse(mutations.buy(bought, "coins_wall_1")) // permanent, no double spend
            assertNull(pending)

            syncGate = CompletableDeferred()
            assertTrue(mutations.apply(bought, building, changed))
            assertFalse(mutations.apply(bought, building, changed))
            assertEquals(free, building.parts) // no optimistic mutation
            syncGate!!.complete(Unit); yield()
            val applied = owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            val detail = repository.building(requireNotNull(sessions.current()), building.id)
            assertEquals(2L, applied.snapshot.revision)
            assertEquals(900L, applied.snapshot.coins) // edit costs nothing
            assertEquals(3L, detail.revision)
            assertEquals(changed, detail.parts)
            assertEquals(building.floors, detail.floors)
            assertEquals(building.residents, detail.residents)
            assertEquals(24L, detail.baseValue)
            assertEquals(28L, detail.maxPrice)
            assertFalse(mutations.apply(applied, detail, changed))
            assertEquals(listOf(AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT), commands.map { it.route })
            assertNull(pending)
        } finally { scope.cancel() }
    }
}
