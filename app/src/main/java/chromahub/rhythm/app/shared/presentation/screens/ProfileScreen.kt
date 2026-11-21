/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Rhythm fork: Digital Wellbeing-style profile screen.
 */

package chromahub.rhythm.app.shared.presentation.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.melo.app.R
import chromahub.rhythm.app.features.local.presentation.viewmodel.MusicViewModel
import chromahub.rhythm.app.shared.data.model.AppSettings
import chromahub.rhythm.app.shared.data.repository.PlaybackStatsRepository
import chromahub.rhythm.app.shared.data.repository.StatsTimeRange
import chromahub.rhythm.app.shared.presentation.components.icons.Icon
import chromahub.rhythm.app.shared.presentation.components.icons.MaterialSymbolIcon
import chromahub.rhythm.app.shared.presentation.components.icons.RhythmIcons
import chromahub.rhythm.app.ui.theme.RhythmMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

/** The four listening windows shown in the Wellbeing-style profile. */
enum class ProfileRange(val statsRange: StatsTimeRange, val labelRes: Int, val daysInWindow: Int) {
    DAY(StatsTimeRange.TODAY, R.string.profile_range_day, 1),
    WEEK(StatsTimeRange.WEEK, R.string.profile_range_week, 7),
    MONTH(StatsTimeRange.MONTH, R.string.profile_range_month, 30),
    YEAR(StatsTimeRange.YEAR, R.string.profile_range_year, 365)
}

private data class ProfileSnapshot(
    val range: ProfileRange,
    val summary: PlaybackStatsRepository.PlaybackStatsSummary?,
    val todayMinutes: Int
)

/**
 * Wellbeing-style profile screen. Shows the user identity and "Rhythm
 * estadísticas" for day / week / month / year with complex, spring-driven
 * state transitions tuned for 120 Hz+ panels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    appSettings: AppSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val statsRepository = remember { PlaybackStatsRepository.getInstance(context) }

    val profileName by appSettings.profileName.collectAsState()
    val photoPath by appSettings.profilePhotoPath.collectAsState()
    val joinDate by appSettings.profileJoinDate.collectAsState()
    val dailyGoal by appSettings.profileDailyGoalMinutes.collectAsState()
    val dailyStats by appSettings.dailyListeningStats.collectAsState()

    var selectedRange by rememberSaveable { mutableStateOf(ProfileRange.WEEK) }
    var snapshot by remember {
        mutableStateOf(ProfileSnapshot(selectedRange, null, 0))
    }
    var isLoading by remember { mutableStateOf(true) }
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf(profileName) }
    var isGeneratingWrap by remember { mutableStateOf(false) }
    var wrapError by remember { mutableStateOf<String?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val directory = File(context.filesDir, "profile").apply { mkdirs() }
                    val destination = File(directory, "avatar.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        destination.outputStream().use { output -> input.copyTo(output) }
                    }
                    destination.absolutePath
                }
            }.getOrNull()?.let { path -> appSettings.setProfilePhotoPath(path) }
        }
    }

    LaunchedEffect(selectedRange) {
        isLoading = true
        val summary = runCatching {
            statsRepository.loadSummary(selectedRange.statsRange)
        }.getOrNull()
        val todaySummary = runCatching {
            statsRepository.loadSummary(StatsTimeRange.TODAY)
        }.getOrNull()
        val todayMinutes = ((todaySummary?.totalDurationMs ?: 0L) / 60_000L).toInt()
        snapshot = ProfileSnapshot(selectedRange, summary, todayMinutes)
        isLoading = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = RhythmIcons.Back,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Identity header -------------------------------------------------
            ProfileIdentityHeader(
                name = profileName,
                photoPath = photoPath,
                joinDate = joinDate,
                onAvatarClick = { photoLauncher.launch("image/*") },
                onNameClick = {
                    draftName = profileName
                    showNameDialog = true
                }
            )

            Spacer(Modifier.height(20.dp))

            // --- Range selector with animated indicator -------------------------
            ProfileRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it }
            )

            Spacer(Modifier.height(18.dp))

            // --- Hero ring + stats, swapped with a complex state transition -----
            AnimatedContent(
                targetState = snapshot,
                transitionSpec = { RhythmMotion.rangeContent },
                label = "profileRangeStats"
            ) { state ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val activeDays = rememberActiveDays(state.range, state.summary, dailyStats)
                    ListeningDaysRing(
                        range = state.range,
                        activeDays = activeDays,
                        totalDays = state.range.daysInWindow
                    )
                    Spacer(Modifier.height(18.dp))
                    RhythmStatsGrid(summary = state.summary, isLoading = isLoading)
                    Spacer(Modifier.height(8.dp))
                    StreakCard(summary = state.summary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Listening heatmap (last 12 months) ----------------------------
            ListeningHeatmap(dailyStats = dailyStats)

            Spacer(Modifier.height(20.dp))

            // --- New: daily listening goal (Digital Wellbeing style) ------------
            DailyGoalCard(
                todayMinutes = snapshot.todayMinutes,
                goalMinutes = dailyGoal,
                onGoalChange = { appSettings.setProfileDailyGoalMinutes(it) }
            )

            Spacer(Modifier.height(16.dp))

            FilledTonalButton(
                onClick = {
                    scope.launch {
                        isGeneratingWrap = true
                        wrapError = null
                        val monthSummary = runCatching {
                            statsRepository.loadSummary(StatsTimeRange.MONTH)
                        }.getOrNull()
                        val file = monthSummary?.let {
                            withContext(Dispatchers.IO) {
                                chromahub.rhythm.app.util.RhythmWrapGenerator.generate(
                                    context = context,
                                    summary = it,
                                    profileName = profileName
                                )
                            }
                        }
                        if (file != null) {
                            chromahub.rhythm.app.util.RhythmWrapGenerator.share(context, file)
                        } else {
                            wrapError = context.getString(R.string.profile_wrap_error)
                        }
                        isGeneratingWrap = false
                    }
                },
                enabled = !isGeneratingWrap,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGeneratingWrap) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_wrap_generating))
                } else {
                    Icon(
                        imageVector = MaterialSymbolIcon("ios_share"),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_wrap_button))
                }
            }
            wrapError?.let { error ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Leave room for the mini player when a song is playing.
            Spacer(Modifier.height(112.dp))
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.profile_edit_name_title)) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.profile_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (draftName.isNotBlank()) appSettings.setProfileName(draftName)
                    showNameDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

/**
 * Reusable avatar used both in the top-left corner of Home and in the profile
 * header. Alpha/scale are applied with graphicsLayer so the press animation
 * never invalidates layout (important on 120 Hz displays).
 */
@Composable
fun ProfileAvatar(
    photoPath: String?,
    name: String,
    size: Dp,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = RhythmMotion.spatial,
        label = "avatarPress"
    )
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(clickableModifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        val avatarFile = remember(photoPath) { photoPath?.let(::File) }
        // Decode at avatar resolution only: avoids keeping a full-size photo
        // bitmap in memory on devices with little free RAM.
        val avatarRequest = remember(avatarFile) {
            avatarFile?.let {
                ImageRequest.Builder(context)
                    .data(it)
                    .size(256)
                    .crossfade(true)
                    .build()
            }
        }
        if (avatarFile != null && avatarFile.exists() && avatarRequest != null) {
            AsyncImage(
                model = avatarRequest,
                contentDescription = stringResource(R.string.profile_avatar_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ProfileIdentityHeader(
    name: String,
    photoPath: String?,
    joinDate: String,
    onAvatarClick: () -> Unit,
    onNameClick: () -> Unit
) {
    val transition = updateTransition(targetState = name, label = "profileIdentity")
    val headerScale by transition.animateFloat(
        transitionSpec = { RhythmMotion.spatial },
        label = "headerScale"
    ) { 1f }
    val headerAlpha by transition.animateFloat(
        transitionSpec = { RhythmMotion.emphasis },
        label = "headerAlpha"
    ) { 1f }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = headerScale
                scaleY = headerScale
                alpha = headerAlpha
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            ProfileAvatar(
                photoPath = photoPath,
                name = name,
                size = 104.dp,
                onClick = onAvatarClick
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbolIcon("photo_camera"),
                    contentDescription = stringResource(R.string.profile_change_photo_cd),
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedContent(
            targetState = name,
            transitionSpec = { RhythmMotion.identityContent },
            label = "profileName"
        ) { displayName ->
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        TextButton(onClick = onNameClick) {
            Text(stringResource(R.string.profile_edit_name))
        }
        val joinLabel = remember(joinDate) {
            val parsed = runCatching { LocalDate.parse(joinDate).toString() }.getOrDefault(joinDate)
            parsed
        }
        Text(
            text = stringResource(R.string.profile_member_since, joinLabel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileRangeSelector(
    selectedRange: ProfileRange,
    onRangeSelected: (ProfileRange) -> Unit
) {
    val entries = ProfileRange.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        entries.forEach { range ->
            val selected = range == selectedRange
            val weight by animateFloatAsState(
                targetValue = if (selected) 1.12f else 1f,
                animationSpec = RhythmMotion.spatial,
                label = "rangeWeight"
            )
            val containerColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainer,
                animationSpec = RhythmMotion.color,
                label = "rangeColor"
            )
            val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .weight(weight)
                    .clip(RoundedCornerShape(24.dp))
                    .background(containerColor)
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(range.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun ListeningDaysRing(range: ProfileRange, activeDays: Int, totalDays: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val progress by animateFloatAsState(
        targetValue = (activeDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f),
        animationSpec = RhythmMotion.hero,
        label = "listeningRingProgress"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = activeDays.toString(),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 52.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.profile_days_listening),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.profile_of_days, totalDays),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RhythmStatsGrid(
    summary: PlaybackStatsRepository.PlaybackStatsSummary?,
    isLoading: Boolean
) {
    if (isLoading && summary == null) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
        return
    }
    val stats = summary
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_listening_time),
                value = formatDuration(stats?.totalDurationMs ?: 0L),
                icon = RhythmIcons.Headphones
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_songs),
                value = (stats?.totalPlayCount ?: 0).toString(),
                icon = RhythmIcons.MusicNote
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_artists),
                value = (stats?.uniqueArtists ?: 0).toString(),
                icon = RhythmIcons.Artist
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_sessions),
                value = (stats?.totalSessions ?: 0).toString(),
                icon = RhythmIcons.Queue
            )
        }
        Text(
            text = stringResource(R.string.profile_rhythm_stats_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: MaterialSymbolIcon
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakCard(summary: PlaybackStatsRepository.PlaybackStatsSummary?) {
    val streak = summary?.currentStreakDays ?: 0
    AnimatedVisibility(visible = streak > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = RhythmIcons.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.profile_streak, streak),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(R.string.profile_streak_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * New feature: a Digital Wellbeing-style daily listening goal. The slider is
 * intentionally interactive (state updates use springs, so dragging stays
 * smooth even on high-refresh panels).
 */
@Composable
private fun DailyGoalCard(
    todayMinutes: Int,
    goalMinutes: Int,
    onGoalChange: (Int) -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (todayMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f),
        animationSpec = RhythmMotion.surface,
        label = "dailyGoalProgress"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = MaterialSymbolIcon("schedule"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.profile_daily_goal_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.profile_daily_goal_progress, todayMinutes, goalMinutes),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.profile_daily_goal_adjust),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = goalMinutes.toFloat(),
                valueRange = 15f..240f,
                steps = 14,
                onValueChange = { onGoalChange(it.toInt()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Counts days with listening activity, combining durable daily counters with stats events. */
@Composable
private fun rememberActiveDays(
    range: ProfileRange,
    summary: PlaybackStatsRepository.PlaybackStatsSummary?,
    dailyStats: Map<String, Long>
): Int {
    return remember(range, dailyStats, summary) {
        val today = LocalDate.now()
        val counted = dailyStats.keys.count { key ->
            val date = runCatching { LocalDate.parse(key) }.getOrNull() ?: return@count false
            val active = (dailyStats[key] ?: 0L) > 0L
            if (!active) return@count false
            when (range) {
                ProfileRange.DAY -> date == today
                ProfileRange.WEEK -> !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
                ProfileRange.MONTH -> !date.isBefore(today.minusDays(29)) && !date.isAfter(today)
                ProfileRange.YEAR -> date.year == today.year
            }
        }
        maxOf(counted, summary?.activeDays ?: 0).coerceAtMost(range.daysInWindow)
    }
}

/**
 * 52×7 listening heatmap. Drawn in a single Canvas pass (no 364 composables),
 * which keeps composition and memory pressure low on devices with little free
 * RAM. Intensity comes from the durable daily listening counters.
 */
@Composable
private fun ListeningHeatmap(dailyStats: Map<String, Long>) {
    val today = LocalDate.now()
    val endOfWeek = today.plusDays((6 - (today.dayOfWeek.value - 1)).toLong())
    val startDate = endOfWeek.minusDays(363)

    val levels = remember(dailyStats, startDate) {
        val max = dailyStats.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        List(52 * 7) { index ->
            val date = startDate.plusDays(index.toLong())
            val count = dailyStats[date.toString()] ?: 0L
            when {
                count <= 0L -> 0
                count < max / 4 -> 1
                count < max / 2 -> 2
                count < max * 3 / 4 -> 3
                else -> 4
            }
        }
    }
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val primary = MaterialTheme.colorScheme.primary
    val levelColors = remember(primary) {
        listOf(primary.copy(alpha = 0.28f), primary.copy(alpha = 0.5f),
            primary.copy(alpha = 0.72f), primary)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_heatmap_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.profile_heatmap_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            val gap = 2.dp.toPx()
            val columns = 52
            val rows = 7
            val cellW = (size.width - gap * (columns - 1)) / columns
            val cellH = (size.height - gap * (rows - 1)) / rows
            val side = kotlin.math.min(cellW, cellH)
            val radius = CornerRadius(side * 0.32f)
            levels.forEachIndexed { index, level ->
                val column = index / rows
                val row = index % rows
                val x = column * (cellW + gap)
                val y = row * (cellH + gap)
                val color = if (level == 0) emptyColor else levelColors[level - 1]
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(side, side),
                    cornerRadius = radius
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.profile_heatmap_less),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(emptyColor)
            )
            levelColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.profile_heatmap_more),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / 60_000L
    val hours = minutes / 60L
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "$minutes min"
}

/** Small helper used by the Home top-left avatar to keep layout stable. */
@Composable
fun ProfileAvatarButton(photoPath: String?, name: String, onClick: () -> Unit) {
    ProfileAvatar(photoPath = photoPath, name = name, size = 40.dp, onClick = onClick)
}
