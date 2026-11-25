/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.features.local.data.database.entity

import androidx.room.Entity

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongEntity(
    val playlistId: String,
    val songId: String,
    val orderIndex: Int
)
