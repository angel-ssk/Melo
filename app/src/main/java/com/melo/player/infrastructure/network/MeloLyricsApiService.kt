/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service interface for Melo lyrics API
 * API Documentation: https://lyrics.paxsenix.org/docs
 */
interface MeloLyricsApiService {
    /**
     * Get word-by-word synchronized lyrics for a specific song (Apple Music)
     * @param id Lyrics source song ID
     * @return Lyrics response with word-level timing
     */
    @GET("apple-music/lyrics")
    suspend fun getLyrics(
        @Query("id") id: String
    ): MeloLyricsResponse

    @GET("apple-music/lyrics")
    suspend fun getAppleMusicLyrics(
        @Query("id") id: String
    ): MeloLyricsResponse
}
