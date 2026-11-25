/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.core.domain.usecase

import com.melo.player.core.domain.model.PlayableItem
import com.melo.player.core.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting all songs from a music repository.
 */
class GetSongsUseCase(private val repository: MusicRepository) {
    operator fun invoke(): Flow<List<PlayableItem>> = repository.getSongs()
}

/**
 * Use case for searching songs.
 */
class SearchSongsUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(query: String): List<PlayableItem> =
        repository.searchSongs(query)
}

/**
 * Use case for getting a song by ID.
 */
class GetSongByIdUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(songId: String): PlayableItem? =
        repository.getSongById(songId)
}
