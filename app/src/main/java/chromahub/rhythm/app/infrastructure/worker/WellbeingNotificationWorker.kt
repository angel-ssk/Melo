/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Melo 1.1: Digital Wellbeing notifications.
 */

package chromahub.rhythm.app.infrastructure.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import chromahub.rhythm.app.activities.MainActivity
import chromahub.rhythm.app.shared.data.model.AppSettings
import chromahub.rhythm.app.shared.data.repository.PlaybackStatsRepository
import chromahub.rhythm.app.shared.data.repository.StatsTimeRange
import com.melo.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/** Daily wellbeing reminder: daily goal, wind-down and streak milestones. */
class WellbeingNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val settings = AppSettings.getInstance(applicationContext)
            if (!settings.wellbeingNotifications.value) return@withContext Result.success()

            val stats = PlaybackStatsRepository.getInstance(applicationContext)
            val today = runCatching { stats.loadSummary(StatsTimeRange.TODAY) }.getOrNull()
            val allTime = runCatching { stats.loadSummary(StatsTimeRange.ALL_TIME) }.getOrNull()
            val minutes = (today?.totalDurationMs ?: 0L) / 60_000
            val goal = settings.profileDailyGoalMinutes.value
            val streak = allTime?.currentStreakDays ?: 0
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            val title: String
            val body: String
            when {
                minutes >= goal -> {
                    title = "¡Meta diaria completada!"
                    body = "Hoy escuchaste $minutes min de $goal min. Tu ritmo está en su punto."
                }
                streak >= 7 -> {
                    title = "Llevas $streak días de racha"
                    body = "Escuchaste $minutes min hoy. Mantén tu Melo en movimiento."
                }
                hour >= 21 -> {
                    title = "Hora de bajar el ritmo"
                    body = "Te faltan ${goal - minutes} min para tu meta diaria. Descansa un poco."
                }
                else -> return@withContext Result.success()
            }

            showNotification(title, body)
            Result.success()
        }.getOrDefault(Result.success())
    }

    private fun showNotification(title: String, body: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Melo Bienestar",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Recordatorios de tu meta diaria y rachas" }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "melo_wellbeing_notifications"
        private const val CHANNEL_ID = "melo_wellbeing"
        private const val NOTIFICATION_ID = 2401
    }
}
