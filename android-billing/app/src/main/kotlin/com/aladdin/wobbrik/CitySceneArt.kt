package com.aladdin.wobbrik

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class CitySceneArt(val decorations: Map<String, CosmeticSprite>, val terrain: CityTerrainArt,
    val garden: CityGardenArt, val life: Map<String, CosmeticSprite>, val illustration: CityIllustrationArt)
internal data class CitySceneArtState(val assets: CitySceneArt?, val status: ArtPreparation, val retry: () -> Unit)

/** Immutable APK art only, shared across city re-entry. Never holds an Activity or account. */
private object CitySceneAssets {
    @Volatile var loaded: CitySceneArt? = null
        private set
    @Synchronized fun load(context: android.content.Context): CitySceneArt = loaded ?: CitySceneArt(
        loadDecorationArt(context), loadCityTerrainArt(context), loadCityGardenArt(context), loadCityLifeArt(context), loadCityIllustrationArt(context)
    ).also { loaded = it }
}

@Composable
internal fun rememberCitySceneArt(required: Boolean): CitySceneArtState {
    val context = LocalContext.current.applicationContext
    var retry by remember { mutableIntStateOf(0) }
    var assets by remember { mutableStateOf(CitySceneAssets.loaded) }
    var status by remember { mutableStateOf(if(assets == null) ArtPreparation.Loading else ArtPreparation.Ready) }
    LaunchedEffect(required, context, retry) {
        if(!required || assets != null) return@LaunchedEffect
        status = ArtPreparation.Loading
        try {
            assets = withContext(Dispatchers.IO) { CitySceneAssets.load(context) }
            status = ArtPreparation.Ready
        } catch(cancelled: CancellationException) { throw cancelled }
        catch(_: Exception) { status = ArtPreparation.Failed }
    }
    return CitySceneArtState(assets,status) { retry++ }
}
