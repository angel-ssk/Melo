/*
 * Copyright (C) 2025 nift4 (Gramophone)
 * Modified for Melo by Anjishnu Nandi (angel-ssk)
 *
 * SPDX-FileCopyrightText: 2025 nift4 <https://github.com/FoedusProgramme/Gramophone>
 * SPDX-FileCopyrightText: 2025-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.infrastructure.widget

import android.content.Context
import com.melo.player.shared.data.model.Song
import com.melo.player.infrastructure.widget.glance.GlanceWidgetUpdater

/**
 * Thin shim that forwards widget update calls to the Glance-based updater.
 * The legacy RemoteViews widget (MusicWidgetProvider) has been removed.
 */
object WidgetUpdater {

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
        GlanceWidgetUpdater.updateWidget(
            context = context,
            song = song,
            isPlaying = isPlaying,
            hasPrevious = hasPrevious,
            hasNext = hasNext,
            isFavorite = isFavorite,
            isShuffleEnabled = isShuffleEnabled,
            repeatMode = repeatMode
        )
    }

    fun clearWidget(context: Context) {
        GlanceWidgetUpdater.updateWidgetEmpty(context)
    }
}
