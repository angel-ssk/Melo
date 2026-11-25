/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/**
 * Saver for LazyListState to preserve scroll position across configuration changes
 * and navigation.
 */
val LazyListStateSaver: Saver<LazyListState, *> = listSaver(
    save = { listOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) },
    restore = {
        LazyListState(
            firstVisibleItemIndex = it[0],
            firstVisibleItemScrollOffset = it[1]
        )
    }
)
