package com.aladdin.wobbrik

import android.content.res.AssetManager

/** Process-owned, immutable bundled art only: no Activity, account, catalog or ownership data.
 * Three lazy groups bound retention to the shipped atlases (~60 MiB once all are used).
 * Do not recycle these bitmaps: composed screens may still reference them. Process death
 * releases them. No startup warmup; the first caller still pays the cold decode cost.
 */
internal object ApprovedArt {
    private var game: Map<String, GameAtlas>? = null
    private var window: Map<String, CosmeticSprite>? = null
    private var cosmetic: Map<String, CosmeticSprite>? = null

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
