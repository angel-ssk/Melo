/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.data.model

/**
 * Preference for canvas network playback mode
 */
enum class CanvasNetworkMode(val displayName: String) {
    WIFI_ONLY("Wi-Fi Only"),
    BOTH("Wi-Fi and Cellular");
    
    companion object {
        fun fromOrdinal(ordinal: Int): CanvasNetworkMode {
            return values().getOrElse(ordinal) { BOTH }
        }
    }
}
