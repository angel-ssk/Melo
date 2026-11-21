/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Rhythm fork: panel refresh-rate detection.
 */

package chromahub.rhythm.app.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt

/**
 * Observes the real refresh rate of the display the app is on.
 *
 * Uses [Display.getRefreshRate] instead of hardcoding 60/120 Hz, and listens
 * for display changes so foldables / external monitors / variable-refresh
 * panels are handled correctly. Falls back to 60 Hz if the value is invalid.
 */
@Composable
fun rememberDisplayRefreshRate(): Float {
    val context = LocalContext.current
    val view = LocalView.current
    val refreshRate = remember { mutableFloatStateOf(readRefreshRate(context, view)) }

    DisposableEffect(view) {
        val manager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = update()
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) = update()
            private fun update() {
                refreshRate.floatValue = readRefreshRate(context, view)
            }
        }
        manager?.registerDisplayListener(listener, null)
        onDispose { manager?.unregisterDisplayListener(listener) }
    }

    return refreshRate.floatValue
}

private fun readRefreshRate(context: Context, view: View): Float {
    val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.display
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
    }
    val hz = display?.refreshRate ?: 60f
    return if (hz in 30f..360f) hz else 60f
}

fun formatRefreshRate(hz: Float): String = "${hz.roundToInt()} Hz"

/**
 *devices with 90/120/144 Hz panels get a modest speed boost so motion feels
 *snappier instead of simply rendering more frames of the same long animation.
 */
fun adaptiveMotionFactor(refreshHz: Float): Float =
    1f + ((refreshHz - 60f) / 60f).coerceIn(0f, 1f) * 0.6f
