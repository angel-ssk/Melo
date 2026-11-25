/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.data.model

enum class MediaScanMode(val value: String) {
    BLACKLIST("blacklist"),
    WHITELIST("whitelist");

    companion object {
        fun fromValue(value: String): MediaScanMode =
            entries.firstOrNull { it.value == value } ?: BLACKLIST
    }
}
