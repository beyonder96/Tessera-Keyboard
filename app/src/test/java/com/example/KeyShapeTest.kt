package com.example

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.example.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class KeyShapeTest {

    @Test
    fun testKeyShapePreferencesStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)

        val shapes = listOf("squircle", "pill", "borderless")
        for (shape in shapes) {
            prefs.edit().putString("PREF_KEY_SHAPE", shape).commit()
            val retrieved = prefs.getString("PREF_KEY_SHAPE", "squircle")
            assertEquals(shape, retrieved)
        }
    }

    @Test
    fun testKeyShapeDrawablesLoadable() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Ensure all key shape drawables exist and load without crashing
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.bg_key_editorial_flat))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.bg_key_circle))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.bg_key_borderless))

        // Ensure all command shape drawables exist and load without crashing
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.bg_key_action_flat))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.bg_command_pill))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.bg_command_borderless))
    }
}
