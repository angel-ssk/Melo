/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.infrastructure.widget.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * Worker to update Glance widgets
 * This ensures widgets stay synchronized with playback state
 */
class MeloWidgetWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            // Get the current widget state from SharedPreferences
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            
            val songId = prefs.getString(MeloMusicWidget.KEY_SONG_ID, "") ?: ""
            val songTitle = prefs.getString(MeloMusicWidget.KEY_SONG_TITLE, "Melo") ?: "Melo"
            val artistName = prefs.getString(MeloMusicWidget.KEY_ARTIST_NAME, "") ?: ""
            val albumName = prefs.getString(MeloMusicWidget.KEY_ALBUM_NAME, "") ?: ""
            val isPlaying = prefs.getBoolean(MeloMusicWidget.KEY_IS_PLAYING, false)
            val artworkUri = prefs.getString(MeloMusicWidget.KEY_ARTWORK_URI, null)
            val hasPrevious = prefs.getBoolean(MeloMusicWidget.KEY_HAS_PREVIOUS, false)
            val hasNext = prefs.getBoolean(MeloMusicWidget.KEY_HAS_NEXT, false)
            val isFavorite = prefs.getBoolean(MeloMusicWidget.KEY_IS_FAVORITE, false)
            
            // Update all widget instances
            val glanceWidgetManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceWidgetManager.getGlanceIds(MeloMusicWidget::class.java)
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    if (songId.isNotEmpty()) {
                        prefs[stringPreferencesKey(MeloMusicWidget.KEY_SONG_ID)] = songId
                    } else {
                        prefs.remove(stringPreferencesKey(MeloMusicWidget.KEY_SONG_ID))
                    }
                    prefs[stringPreferencesKey(MeloMusicWidget.KEY_SONG_TITLE)] = songTitle
                    prefs[stringPreferencesKey(MeloMusicWidget.KEY_ARTIST_NAME)] = artistName
                    prefs[stringPreferencesKey(MeloMusicWidget.KEY_ALBUM_NAME)] = albumName
                    prefs[booleanPreferencesKey(MeloMusicWidget.KEY_IS_PLAYING)] = isPlaying
                    prefs[booleanPreferencesKey(MeloMusicWidget.KEY_HAS_PREVIOUS)] = hasPrevious
                    prefs[booleanPreferencesKey(MeloMusicWidget.KEY_HAS_NEXT)] = hasNext
                    prefs[booleanPreferencesKey(MeloMusicWidget.KEY_IS_FAVORITE)] = isFavorite
                    if (artworkUri != null) {
                        prefs[stringPreferencesKey(MeloMusicWidget.KEY_ARTWORK_URI)] = artworkUri
                    }
                }
            }
            
            // Trigger widget updates
            MeloMusicWidget().updateAll(context)
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MeloWidgetWorker", "Error updating widget", e)
            Result.failure()
        }
    }
}
