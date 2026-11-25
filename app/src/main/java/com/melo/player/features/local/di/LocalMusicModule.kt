/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.features.local.di

import android.content.Context
import com.melo.player.core.domain.repository.MusicRepository as MusicRepositoryInterface
import com.melo.player.features.local.data.repository.MusicRepository

/**
 * Dependency injection module for local music feature.
 * Provides instances for local music library functionality.
 * 
 * This uses manual DI for now. Can be converted to Hilt/Dagger later.
 */
object LocalMusicModule {
    
    /**
     * Singleton instance of local MusicRepository.
     */
    @Volatile
    private var musicRepository: MusicRepository? = null
    
    /**
     * Provides MusicRepository instance for local music.
     * Returns the concrete implementation from data layer.
     */
    fun provideMusicRepository(context: Context): MusicRepository {
        return musicRepository ?: synchronized(this) {
            musicRepository ?: MusicRepository(context).also {
                musicRepository = it
            }
        }
    }
}
