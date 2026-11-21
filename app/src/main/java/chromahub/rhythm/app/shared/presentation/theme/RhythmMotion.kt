/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Rhythm fork: high-refresh motion system.
 */

package chromahub.rhythm.app.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Motion tokens tuned for 120 Hz+ displays.
 *
 * The fork uses critically-damped, high-stiffness springs instead of long
 * duration tweens. That matters on low-RAM devices too:
 *  - springs are time-based but settle in ~150-250 ms instead of the 400-500 ms
 *    of Material's expressive scheme, so there are fewer animated frames and
 *    less work for the composition thread;
 *  - only alpha / scale / translation are animated and they are applied with
 *    `graphicsLayer`, never with layout-affecting modifiers;
 *  - visibility thresholds are relatively large so the animation stops as
 *    soon as it is visually complete instead of rendering a long sub-pixel tail.
 *
 * [setSpeed] is called from [RhythmTheme] with the user setting and the
 * detected panel refresh rate. All specs below are getters, so changing the
 * speed recomposes and retargets every animation in the app automatically.
 */
object RhythmMotion {

    const val TARGET_REFRESH_HZ = 120

    private val speedState = mutableFloatStateOf(1f)
    private val reducedState = mutableStateOf(false)

    /** Current speed multiplier (written once per composition by the theme). */
    val speed: Float get() = speedState.floatValue

    /** True when the system/user asked for reduced motion. */
    val reducedMotion: Boolean get() = reducedState.value

    fun setReducedMotion(reduced: Boolean) {
        if (reducedState.value != reduced) reducedState.value = reduced
    }

    /** Near-instant spec used when motion is reduced for accessibility or battery. */
    private fun <T> instant(visibilityThreshold: T? = null): SpringSpec<T> =
        spring<T>(dampingRatio = 1f, stiffness = 100_000f, visibilityThreshold = visibilityThreshold)

    fun setSpeed(value: Float) {
        val safe = value.coerceIn(0.6f, 2.5f)
        if (speedState.floatValue != safe) speedState.floatValue = safe
    }

    /** Fast opacity/emphasis spring. */
    val emphasis: SpringSpec<Float>
        get() = if (reducedState.value) instant(0.002f) else spring(
            dampingRatio = 1f,
            stiffness = 900f * speed,
            visibilityThreshold = 0.002f
        )

    /** Spatial spring for size and weight changes. */
    val spatial: SpringSpec<Float>
        get() = if (reducedState.value) instant(0.002f) else spring(
            dampingRatio = 0.96f,
            stiffness = 800f * speed,
            visibilityThreshold = 0.002f
        )

    /** Spring for large surfaces (sheets, hero cards). */
    val surface: SpringSpec<Float>
        get() = if (reducedState.value) instant(0.002f) else spring(
            dampingRatio = 1f,
            stiffness = 650f * speed,
            visibilityThreshold = 0.002f
        )

    /** Slowest spring, used only for the big listening ring. */
    val hero: SpringSpec<Float>
        get() = if (reducedState.value) instant(0.003f) else spring(
            dampingRatio = 1f,
            stiffness = 550f * speed,
            visibilityThreshold = 0.003f
        )

    /** Slide transitions animate IntOffset (pixels), so they need their own spec. */
    val offset: SpringSpec<IntOffset>
        get() = if (reducedState.value) instant(IntOffset(1, 1)) else spring<IntOffset>(
            dampingRatio = 1f,
            stiffness = 800f * speed,
            visibilityThreshold = IntOffset(1, 1)
        )

    val color: SpringSpec<Color>
        get() = if (reducedState.value) instant<Color>() else spring<Color>(
            dampingRatio = 1f,
            stiffness = 900f * speed
        )

    val spatialDp: SpringSpec<Dp>
        get() = if (reducedState.value) instant<Dp>(0.1f.dp) else spring<Dp>(
            dampingRatio = 0.96f,
            stiffness = 800f * speed,
            visibilityThreshold = 0.1f.dp
        )

    /** Complex range swap: fade + scale + slide overlapping on different springs. */
    val rangeContent: ContentTransform
        get() = (fadeIn(animationSpec = emphasis) +
            scaleIn(initialScale = 0.96f, animationSpec = spatial) +
            slideInVertically(initialOffsetY = { it / 32 }, animationSpec = offset)) togetherWith
            (fadeOut(animationSpec = emphasis) +
                scaleOut(targetScale = 0.97f, animationSpec = spatial) +
                slideOutVertically(targetOffsetY = { -(it / 40) }, animationSpec = offset))

    val identityContent: ContentTransform
        get() = (fadeIn(animationSpec = emphasis) +
            slideInHorizontally(initialOffsetX = { it / 16 }, animationSpec = offset)) togetherWith
            (fadeOut(animationSpec = emphasis) +
                slideOutHorizontally(targetOffsetX = { -(it / 24) }, animationSpec = offset))

    val screenEnter: EnterTransition
        get() = fadeIn(animationSpec = emphasis) +
            slideInVertically(initialOffsetY = { it / 12 }, animationSpec = offset)

    val screenExit: ExitTransition
        get() = fadeOut(animationSpec = emphasis)

    val screenPopExit: ExitTransition
        get() = fadeOut(animationSpec = emphasis) +
            slideOutVertically(targetOffsetY = { it / 12 }, animationSpec = offset)

    val cardVisibility: ContentTransform
        get() = (fadeIn(animationSpec = emphasis) +
            scaleIn(initialScale = 0.95f, animationSpec = spatial)) togetherWith
            (fadeOut(animationSpec = emphasis) +
                scaleOut(targetScale = 0.97f, animationSpec = spatial))

    fun <T> springSpec(
        dampingRatio: Float = 0.96f,
        stiffness: Float = 800f,
        visibilityThreshold: T? = null
    ): FiniteAnimationSpec<T> = spring<T>(
        dampingRatio = dampingRatio,
        stiffness = stiffness * speed,
        visibilityThreshold = visibilityThreshold
    )
}

/**
 * Replaces Material 3's expressive scheme (hundreds of milliseconds per
 * interaction) with short, critical springs. This speeds up every Material
 * component — switches, navigation items, sheets, ripples — not only our own
 * animations, which is the main cause of the "slow even with ultra smooth on"
 * feeling on devices with little free RAM.
 */
class RhythmMotionScheme(private val speed: Float) : MotionScheme {
    private fun <T> spec(stiffness: Float, damping: Float): FiniteAnimationSpec<T> =
        spring<T>(dampingRatio = damping, stiffness = stiffness * speed)

    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = spec(720f, 1f)
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = spec(1000f, 1f)
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = spec(500f, 1f)
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = spec(900f, 1f)
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = spec(1200f, 1f)
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = spec(600f, 1f)
}

/** Material motion used when the user asked for reduced motion. */
object ReducedMotionScheme : MotionScheme {
    private fun <T> instant(): FiniteAnimationSpec<T> =
        spring<T>(dampingRatio = 1f, stiffness = 100_000f)

    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = instant()
}
