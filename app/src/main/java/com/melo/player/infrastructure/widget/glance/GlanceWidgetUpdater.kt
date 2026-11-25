/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.infrastructure.widget.glance

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.melo.player.shared.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.withContext
import com.melo.player.infrastructure.service.MeloTileService
import androidx.core.content.edit
import androidx.core.content.pm.ShortcutManagerCompat

/**
 * Utility object for updating the Glance-based widget
 * 
 * This handles updating widget state when playback changes
 */
object GlanceWidgetUpdater {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    /**
     * Update widget with current playback state
     */
    fun updateWidget(
        context: Context,
        song: Song?,
        isPlaying: Boolean,
        hasPrevious: Boolean = false,
        hasNext: Boolean = false,
        isFavorite: Boolean = false,
        isShuffleEnabled: Boolean = false,
        repeatMode: Int = 0
    ) {
        // Use the application context for every widget/state API so short-lived
        // contexts (e.g. the playback service) are never retained by Glance's
        // process-lifetime state cache.
        val appContext = context.applicationContext

        // Update dynamic launcher shortcuts
        updateAppShortcuts(appContext, isPlaying)

        // Update SharedPreferences shared with the Glance widgets
        val prefs = appContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            if (song != null) {
                putString(MeloMusicWidget.KEY_SONG_ID, song.id)
                putString(MeloMusicWidget.KEY_SONG_TITLE, song.title)
                putString(MeloMusicWidget.KEY_ARTIST_NAME, song.artist)
                putString(MeloMusicWidget.KEY_ALBUM_NAME, song.album)
                putString(MeloMusicWidget.KEY_ARTWORK_URI, song.artworkUri?.toString())
            } else {
                putString(MeloMusicWidget.KEY_SONG_ID, "")
                putString(MeloMusicWidget.KEY_SONG_TITLE, "Melo")
                putString(MeloMusicWidget.KEY_ARTIST_NAME, "")
                putString(MeloMusicWidget.KEY_ALBUM_NAME, "")
                remove(MeloMusicWidget.KEY_ARTWORK_URI)
            }
            putBoolean(MeloMusicWidget.KEY_IS_PLAYING, isPlaying)
            putBoolean(MeloMusicWidget.KEY_HAS_PREVIOUS, hasPrevious)
            putBoolean(MeloMusicWidget.KEY_HAS_NEXT, hasNext)
            putBoolean(MeloMusicWidget.KEY_IS_FAVORITE, isFavorite)
            putBoolean("is_shuffle", isShuffleEnabled)
            putInt("repeat_mode", repeatMode)
        }
        
        // Update Glance widget state directly using Glance state system
        scope.launch {
            try {
                // Preload bitmap in background if artworkUri exists
                val artworkUri = song?.artworkUri?.toString()
                if (!artworkUri.isNullOrBlank()) {
                    try {
                        withContext(Dispatchers.IO) {
                            val imageLoader = coil.Coil.imageLoader(appContext)
                            val request = ImageRequest.Builder(appContext)
                                .data(artworkUri)
                                .size(Size(150, 150))
                                .build()
                            val result = imageLoader.execute(request)
                            val loaded = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                            if (loaded != null) {
                                MeloMusicWidget.cacheBitmap(artworkUri, loaded)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GlanceWidgetUpdater", "Error preloading bitmap in updater", e)
                    }
                }

                val manager = GlanceAppWidgetManager(appContext)
                
                // Helper to update state for any music widget
                val updatePrefsHelper = { mutablePrefs: androidx.datastore.preferences.core.MutablePreferences ->
                    if (song != null) {
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_SONG_ID)] = song.id
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_SONG_TITLE)] = song.title
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_ARTIST_NAME)] = song.artist
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_ALBUM_NAME)] = song.album
                        song.artworkUri?.let {
                            mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_ARTWORK_URI)] = it.toString()
                        }
                    } else {
                        mutablePrefs.remove(stringPreferencesKey(MeloMusicWidget.KEY_SONG_ID))
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_SONG_TITLE)] = "Melo"
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_ARTIST_NAME)] = ""
                        mutablePrefs[stringPreferencesKey(MeloMusicWidget.KEY_ALBUM_NAME)] = ""
                        mutablePrefs.remove(stringPreferencesKey(MeloMusicWidget.KEY_ARTWORK_URI))
                    }
                    mutablePrefs[booleanPreferencesKey(MeloMusicWidget.KEY_IS_PLAYING)] = isPlaying
                    mutablePrefs[booleanPreferencesKey(MeloMusicWidget.KEY_HAS_PREVIOUS)] = hasPrevious
                    mutablePrefs[booleanPreferencesKey(MeloMusicWidget.KEY_HAS_NEXT)] = hasNext
                    mutablePrefs[booleanPreferencesKey(MeloMusicWidget.KEY_IS_FAVORITE)] = isFavorite
                    mutablePrefs[booleanPreferencesKey("is_shuffle")] = isShuffleEnabled
                    mutablePrefs[intPreferencesKey("repeat_mode")] = repeatMode
                }

                // 1. MeloMusicWidget
                manager.getGlanceIds(MeloMusicWidget::class.java).forEach { glanceId ->
                    updateAppWidgetState(appContext, glanceId) { prefs -> updatePrefsHelper(prefs) }
                }
                
                // 2. MeloCookieWidget
                try {
                    manager.getGlanceIds(MeloCookieWidget::class.java).forEach { glanceId ->
                        updateAppWidgetState(appContext, glanceId) { prefs -> updatePrefsHelper(prefs) }
                    }
                } catch (_: Exception) {}
                
                // Update MeloLyricsWidget as well
                val lyricGlanceIds = manager.getGlanceIds(MeloLyricsWidget::class.java)
                lyricGlanceIds.forEach { glanceId ->
                    updateAppWidgetState(appContext, glanceId) { prefs ->
                        if (song != null) {
                            prefs[stringPreferencesKey(MeloLyricsWidget.KEY_SONG_TITLE)] = song.title
                            prefs[stringPreferencesKey(MeloLyricsWidget.KEY_ARTIST_NAME)] = song.artist
                            song.artworkUri?.let {
                                prefs[stringPreferencesKey(MeloLyricsWidget.KEY_ARTWORK_URI)] = it.toString()
                            } ?: prefs.remove(stringPreferencesKey(MeloLyricsWidget.KEY_ARTWORK_URI))
                        } else {
                            prefs[stringPreferencesKey(MeloLyricsWidget.KEY_SONG_TITLE)] = "Melo"
                            prefs[stringPreferencesKey(MeloLyricsWidget.KEY_ARTIST_NAME)] = ""
                            prefs[stringPreferencesKey(MeloLyricsWidget.KEY_LYRIC_LINES)] = ""
                            prefs[intPreferencesKey(MeloLyricsWidget.KEY_ACTIVE_INDEX)] = -1
                            prefs.remove(stringPreferencesKey(MeloLyricsWidget.KEY_ARTWORK_URI))
                        }
                        prefs[booleanPreferencesKey(MeloLyricsWidget.KEY_IS_PLAYING)] = isPlaying
                    }
                }
                
                // Force update all widgets
                try { MeloMusicWidget().updateAll(appContext) } catch (_: Exception) {}
                try { MeloCookieWidget().updateAll(appContext) } catch (_: Exception) {}
                try { MeloLyricsWidget().updateAll(appContext) } catch (_: Exception) {}
                try { MeloStatsWidget().updateAll(appContext) } catch (_: Exception) {}

                // Update Quick Settings Tile
try {
    android.service.quicksettings.TileService.requestListeningState(
        appContext,
        android.content.ComponentName(appContext, MeloTileService::class.java)
    )
} catch (e: Exception) {
    android.util.Log.e("GlanceWidgetUpdater", "Error updating tile listening state", e)
}
            } catch (e: Exception) {
                android.util.Log.e("GlanceWidgetUpdater", "Error updating widget", e)
            }
        }
    }
    
    /**
     * Update widget to show "No song playing" state
     */
    fun updateWidgetEmpty(context: Context) {
        updateWidget(
            context = context,
            song = null,
            isPlaying = false,
            hasPrevious = false,
            hasNext = false
        )
    }
    
    /**
     * Force update all widgets
     */
    fun forceUpdateAll(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { MeloMusicWidget().updateAll(appContext) } catch (_: Exception) {}
            try { MeloCookieWidget().updateAll(appContext) } catch (_: Exception) {}
            try { MeloLyricsWidget().updateAll(appContext) } catch (_: Exception) {}
            try { MeloStatsWidget().updateAll(appContext) } catch (_: Exception) {}
        }
        
        // Also trigger worker update
        scheduleWidgetUpdate(appContext, delayMillis = 0)
    }
    
    /**
     * Schedule a widget update using WorkManager for reliability
     */
    private fun scheduleWidgetUpdate(context: Context, delayMillis: Long = 0) {
        try {
            val updateRequest = OneTimeWorkRequestBuilder<MeloWidgetWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()
            
            WorkManager.getInstance(context).enqueue(updateRequest)
        } catch (e: Exception) {
            android.util.Log.e("GlanceWidgetUpdater", "Error scheduling widget update", e)
        }
    }
    
    /**
     * Update lyrics widget with dynamic lyric lines and active index
     */
    fun updateLyrics(
        context: Context,
        lyricTexts: List<String>,
        activeIndex: Int
    ) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(appContext)
                val glanceIds = manager.getGlanceIds(MeloLyricsWidget::class.java)
                if (glanceIds.isEmpty()) return@launch
                
                val joined = lyricTexts.joinToString("##LINE##")
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(appContext, glanceId) { prefs ->
                        prefs[stringPreferencesKey(MeloLyricsWidget.KEY_LYRIC_LINES)] = joined
                        prefs[intPreferencesKey(MeloLyricsWidget.KEY_ACTIVE_INDEX)] = activeIndex
                    }
                }
                try { MeloLyricsWidget().updateAll(appContext) } catch (_: Exception) {}
            } catch (e: Exception) {
                android.util.Log.e("GlanceWidgetUpdater", "Error updating lyrics widget", e)
            }
        }
    }

    /**
     * Update launcher app shortcuts dynamically
     */
    private fun updateAppShortcuts(context: Context, isPlaying: Boolean) {
        try {
            val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)
            if (shortcutManager != null) {
                val playPauseShortcut = android.content.pm.ShortcutInfo.Builder(context, "shortcut_play_pause")
                    .setShortLabel(if (isPlaying) "Pause" else "Play")
                    .setLongLabel(if (isPlaying) "Pause Music" else "Play Music")
                    .setIcon(android.graphics.drawable.Icon.createWithResource(context, if (isPlaying) com.melo.player.R.drawable.ic_pause_shortcut else com.melo.player.R.drawable.ic_play_shortcut))
                    .setIntent(Intent(context, com.melo.player.activities.MainActivity::class.java).apply {
                        action = "com.melo.player.action.SHORTCUT_PLAY_PAUSE"
                    })
                    .build()

                val nextShortcut = android.content.pm.ShortcutInfo.Builder(context, "shortcut_next")
                    .setShortLabel("Next")
                    .setLongLabel("Next Track")
                    .setIcon(android.graphics.drawable.Icon.createWithResource(context, com.melo.player.R.drawable.ic_skip_next_shortcut))
                    .setIntent(Intent(context, com.melo.player.activities.MainActivity::class.java).apply {
                        action = "com.melo.player.action.SHORTCUT_SKIP_NEXT"
                    })
                    .build()

                val prevShortcut = android.content.pm.ShortcutInfo.Builder(context, "shortcut_previous")
                    .setShortLabel("Previous")
                    .setLongLabel("Previous Track")
                    .setIcon(android.graphics.drawable.Icon.createWithResource(context, com.melo.player.R.drawable.ic_skip_previous_shortcut))
                    .setIntent(Intent(context, com.melo.player.activities.MainActivity::class.java).apply {
                        action = "com.melo.player.action.SHORTCUT_SKIP_PREVIOUS"
                    })
                    .build()

                val openPlayerShortcut = android.content.pm.ShortcutInfo.Builder(context, "shortcut_open_player")
                    .setShortLabel("Open Player")
                    .setLongLabel("Open Music Player")
                    .setIcon(android.graphics.drawable.Icon.createWithResource(context, com.melo.player.R.drawable.melo_icon_small))
                    .setIntent(Intent(context, com.melo.player.activities.MainActivity::class.java).apply {
                        action = Intent.ACTION_MAIN
                        putExtra("OPEN_PLAYER", true)
                    })
                    .build()

                shortcutManager.dynamicShortcuts = listOf(playPauseShortcut, nextShortcut, prevShortcut, openPlayerShortcut)
                // Track shortcut usage so the system can surface the shortcuts as suggestions
                listOf("shortcut_play_pause", "shortcut_next", "shortcut_previous", "shortcut_open_player")
                    .forEach { ShortcutManagerCompat.reportShortcutUsed(context, it) }
            }
        } catch (e: Exception) {
            android.util.Log.e("GlanceWidgetUpdater", "Error updating dynamic shortcuts", e)
        }
    }
}
