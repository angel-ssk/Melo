/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.features.local.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.core.net.toUri

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artworkUri: String?,
    val numberOfAlbums: Int,
    val numberOfTracks: Int,
    val groupByAlbumArtist: Boolean
)

fun ArtistEntity.toArtist(): com.melo.player.shared.data.model.Artist {
    return com.melo.player.shared.data.model.Artist(
        id = id,
        name = name,
        artworkUri = artworkUri?.let { (it).toUri() },
        numberOfAlbums = numberOfAlbums,
        numberOfTracks = numberOfTracks
    )
}
