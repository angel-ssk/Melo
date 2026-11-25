/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.presentation.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.melo.player.shared.presentation.components.icons.MaterialSymbolIcon
import com.melo.player.shared.presentation.components.icons.MeloIcons
import com.melo.player.shared.data.model.Song
import com.melo.player.R

private data class SongMenuItem(
    val title: String,
    val icon: Any,
    val iconBgColor: Color,
    val iconTint: Color,
    val onClick: () -> Unit
)

@Composable
fun MeloSongMenuContent(
    modifier: Modifier = Modifier,
    song: Song? = null,
    onPlay: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    isLiked: Boolean? = null,
    onToggleLike: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onShowSongInfo: (() -> Unit)? = null,
    onGoToAlbum: (() -> Unit)? = null,
    onGoToArtist: (() -> Unit)? = null,
    onAddToBlacklist: (() -> Unit)? = null,
    onDeleteSong: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    isDownloaded: Boolean? = null,
    isDownloading: Boolean = false,
    onToggleDownload: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val finalOnShare = onShare ?: song?.let { s ->
        {
            try {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(android.content.Intent.EXTRA_STREAM, s.uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share ${s.title}"))
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, R.string.materialplayerscreen_unable_to_share_file, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    val menuItems = buildList {
        onPlay?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_play),
                    icon = MeloIcons.Play,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = action
                )
            )
        }
        onPlayNext?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_play_next),
                    icon = MeloIcons.SkipNext,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = action
                )
            )
        }
        onAddToQueue?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_add_to_queue),
                    icon = MeloIcons.Queue,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = action
                )
            )
        }
        onToggleFavorite?.let { action ->
            val fav = isFavorite == true
            add(
                SongMenuItem(
                    title = if (fav) context.getString(R.string.action_dislike) else context.getString(R.string.action_like),
                    icon = if (fav) MaterialSymbolIcon("thumb_up", filled = true) else MaterialSymbolIcon("thumb_up", filled = false),
                    iconBgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = action
                )
            )
        }
        onToggleLike?.let { action ->
            val liked = isLiked == true
            add(
                SongMenuItem(
                    title = if (liked) "Unlike" else "Like",
                    icon = if (liked) MaterialSymbolIcon("thumb_up", filled = true) else MaterialSymbolIcon("thumb_up", filled = false),
                    iconBgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = action
                )
            )
        }
        onAddToPlaylist?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.library_action_add_to_playlist),
                    icon = MeloIcons.AddToPlaylist,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = action
                )
            )
        }
        onToggleDownload?.let { action ->
            val downloaded = isDownloaded == true
            add(
                SongMenuItem(
                    title = when {
                        isDownloading -> stringResource(R.string.streaming_downloading)
                        downloaded -> stringResource(R.string.streaming_remove_download)
                        else -> stringResource(R.string.streaming_download)
                    },
                    icon = when {
                        isDownloading -> MaterialSymbolIcon("sync")
                        downloaded -> MaterialSymbolIcon("download_done", filled = true)
                        else -> MaterialSymbolIcon("download")
                    },
                    iconBgColor = if (downloaded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    iconTint = if (downloaded) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = action
                )
            )
        }
        finalOnShare?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_share),
                    icon = MeloIcons.Share,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = action
                )
            )
        }
        onShowSongInfo?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_song_info),
                    icon = MeloIcons.Info,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = action
                )
            )
        }
        onGoToAlbum?.let { action ->
            add(
                SongMenuItem(
                    title = stringResource(R.string.multiselectionbottomsheet_go_to_album),
                    icon = MeloIcons.AlbumFilled,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = action
                )
            )
        }
        onGoToArtist?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.multiselectionbottomsheet_go_to_artist),
                    icon = MeloIcons.ArtistFilled,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = action
                )
            )
        }
        onAddToBlacklist?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_add_to_blacklist),
                    icon = MeloIcons.Block,
                    iconBgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = action
                )
            )
        }
        onDeleteSong?.let { action ->
            add(
                SongMenuItem(
                    title = context.getString(R.string.action_delete_song),
                    icon = MeloIcons.Delete,
                    iconBgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = action
                )
            )
        }
    }

    MeloGroupedMenuContent(
        modifier = modifier,
        items = menuItems.map { item ->
            MeloMenuItem(
                title = item.title,
                icon = item.icon,
                iconContainerColor = item.iconBgColor,
                iconTint = item.iconTint,
                isDestructive = item.icon == MeloIcons.Block || item.icon == MeloIcons.Delete,
                onClick = item.onClick
            )
        }
    )
}
