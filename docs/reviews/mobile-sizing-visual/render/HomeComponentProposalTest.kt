package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Review-only proposal. Not connected to app routes or a production account. */
@Composable
private fun ComponentHomeProposal(onPlay: () -> Unit) {
    val font2 = LocalDensity.current.fontScale > 1f
    Scaffold(containerColor = GameTokens.Background,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(GameTokens.Gap), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik",
                    Modifier.weight(1f).height(GameTokens.Target), contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
                Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Image(painterResource(R.drawable.ui_coin), null, Modifier.size(GameTokens.Inset))
                        Text("246", color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }, bottomBar = { ComponentDock(onPlay) }) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).padding(horizontal = GameTokens.Gap)
            .then(if (font2) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            Text("Öz şəhərini qur", color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Title,
                fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = GameTokens.Gap))
            if (!font2) BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                val city = ImageBitmap.imageResource(R.drawable.home_city_cutout_v3)
                val painter = remember(city) { BitmapPainter(city, IntOffset(GameTokens.MobileHome.HeroLeft, 0),
                    IntSize(GameTokens.MobileHome.HeroWidth, city.height)) }
                val height = minOf(maxHeight, GameTokens.MobileHome.HeroMax)
                Image(painter, null, Modifier.height(height).widthIn(max = maxWidth), contentScale = ContentScale.Fit)
            }
            Column(Modifier.fillMaxWidth().padding(bottom = GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (font2) {
                    ComponentStat(R.drawable.ui_level, "Səviyyə 3")
                    ComponentStat(R.drawable.ui_crown, "Rekord 40")
                } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ComponentStat(R.drawable.ui_level, "Səviyyə 3")
                    ComponentStat(R.drawable.ui_crown, "Rekord 40")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Növbəti səviyyə", color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Caption)
                    Text("96 / 150 XP", color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(progress = { 96f / 150f }, color = GameTokens.Accent, trackColor = GameTokens.Surface,
                    drawStopIndicator = {}, modifier = Modifier.fillMaxWidth().height(GameTokens.Small))
            }
        }
    }
}

@Composable
private fun ComponentStat(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Image(painterResource(icon), null, Modifier.size(GameTokens.Inset))
        Text(text, fontFamily = GameTypeface, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ComponentDock(onPlay: () -> Unit) {
    val labels = LocalDensity.current.fontScale <= 1f
    val items = listOf("Ana" to R.drawable.ui_home, "Şəhər" to R.drawable.ui_city, "İnventar" to R.drawable.ui_inventory,
        "Oyna" to 0, "Bazar" to R.drawable.ui_market, "Uğurlar" to R.drawable.ui_trophy, "Profil" to R.drawable.ui_account)
    BoxWithConstraints(Modifier.fillMaxWidth().background(GameTokens.Surface)) {
        if (maxWidth >= GameTokens.Target * items.size) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.MobileHome.NavbarHeight), verticalAlignment = Alignment.CenterVertically) {
                items.forEachIndexed { index, (title, icon) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        if (index == 3) FilledIconButton(onClick = onPlay,
                            modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Oyunu başlat" },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = GameTokens.Accent, contentColor = GameTokens.Background)) {
                            Text("▶", fontSize = GameTokens.Body)
                        } else IconButton(onClick = {}, modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = title },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = if (index == 0) GameTokens.Raised else GameTokens.Surface)) {
                            Image(painterResource(icon), null, Modifier.size(GameTokens.Inset))
                        }
                        if (labels) Text(title, fontFamily = GameTypeface, fontSize = GameTokens.Caption, softWrap = false,
                            color = if (index == 0) GameTokens.Accent else GameTokens.Muted)
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(vertical = GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                @Composable fun side(entries: List<Pair<String, Int>>, modifier: Modifier) {
                    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                        entries.forEach { (title, icon) -> IconButton(onClick = {}, modifier = Modifier.size(GameTokens.Target)) {
                            Image(painterResource(icon), title, Modifier.size(GameTokens.Inset))
                        } }
                    }
                }
                side(items.take(3), Modifier.weight(1f))
                FilledIconButton(onClick = onPlay, modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Oyunu başlat" },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = GameTokens.Accent, contentColor = GameTokens.Background)) { Text("▶") }
                side(items.takeLast(3), Modifier.weight(1f))
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeComponentProposalTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun render(font: Float, name: String) {
        var plays = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, font)) {
                MaterialTheme { ComponentHomeProposal { plays++ } }
            }
        }
        compose.waitForIdle()
        compose.runOnUiThread {
            fun invalidate(v: android.view.View) {
                v.invalidate()
                if (v.javaClass.name == "androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
                if (v is android.view.ViewGroup) for (i in 0 until v.childCount) invalidate(v.getChildAt(i))
            }
            invalidate(compose.activity.window.decorView)
        }
        compose.mainClock.advanceTimeByFrame(); compose.waitForIdle()
        compose.runOnUiThread {
            val v = compose.activity.window.decorView
            val warm = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            v.draw(Canvas(warm)); warm.recycle(); v.invalidate()
        }
        compose.mainClock.advanceTimeByFrame(); compose.waitForIdle()
        compose.runOnUiThread {
            val v = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            var result = -1
            android.view.PixelCopy.request(compose.activity.window, bitmap, { result = it }, android.os.Handler(android.os.Looper.getMainLooper()))
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            assertEquals(android.view.PixelCopy.SUCCESS, result)
            val out = File("/Users/mac/My Workspace/Wobbrik/docs/reviews/home-component-proposal").apply { mkdirs() }
            File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed().performClick()
        assertEquals(1, plays)
    }
    @Test fun normal390() = render(1f, "home-390")
    @Test @Config(qualifiers="w360dp-h640dp-xxhdpi") fun compact360() = render(1f, "home-360")
}
