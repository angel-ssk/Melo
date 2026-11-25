/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.melo.player.shared.presentation.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.melo.player.R
import com.melo.player.shared.data.model.AppSettings
import com.melo.player.shared.presentation.components.bottomsheets.AdaptiveSheetScrollContainer
import com.melo.player.shared.presentation.components.bottomsheets.MeloAdaptiveModalSheet
import com.melo.player.shared.presentation.components.bottomsheets.SheetAdaptiveType
import com.melo.player.shared.presentation.components.bottomsheets.StandardBottomSheetHeader
import com.melo.player.shared.presentation.components.bottomsheets.groupedBottomSheetItemShape
import com.melo.player.shared.presentation.components.icons.Icon
import com.melo.player.shared.presentation.components.icons.MeloIcons
import com.melo.player.util.HapticType
import com.melo.player.util.HapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoutingDialog(
    onDismiss: () -> Unit,
    appSettings: AppSettings,
    context: Context,
    haptic: HapticFeedback
) {
    val routingMode by appSettings.audioRoutingMode.collectAsState()
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    MeloAdaptiveModalSheet(
        adaptiveType = SheetAdaptiveType.COMPACT_DIALOG,
        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        StandardBottomSheetHeader(
            title = context.getString(R.string.settings_audio_routing_mode),
            subtitle = context.getString(R.string.settings_audio_routing_mode_desc),
            visible = true
        )

        val scrollState = rememberScrollState()

        AdaptiveSheetScrollContainer(
            scrollState = scrollState,
            modifier = Modifier.fillMaxWidth()
        ) { endPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, end = 24.dp + endPadding, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val routingOptions = listOf(
                    "default" to Triple(
                        context.getString(R.string.audio_routing_default),
                        context.getString(R.string.audio_routing_default_desc),
                        MeloIcons.Devices.Speaker
                    ),
                    "app" to Triple(
                        context.getString(R.string.audio_routing_app),
                        context.getString(R.string.audio_routing_app_desc),
                        MeloIcons.Devices.Usb
                    ),
                    "system" to Triple(
                        context.getString(R.string.audio_routing_system),
                        context.getString(R.string.audio_routing_system_desc),
                        MeloIcons.Devices.Headphones
                    )
                )

                routingOptions.forEachIndexed { index, (mode, info) ->
                    val (title, description, icon) = info
                    val isSelected = routingMode == mode

                    Card(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticType.LIGHT)
                            appSettings.setAudioRoutingMode(mode)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = groupedBottomSheetItemShape(index, routingOptions.size),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(if (isSelected) 30.dp else 26.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = MeloIcons.CheckCircle,
                                    contentDescription = context.getString(R.string.cd_selected),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
