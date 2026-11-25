/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.melo.player.shared.presentation.screens.settings

import com.melo.player.shared.presentation.components.bottomsheets.AdaptiveSheetScrollContainer
import com.melo.player.shared.presentation.components.bottomsheets.MeloAdaptiveModalSheet
import com.melo.player.shared.presentation.components.bottomsheets.SheetAdaptiveType
import com.melo.player.shared.presentation.components.bottomsheets.groupedBottomSheetItemShape



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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import com.melo.player.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSourceDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    selectedFontSource: FontSource,
    onFontSourceSelected: (FontSource) -> Unit,
    appSettings: AppSettings,
    customFontPath: String?,
    context: Context,
    haptic: HapticFeedback,
    onShowRestartDialog: (String) -> Unit
) {
    if (showDialog) {
        val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))

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
                title = context.getString(R.string.theme_font_source),
                subtitle = context.getString(R.string.theme_font_source_desc),
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
                    FontSource.entries.forEachIndexed { index, source ->
                        val isSelected = selectedFontSource == source
                        Card(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticType.LIGHT)
                                onFontSourceSelected(source)
                                when (source) {
                                    FontSource.SYSTEM -> {
                                        appSettings.setFontSource("SYSTEM")
                                        if (customFontPath == null) {
                                            appSettings.setCustomFont("System")
                                        }
                                    }
                                    FontSource.CUSTOM -> {
                                        appSettings.setFontSource("CUSTOM")
                                    }
                                }
                                onDismiss()
                                onShowRestartDialog(context.getString(R.string.theme_font_source_changed_restart))
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = groupedBottomSheetItemShape(index, FontSource.entries.size),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = source.icon,
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
                                        text = source.getDisplayName(context),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = source.getDescription(context),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = MeloIcons.CheckCircle,
                                        contentDescription = stringResource(R.string.streaming_selected),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}