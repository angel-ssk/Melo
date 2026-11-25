/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.presentation.components.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.melo.player.R
import com.melo.player.shared.presentation.components.icons.MaterialSymbolIcon
import com.melo.player.shared.presentation.components.icons.MeloIcons
import com.melo.player.util.HapticType
import com.melo.player.util.HapticUtils

/**
 * The "Download all" button of a server playlist or album (Go mode). Three states:
 * idle (starts the run), running (shows "done/total" and stops the run on press), and
 * complete (every track is on the device; disabled). [activeDownloadProgress] is
 * (finished, total) for a run that belongs to this screen's item, null otherwise.
 */
@Composable
fun RowScope.DownloadAllActionButton(
    songCount: Int,
    downloadedSongCount: Int,
    activeDownloadProgress: Pair<Int, Int>?,
    onDownloadAll: () -> Unit,
    onCancelDownloadAll: () -> Unit,
    height: Dp = 52.dp,
    isFirst: Boolean = false,
    isLast: Boolean = true,
    compact: Boolean = true
) {
    val state = downloadAllButtonState(
        songCount, downloadedSongCount, activeDownloadProgress, onDownloadAll, onCancelDownloadAll, compact = compact
    )
    MeloDetailActionButton(
        onClick = state.onClick,
        height = height,
        type = state.type,
        enabled = state.enabled,
        isFirst = isFirst,
        isLast = isLast,
        icon = state.icon,
        text = state.text,
        fontWeight = FontWeight.Medium,
        contentDescription = state.contentDescription
    )
}

/** Full-width variant for the tablet layouts, same states as [DownloadAllActionButton]. */
@Composable
fun DownloadAllActionButtonFullWidth(
    songCount: Int,
    downloadedSongCount: Int,
    activeDownloadProgress: Pair<Int, Int>?,
    onDownloadAll: () -> Unit,
    onCancelDownloadAll: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val state = downloadAllButtonState(
        songCount, downloadedSongCount, activeDownloadProgress, onDownloadAll, onCancelDownloadAll, compact = false
    )
    MeloDetailActionButtonFullWidth(
        onClick = state.onClick,
        modifier = modifier,
        height = height,
        type = state.type,
        enabled = state.enabled,
        icon = state.icon,
        iconSize = 24.dp,
        text = state.text,
        fontWeight = FontWeight.Medium,
        contentDescription = state.contentDescription
    )
}

private class DownloadAllButtonState(
    val icon: MaterialSymbolIcon,
    val text: String,
    val contentDescription: String,
    val type: MeloButtonType,
    val enabled: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun downloadAllButtonState(
    songCount: Int,
    downloadedSongCount: Int,
    activeDownloadProgress: Pair<Int, Int>?,
    onDownloadAll: () -> Unit,
    onCancelDownloadAll: () -> Unit,
    compact: Boolean
): DownloadAllButtonState {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val allDownloaded = songCount > 0 && downloadedSongCount >= songCount
    return when {
        activeDownloadProgress != null -> DownloadAllButtonState(
            icon = MeloIcons.CloudDownload,
            text = stringResource(
                R.string.streaming_download_all_progress,
                activeDownloadProgress.first,
                activeDownloadProgress.second
            ),
            contentDescription = stringResource(R.string.content_desc_cancel_download_all),
            type = MeloButtonType.Filled,
            enabled = true,
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticType.LIGHT)
                onCancelDownloadAll()
            }
        )
        allDownloaded -> DownloadAllButtonState(
            icon = MeloIcons.CheckCircle,
            text = stringResource(R.string.streaming_downloaded),
            contentDescription = stringResource(R.string.streaming_downloaded),
            type = MeloButtonType.Tonal,
            enabled = false,
            onClick = {}
        )
        else -> DownloadAllButtonState(
            icon = MeloIcons.Download,
            // The compact row already holds Play and Shuffle, so the short label fits there.
            text = stringResource(if (compact) R.string.streaming_download else R.string.streaming_download_all),
            contentDescription = stringResource(R.string.content_desc_download_all),
            type = MeloButtonType.Tonal,
            enabled = songCount > 0,
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticType.HEAVY)
                onDownloadAll()
            }
        )
    }
}
