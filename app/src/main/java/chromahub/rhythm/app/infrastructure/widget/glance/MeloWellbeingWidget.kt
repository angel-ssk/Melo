/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Melo 1.1: wellbeing home-screen widget.
 */

package chromahub.rhythm.app.infrastructure.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import chromahub.rhythm.app.activities.MainActivity
import chromahub.rhythm.app.shared.data.model.AppSettings
import chromahub.rhythm.app.shared.data.repository.PlaybackStatsRepository
import chromahub.rhythm.app.shared.data.repository.StatsTimeRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Small 2x2 widget showing today's listening goal progress and active days. */
class MeloWellbeingWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = AppSettings.getInstance(context)
        val goal = settings.profileDailyGoalMinutes.value
        val activeDays = settings.dailyListeningStats.value.count { it.value > 0L }
        val today = withContext(Dispatchers.IO) {
            runCatching {
                PlaybackStatsRepository.getInstance(context).loadSummary(StatsTimeRange.TODAY)
            }.getOrNull()
        }
        val minutes = ((today?.totalDurationMs ?: 0L) / 60_000L).toInt()

        provideContent {
            WidgetContent(minutes, goal, activeDays)
        }
    }

    @Composable
    private fun WidgetContent(minutes: Int, goal: Int, activeDays: Int) {
        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(20.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Melo · Bienestar",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = "$minutes / $goal min",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .cornerRadius(4.dp)
                            .background(GlanceTheme.colors.onPrimaryContainer)
                    ) {}
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = "$activeDays días escuchando",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
