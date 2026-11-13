package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import kotlin.coroutines.coroutineContext
import kotlin.math.sin

/** Only the Canvas reads this state, so ambient frames do not recompose navigation or reload data.
 * The clock suspends off-screen/in background and respects the platform animation duration scale. */
@Composable
internal fun rememberCityLifeClock(enabled: Boolean, sceneKey: Any?): State<Double> {
    val time = remember(sceneKey) { mutableDoubleStateOf(0.0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(enabled, sceneKey, lifecycle) {
        if (!enabled) return@LaunchedEffect
        val motion = coroutineContext[MotionDurationScale]
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            snapshotFlow { motion?.scaleFactor ?: 1f }.collectLatest { scale ->
                if (scale <= 0f) { time.doubleValue = 0.0; awaitCancellation() }
                var previous = withFrameNanos { it }
                var pending = 0.0
                while (true) {
                    val now = withFrameNanos { it }
                    pending += (now - previous).coerceAtLeast(0) / 1_000_000_000.0
                    previous = now
                    // A small ambient scene needs at most 30 redraws/second, no catch-up after pause.
                    if (pending >= 1.0 / 30) {
                        time.doubleValue += pending / scale
                        pending = 0.0
                    }
                }
            }
        }
    }
    return time
}

@Composable
internal fun rememberCityLifeArt(): Map<String, CosmeticSprite> {
    val context = LocalContext.current
    return remember(context) {
        val data = JSONObject(context.assets.open("city/life-actors-v1.json").bufferedReader().use { it.readText() })
        val image = context.assets.open("city/${data.getString("source")}").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        val entries = data.getJSONArray("sprites")
        (0 until entries.length()).associate { index ->
            val entry = entries.getJSONObject(index)
            entry.getString("id") to CosmeticSprite(image,IntOffset(entry.getInt("x"),entry.getInt("y")),
                IntSize(entry.getInt("width"),entry.getInt("height")))
        }
    }
}

internal data class CityLifeActor(val route: CityLifeRoutes.Route, val position: CityBuildingProjection.Point,
    val direction: String, val seconds: Double)

internal fun cityLifeActors(routes: List<CityLifeRoutes.Route>, seconds: Double): List<CityLifeActor> = routes.map { route ->
    val t = seconds + (route.id.hashCode().toUInt().toLong() % 1700) / 100.0
    val world = route.position(t)
    val after = route.position(t + 0.001)
    val direction = when {
        after.x > world.x -> "ne"
        after.x < world.x -> "sw"
        after.y > world.y -> "nw"
        else -> "se"
    }
    CityLifeActor(route,CityBuildingProjection.project(world.x,world.y),direction,t)
}

internal fun DrawScope.cityLifeActor(actor: CityLifeActor, view: CityViewport, art: Map<String, CosmeticSprite>, moving: Boolean) {
    val resident = actor.route.kind == CityLifeRoutes.Kind.Resident
    val sprite = art["${if (resident) "resident" else "car"}_${actor.direction}"] ?: return
    // World-space dimensions keep people and vehicles subordinate to real 100-unit building floors.
    val height = if (resident) 18.0 else 23.0
    val width = height * sprite.size.width / sprite.size.height
    val bob = if (resident && moving) sin(actor.seconds * 9) * 0.35 else 0.0
    val x = view.x + actor.position.x * view.scale
    val y = view.y + (actor.position.y - bob) * view.scale
    sprite(sprite,(x - width*view.scale/2).toFloat(),(y - height*view.scale).toFloat(),
        (width*view.scale).toFloat(),(height*view.scale).toFloat())
}

internal fun cityWindowAlpha(id: String, floor: Int, seconds: Double): Float {
    val phase = (id.hashCode().toUInt().toLong() % 1000) / 100.0 + floor * 1.7
    return (0.82 + 0.18 * sin(seconds * 0.35 + phase)).toFloat()
}

internal fun DrawScope.cityLivingDecoration(drawing: CityDecorationDrawing, view: CityViewport, seconds: Double, moving: Boolean) {
    if (!moving || !drawing.source.copy.itemId.startsWith("decor_tree_")) {
        cityDecoration(drawing,view)
        return
    }
    val phase = (drawing.source.copy.id.hashCode().toUInt().toLong() % 1000) / 100.0
    val angle = (sin(seconds * 1.1 + phase) * 0.45).toFloat()
    rotate(angle,Offset((view.x+drawing.anchor.x*view.scale).toFloat(),(view.y+drawing.anchor.y*view.scale).toFloat())) {
        cityDecoration(drawing,view)
    }
}
