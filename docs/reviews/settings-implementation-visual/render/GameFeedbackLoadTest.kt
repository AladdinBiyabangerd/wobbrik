package com.aladdin.wobbrik

import android.app.Application
import android.content.Context
import android.media.SoundPool
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowSoundPool
import org.robolectric.util.ReflectionHelpers

/** Completes during load to exercise the earliest possible asynchronous completion. */
@Implements(SoundPool::class)
class ImmediatelyLoadedSoundPool : ShadowSoundPool() {
    @Implementation
    override fun load(context: Context, resId: Int, priority: Int): Int {
        val id = super.load(context, resId, priority)
        notifyResourceLoaded(resId, true)
        return id
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, shadows = [ImmediatelyLoadedSoundPool::class])
class GameFeedbackLoadTest {
    @Test fun fastLoadStillPlaysAndRespectsSoundAndForegroundSettings() {
        val context = RuntimeEnvironment.getApplication()
        val output = GameFeedback(context, View(context))
        val pool = ReflectionHelpers.getField<SoundPool>(output, "pool")
        val shadow = Shadows.shadowOf(pool)
        try {
            output.update(DevicePreferences(music = false), true)
            output.click()
            assertEquals(1, shadow.getResourcePlaybacks(R.raw.sfx_ui_click).size)
            output.update(DevicePreferences(music = false, sound = false), true)
            output.click()
            output.update(DevicePreferences(music = false), false)
            output.click()
            assertEquals(1, shadow.getResourcePlaybacks(R.raw.sfx_ui_click).size)
        } finally { output.release() }
    }
}
