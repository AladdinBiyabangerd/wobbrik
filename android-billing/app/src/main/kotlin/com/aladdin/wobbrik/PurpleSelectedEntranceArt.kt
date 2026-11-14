package com.aladdin.wobbrik

import androidx.compose.ui.unit.IntSize

/** Frontal elevation of the existing entrance art. Catalog cells also contain
 * a right return and baked stairs/supports; these cannot be flattened onto the
 * front wall. The approved template already owns the landing and ground stairs.
 * Pixel bounds end at each portal's front threshold, not the sprite's bottom.
 */
internal fun purpleEntranceElevation(item: CosmeticSprite, id: String?): CosmeticSprite {
    val extent=when(id) {
        "base_2_entrance" -> IntSize(202,243)
        "base_3_entrance" -> IntSize(180,260)
        "base_4_entrance" -> IntSize(201,257)
        "base_5_entrance" -> IntSize(177,260)
        "base_6_entrance" -> IntSize(199,252)
        "coins_entrance_1" -> IntSize(198,245)
        "coins_entrance_2" -> IntSize(213,262)
        "coins_entrance_3" -> IntSize(183,252)
        "reward_entrance_1" -> IntSize(195,245)
        "reward_entrance_2" -> IntSize(199,249)
        "reward_entrance_3" -> IntSize(195,240)
        "paid_entrance_1" -> IntSize(198,260)
        "balance_entry" -> IntSize(202,260)
        else -> return item
    }
    return item.copy(size=extent)
}
