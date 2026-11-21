/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Rhythm fork: monthly shareable Wrap card.
 */

package chromahub.rhythm.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import chromahub.rhythm.app.shared.data.repository.PlaybackStatsRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Draws a shareable "Melo Wrap" card directly with [Canvas] instead of
 * composing an offscreen view. A single 720×1280 bitmap (~3.5 MB) is allocated
 * and immediately recycled, which is important on low-RAM devices.
 */
object RhythmWrapGenerator {

    fun generate(
        context: Context,
        summary: PlaybackStatsRepository.PlaybackStatsSummary,
        profileName: String
    ): File? = runCatching {
        val width = 720
        val height = 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                longArrayOf(0xFF1B1030, 0xFF37146B, 0xFF0B2E4D),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), background)

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                560f, 180f, 30f, 560f, 180f, 420f,
                longArrayOf(0x66FF8FD8, 0x00FF8FD8),
                floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(560f, 180f, 420f, glow)

        val white = Color.parseColor("#FFFFFFFF")
        val muted = Color.parseColor("#B8FFFFFF")
        val bold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val regular = Typeface.DEFAULT

        fun drawText(
            text: String, x: Float, y: Float, size: Float,
            color: Int = white, typeface: Typeface = bold, align: Paint.Align = Paint.Align.LEFT
        ) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = size
                this.typeface = typeface
                textAlign = align
            }
            canvas.drawText(text, x, y, paint)
        }

        val month = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        drawText("RHYTHM", 64f, 110f, 34f, muted)
        drawText("WRAP", 64f, 175f, 82f)
        drawText(month.replaceFirstChar { it.uppercase() }, 64f, 230f, 30f, muted, regular)

        drawText(profileName, 64f, 315f, 42f)

        val totalMinutes = summary.totalDurationMs / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val duration = if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
        drawText("TIEMPO DE ESCUCHA", 64f, 405f, 24f, muted, regular)
        drawText(duration, 64f, 490f, 92f)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26FFFFFF")
        }
        fun card(x: Float, y: Float, w: Float, h: Float) =
            canvas.drawRoundRect(x, y, x + w, y + h, 28f, 28f, cardPaint)

        card(64f, 560f, 280f, 190f)
        card(376f, 560f, 280f, 190f)
        card(64f, 778f, 592f, 150f)

        drawText(summary.totalPlayCount.toString(), 92f, 650f, 62f)
        drawText("CANCIONES", 92f, 700f, 22f, muted, regular)
        drawText(summary.uniqueArtists.toString(), 404f, 650f, 62f)
        drawText("ARTISTAS", 404f, 700f, 22f, muted, regular)

        val topArtist = summary.topArtists.firstOrNull()?.artist ?: "—"
        val artistText = if (topArtist.length > 22) topArtist.take(21) + "…" else topArtist
        drawText("TU ARTISTA TOP", 92f, 840f, 22f, muted, regular)
        drawText(artistText, 92f, 890f, 44f)

        val topSong = summary.topSongs.firstOrNull()
        val songText = topSong?.title?.let {
            if (it.length > 30) it.take(29) + "…" else it
        } ?: "—"
        drawText("TU CANCIÓN TOP", 92f, 1000f, 22f, muted, regular)
        drawText(songText, 92f, 1050f, 38f)

        drawText("Your music, your rhythm.", 64f, 1180f, 26f, muted, regular)

        val dir = File(context.externalCacheDir ?: context.cacheDir, "rhythm_wrap").apply { mkdirs() }
        val file = File(dir, "rhythm_wrap_${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
        bitmap.recycle()
        file
    }.getOrNull()

    fun share(context: Context, file: File) {
        runCatching {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Melo Wrap"))
        }
    }
}
