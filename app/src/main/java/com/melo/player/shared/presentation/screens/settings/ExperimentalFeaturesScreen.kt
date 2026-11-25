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


@Composable
fun ExperimentalFeaturesScreen(
    onBackClick: () -> Unit,
    onNavigateTo: (String) -> Unit = {},
    onNavigateToGoSettings: (() -> Unit)? = null
) {
    LabsSettingsScreen(
        onBackClick = onBackClick,
        onNavigateTo = onNavigateTo,
        onNavigateToGoSettings = onNavigateToGoSettings
    )
}

@Composable
fun LabsSettingsScreen(
    onBackClick: () -> Unit,
    onNavigateTo: (String) -> Unit = {},
    onNavigateToGoSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val appMode by appSettings.appMode.collectAsState()
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
    val enableAlbumEditing by appSettings.enableAlbumEditing.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    val forcePlayerCompactMode by appSettings.forcePlayerCompactMode.collectAsState()
    
    val updaterViewModel: AppUpdaterViewModel = viewModel()
    val latestVersion by updaterViewModel.latestVersion.collectAsState()

    var showRestartDialog by remember { mutableStateOf(false) }
    var restartDialogMessage by remember { mutableStateOf("") }

    val audioRoutingMode by appSettings.audioRoutingMode.collectAsState()
    var showAudioRoutingDialog by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = context.getString(R.string.settings_labs),
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        val settingGroups = buildList {
            add(
                SettingGroup(
                    title = context.getString(R.string.settings_metadata_editing),
                    items = listOf(
                        SettingItem(
                            MaterialSymbolIcon("edit"),
                            context.getString(R.string.settings_enable_album_editing),
                            context.getString(R.string.settings_enable_album_editing_desc),
                            toggleState = enableAlbumEditing,
                            onToggleChange = { appSettings.setEnableAlbumEditing(it) }
                        )
                    )
                )
            )
            
            add(
                SettingGroup(
                    title = context.getString(R.string.settings_viper_group),
                    items = listOf(
                        SettingItem(
                            icon = MaterialSymbolIcon("developer_board", filled = true),
                            title = context.getString(R.string.settings_viper_title),
                            description = context.getString(R.string.settings_viper_desc),
                            onClick = { onNavigateTo(SettingsRoutes.VIPER) }
                        )
                    )
                )
            )

            // Developer/Debugging features group
            add(
                SettingGroup(
                    title = context.getString(R.string.exp_developer_debugging),
                    items = listOf(
                        SettingItem(
                            icon = MeloIcons.Devices.Usb,
                            title = context.getString(R.string.settings_audio_routing_mode),
                            description = when (audioRoutingMode) {
                                "app" -> context.getString(R.string.audio_routing_app)
                                "system" -> context.getString(R.string.audio_routing_system)
                                else -> context.getString(R.string.audio_routing_default)
                            },
                            onClick = { showAudioRoutingDialog = true }
                        ),
                        SettingItem(
                            MaterialSymbolIcon("running_with_errors"),
                            context.getString(R.string.exp_track_error_checker),
                            context.getString(R.string.exp_track_error_checker_desc),
                            toggleState = appSettings.trackErrorCheckerEnabled.collectAsState().value,
                            onToggleChange = { appSettings.setTrackErrorCheckerEnabled(it) }
                        ),
                        SettingItem(
                            MeloIcons.Code,
                            context.getString(R.string.exp_codec_monitoring),
                            context.getString(R.string.exp_codec_monitoring_desc),
                            toggleState = appSettings.codecMonitoringEnabled.collectAsState().value,
                            onToggleChange = { appSettings.setCodecMonitoringEnabled(it) }
                        ),
                        SettingItem(
                            MeloIcons.Headphones,
                            context.getString(R.string.exp_audio_device_logging),
                            context.getString(R.string.exp_audio_device_logging_desc),
                            toggleState = appSettings.audioDeviceLoggingEnabled.collectAsState().value,
                            onToggleChange = { appSettings.setAudioDeviceLoggingEnabled(it) }
                        ),
                        SettingItem(
                            MeloIcons.BugReport,
                            context.getString(R.string.exp_test_crash),
                            context.getString(R.string.exp_test_crash_desc),
                            onClick = { com.melo.player.util.CrashReporter.testCrash() }
                        ),
                        SettingItem(
                            MaterialSymbolIcon("smartphone"),
                            context.getString(R.string.exp_force_player_compact_mode),
                            context.getString(R.string.exp_force_player_compact_mode_desc),
                            toggleState = forcePlayerCompactMode,
                            onToggleChange = { appSettings.setForcePlayerCompactMode(it) }
                        ),
                        SettingItem(
                            MaterialSymbolIcon("cloud_queue"),
                            context.getString(R.string.exp_go_mode),
                            context.getString(R.string.exp_go_mode_desc),
                            toggleState = appMode == "STREAMING",
                            onToggleChange = { enabled ->
                                appSettings.setAppMode(if (enabled) "STREAMING" else "LOCAL")
                            },
                            onClick = { onNavigateToGoSettings?.invoke() }
                        )
                    )
                )
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp + LocalMiniPlayerPadding.current.calculateBottomPadding()),
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups, key = { "setting_${it.title}_${settingGroups.indexOf(it)}" }) { group ->
                Spacer(modifier = Modifier.height(24.dp))

                val materialItems = group.items.map { item ->
                    toMaterial3SettingsItem(context = context, item = item)
                }

                Material3SettingsGroup(
                    title = group.title,
                    items = materialItems,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            }




            
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                                text = context.getString(R.string.updates_experimental_coming),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showRestartDialog) {
        AppRestartDialog(
            onDismiss = { showRestartDialog = false },
            onRestart = {
                showRestartDialog = false
                com.melo.player.util.AppRestarter.restartApp(context)
            },
            onContinue = {
                showRestartDialog = false
            },
            message = restartDialogMessage
        )
    }

    if (showAudioRoutingDialog) {
        AudioRoutingDialog(
            onDismiss = { showAudioRoutingDialog = false },
            appSettings = appSettings,
            context = context,
            haptic = haptic
        )
    }
}



fun getUpdateSourceLabel(context: Context, source: String): String {
    return when (source.lowercase()) {
        "github" -> context.getString(R.string.updates_source_github_label)
        "fdroid" -> context.getString(R.string.updates_source_fdroid_label)
        else -> context.getString(R.string.updates_source_installed_label)
    }
}



/**
 * Toggle card for individual decoration elements
 */
@Composable
fun DecorationToggleCard(
    title: String,
    description: String,
    icon: MaterialSymbolIcon,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with background
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isEnabled)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Toggle switch
            TunerAnimatedSwitch(
                checked = isEnabled,
                onCheckedChange = {
                    onToggle(it)
                }
            )
        }
    }
}



fun getFestivalDisplayName(festivalType: String): String {
    return when (festivalType) {
        "CHRISTMAS" -> "Christmas"
        "NEW_YEAR" -> "New Year"
        "NONE" -> "None"
        "CUSTOM" -> "Custom"
        else -> "Not selected"
    }
}