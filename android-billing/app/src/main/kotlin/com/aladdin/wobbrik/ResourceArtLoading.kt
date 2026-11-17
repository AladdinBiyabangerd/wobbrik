package com.aladdin.wobbrik

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class ResourceArt(val images: List<ImageBitmap>? = null, val failed: Boolean = false)

/** Composition-owned only: no process cache, Activity retention, or account data.
 * A configuration change replaces the state immediately; old workers cannot publish into it.
 */
@Composable
internal fun rememberResourceArt(vararg ids: Int,
    decode: (Resources, Int) -> ImageBitmap = { resources, id -> ImageBitmap.imageResource(resources, id) }
): Pair<ResourceArt, () -> Unit> {
    val configuration = Configuration(LocalConfiguration.current)
    val resources = LocalContext.current.resources
    val density = resources.displayMetrics.densityDpi
    val keys = ids.toList()
    var retry by remember(configuration, density, resources, keys) { mutableIntStateOf(0) }
    val state = remember(configuration, density, resources, keys, retry) { mutableStateOf(ResourceArt()) }
    val snapshot = remember(configuration, density, resources) {
        @Suppress("DEPRECATION")
        Resources(resources.assets, android.util.DisplayMetrics().apply { setTo(resources.displayMetrics) }, configuration)
    }
    LaunchedEffect(state) {
        try {
            val images = withContext(Dispatchers.IO) { keys.map { decode(snapshot, it) } }
            state.value = ResourceArt(images)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { state.value = ResourceArt(failed = true) }
    }
    return state.value to { retry++ }
}
