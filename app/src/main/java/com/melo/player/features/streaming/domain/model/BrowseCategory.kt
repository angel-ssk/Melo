/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.features.streaming.domain.model

/**
 * Represents a browse category/genre from a streaming service.
 * Used for browsing music by category (e.g., "Rock", "Pop", "Workout", etc.).
 */
data class BrowseCategory(
    val id: String,
    val name: String,
    val iconUrl: String? = null
)
