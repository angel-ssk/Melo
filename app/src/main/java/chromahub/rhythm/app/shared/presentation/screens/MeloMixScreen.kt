/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Melo Mix — daily mix inspired by Material 3 Expressive concepts.
 */
package chromahub.rhythm.app.shared.presentation.screens

import android.content.Context
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import chromahub.rhythm.app.features.local.presentation.viewmodel.MusicViewModel
import chromahub.rhythm.app.shared.data.model.Song
import chromahub.rhythm.app.shared.data.repository.PlaybackStatsRepository
import chromahub.rhythm.app.shared.presentation.components.icons.Icon
import chromahub.rhythm.app.shared.presentation.components.icons.MaterialSymbolIcon
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import chromahub.rhythm.app.ui.theme.RhythmMotion
import com.melo.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeloMixScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val songs by viewModel.songs.collectAsState()
    val playCounts by viewModel.songPlayCounts.collectAsState()
    var mix by remember { mutableStateOf<List<Song>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(songs) {
        mix = withContext(Dispatchers.IO) {
            buildDailyMix(context, songs, playCounts)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.melo_mix_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = chromahub.rhythm.app.shared.presentation.components.icons.RhythmIcons.Back,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 24.dp + LocalMiniPlayerPadding.current.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.your_mix),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        lineHeight = 56.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Text(
                    text = stringResource(R.string.melo_mix_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            item {
                MixOrbit(mix = mix) { song ->
                    viewModel.playSongFromContext(song, mix, "Melo Mix")
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                FloatingMixButton(
                    enabled = mix.isNotEmpty(),
                    onClick = { if (mix.isNotEmpty()) viewModel.playQueue(mix, enableShuffle = true) }
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.daily_mix),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.daily_mix_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            mix.take(3).forEach { s ->
                                SongArt(song = s, size = 34.dp)
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        val shown = if (expanded) mix else mix.take(5)
                        shown.forEachIndexed { index, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.playSongFromContext(song, mix, "Mix Diario") }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SongArt(song = song, size = 44.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                IconButton(onClick = { viewModel.playSongFromContext(song, mix, "Mix Diario") }) {
                                    Icon(
                                        imageVector = MaterialSymbolIcon("play_arrow"),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        if (mix.size > 5) {
                            TextButton(onClick = { expanded = !expanded }) {
                                Text(
                                    text = stringResource(
                                        if (expanded) R.string.melo_mix_show_less else R.string.melo_mix_show_all
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SongArt(song: Song, size: androidx.compose.ui.unit.Dp) {
    val art = song.artworkUri?.toString()
    if (!art.isNullOrBlank()) {
        AsyncImage(
            model = art,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(song.title.take(1), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MixOrbit(mix: List<Song>, onClick: (Song) -> Unit) {
    val float = rememberInfiniteTransition(label = "mixOrbit")
    val rotation by float.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000), RepeatMode.Restart),
        label = "mixRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        contentAlignment = Alignment.Center
    ) {
        val items = mix.take(5)
        items.forEachIndexed { index, song ->
            val angle = (index * 72f + rotation)
            val rad = Math.toRadians(angle.toDouble())
            val radius = 110f
            val x = (kotlin.math.cos(rad) * radius).dp
            val y = (kotlin.math.sin(rad) * radius * 0.55f).dp
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(if (index == 0) 132.dp else 76.dp)
                    .rotate(if (index == 0) 0f else angle)
                    .clip(RoundedCornerShape(percent = 42))
                    .clickable { onClick(song) },
                contentAlignment = Alignment.Center
            ) {
                SongArt(song = song, size = if (index == 0) 132.dp else 76.dp)
            }
        }
    }
}

@Composable
private fun FloatingMixButton(enabled: Boolean, onClick: () -> Unit) {
    val float = rememberInfiniteTransition(label = "mixPulse")
    val scale by float.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "mixPulseScale"
    )
    Box(
        modifier = Modifier
            .size(84.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = MaterialSymbolIcon("shuffle"),
            contentDescription = stringResource(R.string.melo_mix_shuffle_cd),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(30.dp)
        )
    }
}

private suspend fun buildDailyMix(
    context: Context,
    songs: List<Song>,
    playCounts: Map<String, Int>
): List<Song> {
    val stats = PlaybackStatsRepository.getInstance(context)
    val ids = linkedSetOf<String>()
    ids.addAll(stats.getOnRepeatSongIds(limit = 10))
    ids.addAll(stats.getRecentlyPlayedSongIds(limit = 12))
    ids.addAll(stats.getForgottenFavoritesSongIds(playCounts, limit = 8))
    val picked = songs.filter { it.id in ids }.toMutableList()
    if (picked.size < 12) {
        picked.addAll(songs.sortedByDescending { it.dateAdded }.take(12 - picked.size))
    }
    return picked.distinctBy { it.id }.shuffled().take(30)
}
