/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.presentation.components.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Arrow position relative to the tooltip body.
 */
enum class MeloTooltipArrowPosition {
    TopStart, TopCenter, TopEnd,
    BottomStart, BottomCenter, BottomEnd,
    StartCenter, EndCenter
}

/**
 * A custom-styled tooltip with an arrow pointer that can be positioned
 * on any side of the tooltip body.
 *
 * @param text The tooltip text content.
 * @param arrowPosition Where the arrow should point from.
 * @param containerColor Background colour of the tooltip.
 * @param contentColor Text colour inside the tooltip.
 */
@Composable
fun MeloTooltip(
    text: String,
    modifier: Modifier = Modifier,
    arrowPosition: MeloTooltipArrowPosition = MeloTooltipArrowPosition.BottomCenter,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val arrowWidth = 16.dp
    val arrowHeight = 8.dp

    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (arrowPosition == MeloTooltipArrowPosition.StartCenter) {
            TooltipArrowSide(
                color = containerColor,
                width = arrowHeight,
                height = arrowWidth,
                direction = TooltipArrowDirection.Left
            )
        }

        Column(
            horizontalAlignment = when (arrowPosition) {
                MeloTooltipArrowPosition.TopStart,
                MeloTooltipArrowPosition.BottomStart -> Alignment.Start
                MeloTooltipArrowPosition.TopCenter,
                MeloTooltipArrowPosition.BottomCenter -> Alignment.CenterHorizontally
                MeloTooltipArrowPosition.TopEnd,
                MeloTooltipArrowPosition.BottomEnd -> Alignment.End
                else -> Alignment.CenterHorizontally
            }
        ) {
            if (arrowPosition == MeloTooltipArrowPosition.TopStart ||
                arrowPosition == MeloTooltipArrowPosition.TopCenter ||
                arrowPosition == MeloTooltipArrowPosition.TopEnd
            ) {
                TooltipArrow(
                    color = containerColor,
                    width = arrowWidth,
                    height = arrowHeight,
                    isUpward = true,
                    modifier = Modifier.padding(
                        start = if (arrowPosition == MeloTooltipArrowPosition.TopStart) 16.dp else 0.dp,
                        end = if (arrowPosition == MeloTooltipArrowPosition.TopEnd) 16.dp else 0.dp
                    )
                )
            }

            Surface(
                color = containerColor,
                contentColor = contentColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (arrowPosition == MeloTooltipArrowPosition.BottomStart ||
                arrowPosition == MeloTooltipArrowPosition.BottomCenter ||
                arrowPosition == MeloTooltipArrowPosition.BottomEnd
            ) {
                TooltipArrow(
                    color = containerColor,
                    width = arrowWidth,
                    height = arrowHeight,
                    isUpward = false,
                    modifier = Modifier.padding(
                        start = if (arrowPosition == MeloTooltipArrowPosition.BottomStart) 16.dp else 0.dp,
                        end = if (arrowPosition == MeloTooltipArrowPosition.BottomEnd) 16.dp else 0.dp
                    )
                )
            }
        }

        if (arrowPosition == MeloTooltipArrowPosition.EndCenter) {
            TooltipArrowSide(
                color = containerColor,
                width = arrowHeight,
                height = arrowWidth,
                direction = TooltipArrowDirection.Right
            )
        }
    }
}

private enum class TooltipArrowDirection { Left, Right }

@Composable
private fun TooltipArrowSide(
    color: Color,
    width: Dp,
    height: Dp,
    direction: TooltipArrowDirection,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(width, height)
            .offset(x = if (direction == TooltipArrowDirection.Left) 1.dp else (-1).dp)
    ) {
        val path = Path().apply {
            if (direction == TooltipArrowDirection.Left) {
                moveTo(size.width, 0f)
                lineTo(0f, size.height / 2f)
                lineTo(size.width, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
            }
            close()
        }
        drawPath(path, color = color)
    }
}

@Composable
private fun TooltipArrow(
    color: Color,
    width: Dp,
    height: Dp,
    isUpward: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(width, height)
            .offset(y = if (isUpward) 1.dp else (-1).dp)
    ) {
        val path = Path().apply {
            if (isUpward) {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path, color = color)
    }
}

/**
 * A convenient wrapper that uses M3's [TooltipBox] with [MeloTooltip].
 *
 * @param tooltipText The tooltip text.
 * @param state Tooltip state controlling visibility.
 * @param arrowPosition Where to place the arrow.
 * @param content The anchor composable that triggers the tooltip on hover/long-press.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeloTooltipBox(
    tooltipText: String,
    state: TooltipState,
    modifier: Modifier = Modifier,
    arrowPosition: MeloTooltipArrowPosition = MeloTooltipArrowPosition.BottomCenter,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = when (arrowPosition) {
                MeloTooltipArrowPosition.StartCenter -> TooltipAnchorPosition.Right
                MeloTooltipArrowPosition.EndCenter -> TooltipAnchorPosition.Left
                MeloTooltipArrowPosition.TopStart,
                MeloTooltipArrowPosition.TopCenter,
                MeloTooltipArrowPosition.TopEnd -> TooltipAnchorPosition.Below
                else -> TooltipAnchorPosition.Above
            },
            spacingBetweenTooltipAndAnchor = 4.dp
        ),
        tooltip = {
            MeloTooltip(
                text = tooltipText,
                arrowPosition = arrowPosition
            )
        },
        state = state,
        modifier = modifier,
        content = content
    )
}
