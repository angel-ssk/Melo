/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.presentation.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.melo.player.R
import com.melo.player.MeloApplication
import androidx.compose.ui.unit.dp

/**
 * Melo signature backdrop: soft brand gradient with the M-wave as a watermark.
 * Decorative only — it must not change the way the icon itself looks.
 */
@Composable
fun MeloWaveBackdrop(
    modifier: Modifier = Modifier,
    alpha: Float = 0.10f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        Image(
            painter = painterResource(R.drawable.ic_melo_wave),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.9f)
                .alpha(alpha)
        )
        content()
    }
}

/**
 * Real blurred brand gradient. Blur is skipped on low-RAM devices and when the
 * user switches the setting off, falling back to the flat gradient.
 */
@Composable
fun MeloWaveGradientBackdrop(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val useBlur = enabled && !MeloApplication.isLowRam
    Box(modifier = modifier.fillMaxSize()) {
        if (useBlur) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
                    .alpha(0.55f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.55f)
                        .align(Alignment.TopStart)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.45f)
                        .align(Alignment.BottomEnd)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
        content()
    }
}

/**
 * Loading indicator using the exact Melo logo artwork, animated side to side.
 * The artwork itself is untouched; only its position oscillates.
 */
@Composable
fun MeloWaveLoader(
    modifier: Modifier = Modifier,
    durationMs: Int = 1100
) {
    val transition = rememberInfiniteTransition(label = "meloWaveLoader")
    val offset by transition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "meloWaveOffset"
    )
    Image(
        painter = painterResource(R.drawable.melo_foreground),
        contentDescription = null,
        modifier = modifier.graphicsLayer { translationX = offset }
    )
}
