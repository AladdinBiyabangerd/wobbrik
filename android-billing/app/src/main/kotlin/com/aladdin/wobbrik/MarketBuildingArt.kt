package com.aladdin.wobbrik

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import appbackend.wobbrik.game.OwnedBuilding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** Two bounded immutable APK materials, loaded only when their selected wall is mounted.
 * No account data or Activity retention; never decoded on the drawing thread. */
internal object MarketBuildingMaterials {
    @Volatile private var materials = emptyMap<String, CosmeticSprite>()
    fun supports(id: String?): Boolean = id == "base_2_wall" || id == "base_4_wall"
    fun prepared(id: String?): CosmeticSprite? = materials[id]

    @Synchronized
    fun load(assets: AssetManager, id: String): CosmeticSprite {
        require(supports(id))
        materials[id]?.let { return it }
        val file = if (id == "base_2_wall") "market-brick-v1.png" else "market-panel-v1.png"
        val bitmap = assets.open("building-purple/$file").use {
            requireNotNull(BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 4 }))
        }.asImageBitmap()
        val sprite = if (id == "base_2_wall") {
            // Five brick courses per storey, excluding the image-edge seam.
            CosmeticSprite(bitmap, IntOffset(bitmap.width / 8, bitmap.height / 4),
                IntSize(bitmap.width * 3 / 4, bitmap.height * 5 / 12))
        } else {
            // Two panel bays per storey; preserve the fasteners and fine seams.
            CosmeticSprite(bitmap, IntOffset.Zero, IntSize(bitmap.width / 2, bitmap.height / 5))
        }
        materials = materials + (id to sprite)
        return sprite
    }
}

/** Market-only material pass. The prepared atlas, selected IDs and geometry stay shared. */
@Composable
internal fun MarketBuildingPreview(building: OwnedBuilding, modifier: Modifier) {
    // The catalog's brick is base_2; base_1 is limestone. Do not recolour an
    // unrelated selected material merely because the reference's first card is brick.
    val wallId = building.parts["wall"]
    key(wallId) { MarketBuildingMaterialPreview(building, modifier, wallId) }
}

@Composable
private fun MarketBuildingMaterialPreview(building: OwnedBuilding, modifier: Modifier, wallId: String?) {
    var material = MarketBuildingMaterials.prepared(wallId)
    if (MarketBuildingMaterials.supports(wallId) && material == null) {
        val assets = LocalContext.current.assets
        var retry by remember { mutableIntStateOf(0) }
        val status = rememberArtPreparation(retry) { MarketBuildingMaterials.load(assets, requireNotNull(wallId)) }
        if (status != ArtPreparation.Ready) {
            androidx.compose.foundation.layout.Box(modifier) { ApprovedArtStatus(status) { retry++ } }
            return
        }
        material = requireNotNull(MarketBuildingMaterials.prepared(wallId))
    }
    BuildingPreview(building, modifier, marketMaterials = true, marketWall = material)
}

/** Light follows each masonry plane, rather than adding a halo behind the whole tower.
 * Coordinates are the renderer's existing art anchors, never accepted game geometry.
 * Translucent light preserves the selected material's texture and colour identity.
 */
internal fun DrawScope.marketFacadeLight(corners: List<Offset>, side: Boolean) {
    val face = Path().apply {
        moveTo(corners[0].x, corners[0].y)
        corners.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    val top = (corners[0] + corners[1]) / 2f
    val bottom = (corners[2] + corners[3]) / 2f
    clipPath(face) {
        drawRect(Brush.linearGradient(
            listOf(GameTokens.Background.copy(alpha = if (side) .42f else .28f),
                GameTokens.Background.copy(alpha = 0f),
                GameTokens.MobileHome.GoldLight.copy(alpha = if (side) .05f else .16f)),
            top, bottom))
        // Right face turns away from the warm facade light; the corner stays crisp.
        if (side) drawRect(Brush.linearGradient(
            listOf(GameTokens.Background.copy(alpha = .26f), GameTokens.Background.copy(alpha = 0f)),
            corners[0], corners[1]))
    }
}

/** Material-only patch: exclude the legacy catalog thumbnail's frame and right return.
 * Generated brick/panel sprites are already material-only patches with their original IDs. */
internal fun marketWallSample(wall: CosmeticSprite, id: String?): CosmeticSprite =
    if (MarketBuildingMaterials.supports(id)) wall else wall.copy(
        origin = IntOffset(wall.origin.x + (wall.size.width * .14f).toInt(),
            wall.origin.y + (wall.size.height * .29f).toInt()),
        size = IntSize((wall.size.width * .64f).toInt(), (wall.size.height * .48f).toInt()))
