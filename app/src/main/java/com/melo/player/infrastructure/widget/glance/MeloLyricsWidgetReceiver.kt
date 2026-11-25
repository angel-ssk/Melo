/*
 * Copyright (C) 2025 nift4 (Gramophone)
 * Modified for Melo by Anjishnu Nandi (angel-ssk)
 *
 * SPDX-FileCopyrightText: 2025 nift4 <https://github.com/FoedusProgramme/Gramophone>
 * SPDX-FileCopyrightText: 2025-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.infrastructure.widget.glance

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class MeloLyricsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeloLyricsWidget()
}
