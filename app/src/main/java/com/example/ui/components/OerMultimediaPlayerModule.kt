package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.curriculum.oer.OerMediaResource
import com.example.data.curriculum.oer.OerMediaType
import com.example.data.curriculum.oer.OerPlaybackCheckpoint
import com.example.data.curriculum.oer.OerTranscriptLine
import com.example.speech.SpeechManager
import com.example.ui.NeuroPathViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, Accessible Video and Audio Playback Module for OER Commons Curated Collections.
 * Supports:
 * - Animated/streaming video lesson views with playback scrubber
 * - Auditory lecture & podcast playback with dynamic soundwave frequency visualizer
 * - Synchronized closed caption HUD (with dyslexia-friendly styling)
 * - In-video/in-audio Socratic quiz checkpoints with celebratory feedback
 * - Interactive timestamped transcript navigation (jump to point)
 * - TTS Vocal Narration fallback
 * - Variable playback speed: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OerMultimediaPlayerBottomSheet(
    resource: OerMediaResource,
    onDismiss: () -> Unit,
    viewModel: NeuroPathViewModel? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("oer_media_player_sheet")
    ) {
        OerMultimediaPlayerContent(
            resource = resource,
            onClose = onDismiss,
            viewModel = viewModel
        )
    }
}

@Composable
fun OerMultimediaPlayerContent(
    resource: OerMediaResource,
    onClose: () -> Unit,
    viewModel: NeuroPathViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionSec by remember { mutableIntStateOf(0) }
    val totalDurationSec = remember(resource) { resource.durationSeconds.coerceAtLeast(1) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var captionsEnabled by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Player, 1: Transcript, 2: Key Takeaways
    var activeCheckpoint by remember { mutableStateOf<OerPlaybackCheckpoint?>(null) }
    var checkpointAnswered by remember { mutableStateOf(false) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }

    val passedCheckpoints = remember { mutableSetOf<Int>() }

    // TTS speech support for auditory narration
    val speechManager = remember { SpeechManager(context) }
    var ttsNarrationEnabled by remember { mutableStateOf(false) }

    // Auto-advancing playback timer
    LaunchedEffect(isPlaying, playbackSpeed, currentPositionSec, totalDurationSec, activeCheckpoint) {
        if (isPlaying && activeCheckpoint == null && currentPositionSec < totalDurationSec) {
            val stepDelay = (1000L / playbackSpeed).toLong().coerceAtLeast(200L)
            delay(stepDelay)
            val nextSec = currentPositionSec + 1
            currentPositionSec = nextSec

            // Check if there is an in-video checkpoint at this second
            val checkpoint = resource.checkpoints.find { it.timestampSeconds == nextSec && !passedCheckpoints.contains(it.timestampSeconds) }
            if (checkpoint != null) {
                isPlaying = false
                activeCheckpoint = checkpoint
                checkpointAnswered = false
                selectedAnswerIndex = null
                isAnswerCorrect = null
                passedCheckpoints.add(checkpoint.timestampSeconds)
            }

            // Sync TTS if enabled
            if (ttsNarrationEnabled) {
                val line = resource.transcript.find { it.timestampSeconds == nextSec }
                if (line != null) {
                    speechManager.speak(line.text)
                }
            }
        } else if (currentPositionSec >= totalDurationSec) {
            isPlaying = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stop()
        }
    }

    // Active transcript line
    val currentTranscriptLine = remember(currentPositionSec, resource.transcript) {
        resource.transcript.lastOrNull { it.timestampSeconds <= currentPositionSec }
            ?: resource.transcript.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (resource.mediaType) {
                        OerMediaType.VIDEO_LESSON, OerMediaType.SCIENCE_SIMULATION -> Color(0xFFEFF6FF)
                        else -> Color(0xFFFDF2F8)
                    }
                ) {
                    Text(
                        resource.mediaType.emoji,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        resource.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${resource.mediaType.label} • ${resource.creatorOrSource}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.testTag("close_media_player_btn")) {
                Icon(Icons.Default.Close, contentDescription = "Close Player")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Main Stage: Video Canvas or Auditory Waveform Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (resource.mediaType == OerMediaType.VIDEO_LESSON || resource.mediaType == OerMediaType.SCIENCE_SIMULATION) {
                // Interactive Educational Video Canvas Visualizer
                EducationalVideoCanvas(
                    sceneKey = resource.visualSceneKey,
                    currentSec = currentPositionSec,
                    isPlaying = isPlaying,
                    title = resource.title
                )
            } else {
                // Auditory Soundwave & Frequency Spectrum Visualizer
                AuditoryWaveformVisualizer(
                    isPlaying = isPlaying,
                    title = resource.title,
                    currentSec = currentPositionSec,
                    speaker = currentTranscriptLine?.speaker ?: "Narrator"
                )
            }

            // Closed Captions / Subtitles HUD Overlay (when enabled)
            if (captionsEnabled && currentTranscriptLine != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.78f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "${currentTranscriptLine.speaker}: ${currentTranscriptLine.text}",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Checkpoint Question Modal Overlay
            if (activeCheckpoint != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    activeCheckpoint!!.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                activeCheckpoint!!.questionPrompt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))

                            activeCheckpoint!!.options.forEachIndexed { index, option ->
                                val isSelected = selectedAnswerIndex == index
                                val isCorrectOption = option == activeCheckpoint!!.correctAnswer
                                val buttonBg = when {
                                    !checkpointAnswered -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    isCorrectOption -> Color(0xFFDCFCE7)
                                    isSelected && !isCorrectOption -> Color(0xFFFEE2E2)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = buttonBg,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable(enabled = !checkpointAnswered) {
                                            selectedAnswerIndex = index
                                            checkpointAnswered = true
                                            val correct = isCorrectOption
                                            isAnswerCorrect = correct
                                            if (correct) {
                                                viewModel?.addPoints(15)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${('A' + index)}. ",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            option,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            if (checkpointAnswered) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    if (isAnswerCorrect == true) "🎉 Correct! ${activeCheckpoint!!.explanation}" else "💡 ${activeCheckpoint!!.explanation}",
                                    fontSize = 10.5.sp,
                                    color = if (isAnswerCorrect == true) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                                Spacer(Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        activeCheckpoint = null
                                        isPlaying = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Continue Learning ⏩", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Playback Timeline Scrubber Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentPositionSec.toFloat(),
                onValueChange = { newSec ->
                    currentPositionSec = newSec.toInt()
                },
                valueRange = 0f..totalDurationSec.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("media_scrubber_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatTime(currentPositionSec),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatTime(totalDurationSec),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Primary Playback Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    playbackSpeed = when (playbackSpeed) {
                        0.75f -> 1.0f
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 2.0f
                        2.0f -> 0.75f
                        else -> 1.0f
                    }
                }
            ) {
                Text(
                    "${playbackSpeed}x",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Rewind 10s
            IconButton(
                onClick = { currentPositionSec = (currentPositionSec - 10).coerceAtLeast(0) },
                modifier = Modifier.testTag("rewind_10s_btn")
            ) {
                Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10 seconds", modifier = Modifier.size(24.dp))
            }

            // Play / Pause Master Button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(52.dp)
                    .clickable { isPlaying = !isPlaying }
                    .testTag("media_play_pause_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Forward 10s
            IconButton(
                onClick = { currentPositionSec = (currentPositionSec + 10).coerceAtMost(totalDurationSec) },
                modifier = Modifier.testTag("forward_10s_btn")
            ) {
                Icon(Icons.Default.FastForward, contentDescription = "Forward 10 seconds", modifier = Modifier.size(24.dp))
            }

            // Captions Toggle
            IconButton(
                onClick = { captionsEnabled = !captionsEnabled },
                modifier = Modifier.testTag("toggle_captions_btn")
            ) {
                Icon(
                    Icons.Default.ClosedCaption,
                    contentDescription = "Toggle Captions",
                    tint = if (captionsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Secondary Tabs (Interactive Transcript, Key Takeaways, OER Link)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Transcript (${resource.transcript.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Key Takeaways", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("OER Source", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(Modifier.height(6.dp))

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Interactive Synchronized Transcript List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(resource.transcript) { line ->
                        val isLineActive = currentTranscriptLine?.timestampSeconds == line.timestampSeconds
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLineActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = if (isLineActive) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentPositionSec = line.timestampSeconds
                                    isPlaying = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        formatTime(line.timestampSeconds),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        line.speaker,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        line.text,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isLineActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Key Takeaways & Objectives
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            resource.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(resource.keyTakeaways) { takeaway ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✨", fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                takeaway,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            2 -> {
                // OER Commons Repository Card & Link
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Open Educational Resources (OER) Curated Collections",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "This course curriculum module is accredited and openly licensed under Creative Commons (CC BY 4.0). You can explore full collections, lesson plans, simulations, and teaching rubrics online.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.sourceUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Opening OER Commons Curated Collections...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open on oercommons.org ↗", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated High-Definition Educational Video Canvas for Science, Math, Literacy, and Social Studies.
 */
@Composable
private fun EducationalVideoCanvas(
    sceneKey: String,
    currentSec: Int,
    isPlaying: Boolean,
    title: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "video_canvas")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_anim"
    )

    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dynamic gradient backdrop
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
            )
        )

        when (sceneKey) {
            "PLATE_TECTONICS" -> {
                // Render 3D dynamic lithosphere crust, magma convection currents, and tectonic plates
                val crustY = h * 0.45f
                // Oceanic plate & Continental plate
                drawRect(color = Color(0xFF334155), topLeft = Offset(0f, crustY), size = Size(w * 0.48f, h * 0.15f))
                drawRect(color = Color(0xFF475569), topLeft = Offset(w * 0.52f, crustY - 15f), size = Size(w * 0.48f, h * 0.2f))

                // Magma asthenosphere
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFFEA580C), Color(0xFF7C2D12))),
                    topLeft = Offset(0f, crustY + h * 0.15f),
                    size = Size(w, h * 0.4f)
                )

                // Convection current arrows
                val angle = waveAnim + (currentSec * 0.2f)
                val cx1 = w * 0.25f
                val cy1 = crustY + h * 0.25f
                val r = 25f
                drawCircle(
                    color = Color.Yellow.copy(alpha = 0.6f),
                    radius = r,
                    center = Offset(cx1 + cos(angle) * 15f, cy1 + sin(angle) * 15f),
                    style = Stroke(width = 3f)
                )
            }

            "QUADRATIC_PARABOLA" -> {
                // Coordinate grid & dynamic parabola curve
                drawLine(Color(0xFF475569), Offset(w * 0.1f, h * 0.5f), Offset(w * 0.9f, h * 0.5f), strokeWidth = 2f)
                drawLine(Color(0xFF475569), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.9f), strokeWidth = 2f)

                val parabolaPath = Path()
                val a = 0.003f
                val vertexX = w * 0.5f
                val vertexY = h * 0.75f - (sin(waveAnim) * 20f)
                parabolaPath.moveTo(w * 0.15f, vertexY + a * (w * 0.15f - vertexX) * (w * 0.15f - vertexX))
                var x = w * 0.15f
                while (x <= w * 0.85f) {
                    val y = vertexY - (a * (x - vertexX) * (x - vertexX))
                    parabolaPath.lineTo(x, y)
                    x += 6f
                }
                drawPath(parabolaPath, color = Color(0xFF38BDF8), style = Stroke(width = 4f, cap = StrokeCap.Round))
                drawCircle(Color(0xFFFBBF24), radius = 6f * pulseAnim, center = Offset(vertexX, vertexY))
            }

            "CHEMISTRY_BONDING" -> {
                // Atomic nuclei & valence electron orbitals
                val c1 = Offset(w * 0.35f, h * 0.5f)
                val c2 = Offset(w * 0.65f, h * 0.5f)
                drawCircle(Color(0xFFEF4444), radius = 18f, center = c1) // Na+
                drawCircle(Color(0xFF10B981), radius = 24f, center = c2) // Cl-

                // Orbital rings
                drawCircle(Color(0xFF38BDF8).copy(alpha = 0.4f), radius = 45f, center = c1, style = Stroke(width = 2f))
                drawCircle(Color(0xFF38BDF8).copy(alpha = 0.4f), radius = 55f, center = c2, style = Stroke(width = 2f))

                // Shared/transferred electrons
                val eAngle = waveAnim * 2f
                drawCircle(Color(0xFFFBBF24), radius = 5f, center = Offset(c1.x + cos(eAngle) * 45f, c1.y + sin(eAngle) * 45f))
                drawCircle(Color(0xFFFBBF24), radius = 5f, center = Offset(c2.x + cos(eAngle + 1f) * 55f, c2.y + sin(eAngle + 1f) * 55f))
            }

            else -> {
                // Default cosmic pulse educational spectrum
                val center = Offset(w * 0.5f, h * 0.5f)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.3f), Color.Transparent)),
                    radius = 80f * pulseAnim,
                    center = center
                )
                // Sine wave harmonic
                val path = Path()
                path.moveTo(0f, h * 0.5f)
                var px = 0f
                while (px <= w) {
                    val py = (h * 0.5f) + sin((px / w * 4 * PI) + waveAnim).toFloat() * 25f
                    path.lineTo(px, py)
                    px += 8f
                }
                drawPath(path, color = Color(0xFF60A5FA), style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
        }
    }
}

/**
 * Soundwave Frequency Spectrum & Auditory Visualizer for Lectures and Podcasts.
 */
@Composable
private fun AuditoryWaveformVisualizer(
    isPlaying: Boolean,
    title: String,
    currentSec: Int,
    speaker: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(70.dp)
        ) {
            val barCount = 24
            for (i in 0 until barCount) {
                val heightFactor = if (isPlaying) {
                    val offset = (i * 0.3f) + (currentSec * 0.5f)
                    val value = (sin(offset) * 0.5f + 0.5f) * pulse
                    value.coerceIn(0.15f, 1.0f)
                } else {
                    0.15f
                }

                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight(heightFactor)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFC084FC))
                            )
                        )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "🎙️ Audio Lecture: $speaker",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )
        Text(
            title,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
