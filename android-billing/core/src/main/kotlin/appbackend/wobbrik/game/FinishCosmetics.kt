package appbackend.wobbrik.game

/** Server catalog and current-account unlocks are the only selection authority. */
object FinishCosmetics {
    val categories = listOf("wall", "window", "entrance", "roof", "lighting")
    fun available(item: CatalogItem, snapshot: GameSnapshot): Boolean =
        item.category in categories && (item.method == "free" || item.id in snapshot.account?.unlocks.orEmpty())

    fun selection(catalog: RecoveryCatalog, snapshot: GameSnapshot, saved: Map<String, String>): Map<String, String> {
        require(catalog.rulesVersion == snapshot.rulesVersion && catalog.engineVersion == "wobbrik-structural-v1")
        return categories.mapNotNull { category ->
            val choices = catalog.items.filter { it.category == category && available(it, snapshot) }
            (choices.firstOrNull { it.id == saved[category] } ?: choices.firstOrNull { it.method == "free" })
                ?.let { category to it.id }
        }.toMap()
    }

    fun valid(catalog: RecoveryCatalog, snapshot: GameSnapshot, parts: Map<String, String>): Boolean =
        catalog.rulesVersion == snapshot.rulesVersion && catalog.engineVersion == "wobbrik-structural-v1" &&
            parts.keys == categories.toSet() && parts.all { (category, id) ->
                catalog.items.any { it.id == id && it.category == category && available(it, snapshot) }
            }
}
