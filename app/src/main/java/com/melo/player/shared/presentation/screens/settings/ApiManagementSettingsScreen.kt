/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.melo.player.shared.presentation.screens.settings




import com.melo.player.ui.LocalMiniPlayerPadding
import androidx.compose.foundation.layout.PaddingValues
import com.melo.player.shared.presentation.components.icons.MeloIcons
import com.melo.player.shared.presentation.components.icons.MaterialSymbolIcon
import com.melo.player.shared.presentation.components.icons.Icon

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import com.melo.player.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Slider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melo.player.BuildConfig
import com.melo.player.shared.data.model.AppSettings
import com.melo.player.shared.data.model.Playlist
import com.melo.player.shared.data.model.Song
import com.melo.player.shared.data.repository.PlaybackStatsRepository
import com.melo.player.shared.data.repository.StatsTimeRange
import com.melo.player.util.GsonUtils
import com.melo.player.util.HapticUtils
import com.melo.player.util.HapticType
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlin.system.exitProcess
import com.melo.player.shared.presentation.components.common.CollapsibleHeaderScreen
import com.melo.player.shared.presentation.components.common.ButtonGroupStyle
import com.melo.player.shared.presentation.components.common.ExpressiveScrollBar
import com.melo.player.shared.presentation.components.common.ExpressiveButtonGroup
import com.melo.player.shared.presentation.components.common.ExpressiveGroupButton
import com.melo.player.shared.presentation.components.bottomsheets.StandardBottomSheetHeader
import com.melo.player.shared.presentation.components.common.StyledProgressBar
import com.melo.player.shared.presentation.components.common.ProgressStyle
import com.melo.player.shared.presentation.components.common.ThumbStyle
import com.melo.player.shared.presentation.components.bottomsheets.LicensesBottomSheet
import com.melo.player.shared.presentation.components.bottomsheets.UpdateBottomSheet
import com.melo.player.ui.utils.LazyListStateSaver
import com.melo.player.features.local.presentation.viewmodel.MusicViewModel
import com.melo.player.shared.presentation.components.common.ExpressiveShapeProvider
import com.melo.player.shared.presentation.components.common.ExpressiveShapes
import com.melo.player.shared.presentation.components.common.buildSplashBackdropShapes
import com.melo.player.shared.presentation.components.common.SplashBackgroundOrbs
import com.melo.player.shared.presentation.viewmodel.AppUpdaterViewModel
import com.melo.player.shared.presentation.viewmodel.AppVersion
import com.melo.player.ui.theme.getFontPreviewStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import com.melo.player.utils.FontLoader
import com.melo.player.ui.theme.parseCustomColorScheme
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.text.HtmlCompat
import com.melo.player.shared.presentation.components.common.M3FourColorCircularLoader
import com.melo.player.shared.presentation.components.player.PlayingEqIcon
import com.melo.player.shared.presentation.components.dialogs.CreatePlaylistDialog
import com.melo.player.shared.presentation.components.dialogs.BulkPlaylistExportDialog
import com.melo.player.shared.presentation.components.dialogs.PlaylistImportDialog
import com.melo.player.shared.presentation.components.common.rememberExpressiveShape
import com.melo.player.shared.presentation.components.dialogs.PlaylistOperationProgressDialog
import com.melo.player.shared.presentation.components.dialogs.PlaylistOperationResultDialog
import com.melo.player.shared.presentation.components.dialogs.AppRestartDialog
import com.melo.player.shared.presentation.components.player.PlayerChipOrderBottomSheet
import com.melo.player.features.local.presentation.components.settings.HomeSectionOrderBottomSheet
import com.melo.player.features.local.presentation.components.settings.LibraryTabOrderBottomSheet
import com.melo.player.shared.presentation.components.Material3SettingsGroup
import com.melo.player.shared.presentation.components.Material3SettingsItem

import com.melo.player.shared.presentation.screens.settings.TunerSettingRow
import com.melo.player.shared.presentation.screens.settings.TunerAnimatedSwitch
import com.melo.player.shared.presentation.screens.settings.TunerSettingCard
import com.melo.player.shared.presentation.screens.settings.SettingItem
import com.melo.player.shared.presentation.screens.settings.SettingGroup


// API Management Screen
@Composable
fun ApiManagementSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)

    // API states
    val integrationsEnabled by appSettings.integrationsEnabled.collectAsState()
    val deezerApiEnabled by appSettings.deezerApiEnabled.collectAsState()
    val onlineArtistArtworkEnabled by appSettings.onlineArtistArtwork.collectAsState()
    val lrclibApiEnabled by appSettings.lrclibApiEnabled.collectAsState()
    val betterLyricsApiEnabled by appSettings.betterLyricsApiEnabled.collectAsState()
    val ytMusicApiEnabled by appSettings.ytMusicApiEnabled.collectAsState()
    val lyricallyApiEnabled by appSettings.lyricallyApiEnabled.collectAsState()
    val wikipediaApiEnabled by appSettings.wikipediaApiEnabled.collectAsState()
    val appleCanvasEnabled by appSettings.appleCanvasEnabled.collectAsState()
    val appleCanvasNetworkMode by appSettings.appleCanvasNetworkMode.collectAsState()
    var showCanvasNetworkModeDialog by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = context.getString(R.string.settings_api_management),
        showBackButton = true,
        onBackClick = {
            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticType.HEAVY)
            onBackClick()
        },
        headerContent = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (integrationsEnabled)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = MaterialSymbolIcon("hub"),
                        contentDescription = null,
                        tint = if (integrationsEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(35.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(if (integrationsEnabled) R.string.status_active else R.string.status_disabled),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TunerAnimatedSwitch(
                        checked = integrationsEnabled,
                        onCheckedChange = { enabled ->
                            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticType.HEAVY)
                            appSettings.setIntegrationsEnabled(enabled)
                        }
                    )
                }
            }
        }
    ) { modifier ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp + LocalMiniPlayerPadding.current.calculateBottomPadding()),
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }


            // API Services
            item {
                Text(
                    text = context.getString(R.string.external_services),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                val apiServiceItems = buildList {
                    add(
                        toMaterial3SettingsItem(
                            context = context,
                            hapticFeedback = hapticFeedback,
                            item = SettingItem(
                                icon = null,
                                title = stringResource(R.string.settings_online_artist_artwork),
                                description = context.getString(R.string.settings_online_artist_artwork_desc),
                                enabled = integrationsEnabled,
                                toggleState = onlineArtistArtworkEnabled,
                                onToggleChange = { enabled -> appSettings.setOnlineArtistArtworkEnabled(enabled) }
                            )
                        )
                    )

                    if (com.melo.player.BuildConfig.ENABLE_DEEZER) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = null,
                                    title = stringResource(R.string.onboarding_integration_deezer),
                                    description = context.getString(R.string.api_deezer_desc),
                                    enabled = integrationsEnabled,
                                    toggleState = deezerApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setDeezerApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.melo.player.BuildConfig.ENABLE_LRCLIB) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = null,
                                    title = stringResource(R.string.onboarding_integration_lrclib),
                                    description = context.getString(R.string.api_lrclib_desc),
                                    enabled = integrationsEnabled,
                                    toggleState = lrclibApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setLrcLibApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.melo.player.BuildConfig.ENABLE_BETTERLYRICS) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = null,
                                    title = stringResource(R.string.onboarding_integration_betterlyrics),
                                    description = context.getString(R.string.api_betterlyrics_desc),
                                    enabled = integrationsEnabled,
                                    toggleState = betterLyricsApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setBetterLyricsApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.melo.player.BuildConfig.ENABLE_LYRICALLY_API) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = null,
                                    title = stringResource(R.string.apimanagementsettingsscreen_lyrically),
                                    description = context.getString(R.string.api_lyrically_desc),
                                    enabled = integrationsEnabled,
                                    toggleState = lyricallyApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setLyricallyApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    add(
                        toMaterial3SettingsItem(
                            context = context,
                            hapticFeedback = hapticFeedback,
                            item = SettingItem(
                                icon = null,
                                title = context.getString(R.string.api_apple_motion_canvas),
                                description = context.getString(
                                    R.string.api_apple_motion_canvas_desc,
                                    when (appleCanvasNetworkMode) {
                                        com.melo.player.shared.data.model.CanvasNetworkMode.WIFI_ONLY -> context.getString(R.string.api_apple_canvas_wifi)
                                        com.melo.player.shared.data.model.CanvasNetworkMode.BOTH -> context.getString(R.string.api_apple_canvas_both)
                                    }
                                ),
                                enabled = integrationsEnabled,
                                toggleState = appleCanvasEnabled,
                                onToggleChange = { enabled -> appSettings.setAppleCanvasEnabled(enabled) },
                                onClick = {
                                    if (appleCanvasEnabled) {
                                        HapticUtils.performHapticFeedback(context, hapticFeedback, HapticType.LIGHT)
                                        showCanvasNetworkModeDialog = true
                                    }
                                }
                            )
                        )
                    )

                    if (com.melo.player.BuildConfig.ENABLE_YOUTUBE_MUSIC) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = null,
                                    title = stringResource(R.string.onboarding_integration_ytmusic),
                                    description = context.getString(R.string.api_ytmusic_desc),
                                    enabled = integrationsEnabled,
                                    toggleState = ytMusicApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setYTMusicApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.melo.player.BuildConfig.ENABLE_WIKIPEDIA) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = null,
                                    title = stringResource(R.string.onboarding_integration_wikipedia),
                                    description = stringResource(R.string.onboarding_integration_wikipedia_desc),
                                    enabled = integrationsEnabled,
                                    toggleState = wikipediaApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setWikipediaApiEnabled(enabled) }
                                )
                            )
                        )
                    }


                    add(
                        Material3SettingsItem(
                            icon = null,
                            title = { Text(stringResource(R.string.apimanagementsettingsscreen_github)) },
                            description = { Text(stringResource(R.string.apimanagementsettingsscreen_app_updates_and_release)) }
                        )
                    )
                }

                Material3SettingsGroup(
                    items = apiServiceItems,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = MaterialSymbolIcon("lightbulb", filled = true),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = context.getString(R.string.api_services),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = context.getString(R.string.external_services_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (showCanvasNetworkModeDialog) {
            CanvasNetworkModeDialog(
                onDismiss = { showCanvasNetworkModeDialog = false },
                appSettings = appSettings,
                context = context,
                haptic = hapticFeedback
            )
        }
    }
}