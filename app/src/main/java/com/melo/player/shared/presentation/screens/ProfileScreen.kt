/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.presentation.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.melo.player.R
import com.melo.player.MeloApplication
import com.melo.player.shared.data.model.AppSettings
import com.melo.player.shared.data.repository.PlaybackStatsRepository
import com.melo.player.shared.data.repository.StatsTimeRange
import com.melo.player.shared.presentation.components.common.ExpressiveButtonGroup
import com.melo.player.shared.presentation.components.icons.Icon
import com.melo.player.shared.presentation.components.icons.MeloIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val profileName by appSettings.profileName.collectAsState()
    val photoPath by appSettings.profilePhotoPath.collectAsState()

    var rangeIndex by remember { mutableIntStateOf(1) }
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf(profileName) }

    val ranges = listOf(
        StatsTimeRange.TODAY,
        StatsTimeRange.WEEK,
        StatsTimeRange.MONTH,
        StatsTimeRange.YEAR
    )
    val range = ranges[rangeIndex]
    val rangeLabels = listOf("Hoy", "Semana", "Mes", "Año")

    var summary by remember { mutableStateOf<PlaybackStatsRepository.PlaybackStatsSummary?>(null) }
    LaunchedEffect(range) {
        summary = withContext(Dispatchers.IO) {
            PlaybackStatsRepository.getInstance(context).loadSummary(range)
        }
    }

    val photoModel = photoPath?.let { File(it) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val dest = File(context.filesDir, "profile_photo.jpg")
                FileOutputStream(dest).use { output -> input.copyTo(output) }
                appSettings.setProfilePhotoPath(dest.absolutePath)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Hero background: user's photo (or app artwork), softly blurred.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .then(if (MeloApplication.isLowRam) Modifier else Modifier.blur(28.dp))
        ) {
            if (photoModel != null) {
                AsyncImage(
                    model = photoModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = MeloIcons.Back, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(120.dp))

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .clickable { pickPhoto.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoModel != null) {
                    AsyncImage(
                        model = photoModel,
                        contentDescription = stringResource(R.string.profile_choose_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(model = R.drawable.melo_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profileName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@melolistener",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExpressiveButtonGroup(
                items = rangeLabels,
                selectedIndex = rangeIndex,
                onItemClick = { rangeIndex = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.profile_melo_stats),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val stats = summary
            if (stats == null || stats.totalPlayCount == 0) {
                Text(
                    text = stringResource(R.string.profile_no_stats),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val hours = stats.totalDurationMs / 3_600_000L
                val minutes = (stats.totalDurationMs % 3_600_000L) / 60_000L
                val topSong = stats.topSongs.firstOrNull()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = "${hours}h ${minutes}m",
                        label = stringResource(R.string.profile_listening_time)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = "${stats.uniqueSongs}",
                        label = stringResource(R.string.profile_unique_songs)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.profile_top_song),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (topSong != null) {
                            Text(
                                text = topSong.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = topSong.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.profile_no_stats),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                StatCard(
                    modifier = Modifier.fillMaxWidth(),
                    value = "${stats.totalSessions}",
                    label = stringResource(R.string.profile_sessions)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { draftName = profileName; showNameDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.profile_edit_name))
                }
                Button(
                    onClick = { pickPhoto.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.profile_choose_photo))
                }
            }
        }

        if (showNameDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text(stringResource(R.string.profile_edit_name)) },
                text = {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.profile_edit_name)) }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        appSettings.setProfileName(draftName.trim().ifBlank { "Melo" })
                        showNameDialog = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = { showNameDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
