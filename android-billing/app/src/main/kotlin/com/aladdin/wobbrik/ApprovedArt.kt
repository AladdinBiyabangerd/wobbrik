package com.aladdin.wobbrik

import android.content.res.AssetManager

/** Process-owned, immutable bundled art only: no Activity, account, catalog or ownership data.
 * Three lazy groups bound retention to the shipped atlases (~60 MiB once all are used).
 * Do not recycle these bitmaps: composed screens may still reference them. Process death
 * releases them. The authenticated root prepares these on IO before composing art users.
 */
internal object ApprovedArt {
    @Volatile var prepared = false
        private set

    private var game: Map<String, GameAtlas>? = null
    private var window: Map<String, CosmeticSprite>? = null
    private var cosmetic: Map<String, CosmeticSprite>? = null

    fun prepare(assets: AssetManager) {
        atlases(assets)
        windows(assets)
        cosmetics(assets)
        prepared = true
    }

    @Synchronized
    fun atlases(assets: AssetManager): Map<String, GameAtlas> =
        game ?: loadGameAtlases(assets).also { game = it }

    @Synchronized
    fun windows(assets: AssetManager): Map<String, CosmeticSprite> =
        window ?: loadGameWindowArt(assets).also { window = it }

    @Synchronized
    fun cosmetics(assets: AssetManager): Map<String, CosmeticSprite> =
        cosmetic ?: loadCosmeticArt(assets).also { cosmetic = it }
}
