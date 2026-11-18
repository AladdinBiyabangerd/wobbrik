package com.aladdin.wobbrik

import android.content.res.AssetManager

/** Process-owned, immutable bundled art only: no Activity, account, catalog or ownership data.
 * Four lazy groups bound retention to the shipped atlases (~84 MiB once all are used).
 * Purple preview modules are shared across cards and route remounts, rather than
 * allocating another ~24 MiB per composed building.
 * Do not recycle these bitmaps: composed screens may still reference them. Process death
 * releases them. The authenticated root prepares these on IO before composing art users.
 */
internal object ApprovedArt {
    @Volatile var prepared = false
        private set

    private var game: Map<String, GameAtlas>? = null
    private var window: Map<String, CosmeticSprite>? = null
    private var purpleBuilding: Map<String, CosmeticSprite>? = null
    private var cosmetic: Map<String, CosmeticSprite>? = null

    fun prepare(assets: AssetManager) {
        atlases(assets)
        windows(assets)
        cosmetics(assets)
        purple(assets)
        prepared = true
    }

    @Synchronized
    fun atlases(assets: AssetManager): Map<String, GameAtlas> =
        game ?: loadGameAtlases(assets).also { game = it }

    @Synchronized
    fun windows(assets: AssetManager): Map<String, CosmeticSprite> =
        window ?: loadGameWindowArt(assets).also { window = it }

    @Synchronized
    fun purple(assets: AssetManager): Map<String, CosmeticSprite> =
        purpleBuilding ?: loadPurpleBuildingArt(assets).also { purpleBuilding = it }

    @Synchronized
    fun cosmetics(assets: AssetManager): Map<String, CosmeticSprite> =
        cosmetic ?: loadCosmeticArt(assets).also { cosmetic = it }
}
