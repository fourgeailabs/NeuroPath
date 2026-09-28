package com.fourgeailabs.neuropath.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fourgeailabs.neuropath.util.rememberReducedMotion
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.components.EducationalChatInterface
import com.fourgeailabs.neuropath.ui.components.ModelLoadingScreen
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf
import com.fourgeailabs.neuropath.ui.theme.ElevationTokens
import com.fourgeailabs.neuropath.ui.theme.ShapeTokens
import com.fourgeailabs.neuropath.ui.theme.SpacingTokens
import androidx.compose.foundation.layout.sizeIn

/**
 * Screen hosting the Educational Learning Buddy.
 * Seamlessly integrates the EducationalChatInterface (with Llama 3.2 3B, Explanation Modes,
 * Subject Filters, Room Message History, and Transformers) and the Live Voice Conversation Stage.
 */
@Composable
fun NeuroBuddyChatScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val isVoiceMode by viewModel.isVoiceConversationMode.collectAsState()
    val isModelLoading by viewModel.isModelLoading.collectAsState()

    // Ensure the on-device model is ready; shows the typewriter loading screen
    // while a cold load runs (parent chose local AI and the model isn't warm).
    LaunchedEffect(Unit) { viewModel.ensureLocalModelReady() }

    if (isModelLoading) {
        val theme = viewModel.getActiveTheme()
        val loadProgress by viewModel.modelLoadProgress.collectAsState()
        val loadStage by viewModel.modelLoadStage.collectAsState()
        val loadError by viewModel.modelLoadError.collectAsState()
        ModelLoadingScreen(
            buddyName = theme.buddyName,
            themeEmoji = theme.emoji,
            themeId = theme.id,
            progress = loadProgress,
            stage = loadStage.ifBlank { "Warming up…" },
            onSkip = { viewModel.skipModelLoading() },
            loadError = loadError,
            onRetry = { viewModel.retryModelLoad() },
            modifier = modifier
        )
        return
    }

    if (isVoiceMode) {
        // Live Voice Conversation Stage
        VoiceConversationScreenWrapper(
            viewModel = viewModel,
            onExitVoice = { viewModel.toggleVoiceConversationMode(false) },
            onBack = { viewModel.navigateTo(AppScreen.HOME) },
            modifier = modifier
        )
    } else {
        // Rich Educational Chat Interface
        EducationalChatInterface(
            viewModel = viewModel,
            onBack = { viewModel.navigateTo(AppScreen.HOME) },
            modifier = modifier
        )
    }
}

@Composable
private fun VoiceConversationScreenWrapper(
    viewModel: NeuroPathViewModel,
    onExitVoice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = viewModel.getActiveTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(SpacingTokens.xl),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("chat_back_btn")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("back"))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(theme.emoji, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(SpacingTokens.s))
                Column {
                    Text(
                        theme.buddyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        t("live_voice_mode"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Switch back to Chat Interface
            Surface(
                shape = ShapeTokens.ExtraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                    .clickable { onExitVoice() }
                    .testTag("toggle_voice_mode_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = t("switch_to_text_chat"),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(SpacingTokens.xl)
                    )
                    Spacer(Modifier.width(SpacingTokens.xxs))
                    Text(
                        t("chat_view"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Voice Stage Visualizer & Controls
        VoiceConversationStage(
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun VoiceConversationStage(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val theme = viewModel.getActiveTheme()
    val isLiveActive by viewModel.isLiveVoiceActive.collectAsState()
    val liveStatus by viewModel.liveVoiceStatus.collectAsState()
    val liveTranscript by viewModel.liveVoiceTranscript.collectAsState()
    val isRecording by viewModel.isRecordingAudio.collectAsState()
    val isTranscribing by viewModel.isTranscribingAudio.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()

    val reducedMotion = rememberReducedMotion()
    val pulseScale = if (reducedMotion) {
        1.0f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isLiveActive || isRecording) 1.25f else 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        ).value
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Voice Mode Header Badge
        Surface(
            shape = ShapeTokens.Large,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = SpacingTokens.l, vertical = SpacingTokens.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = t("live_api"),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(SpacingTokens.xl)
                )
                Spacer(Modifier.width(SpacingTokens.xs))
                Text(
                    t("live_voice_conversation_llama_3_2_3b"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Center Pulsing Avatar Visualizer
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glowing outer ripple
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            // Inner core avatar orb
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = ElevationTokens.Level5,
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(theme.emoji, fontSize = 48.sp)
                }
            }
        }

        // Live Status Text & Real-time Transcript Box
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                liveStatus,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(SpacingTokens.s))

            if (!liveTranscript.isNullOrBlank()) {
                ElevatedCard(
                    shape = ShapeTokens.Large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.s)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t("live_transcript"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.weight(1f))
                            Text(tf("curriculum", profile.stateStandard), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(SpacingTokens.xs))
                        Text(
                            liveTranscript ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Push-to-Talk Big Action Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = SpacingTokens.l)
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isRecording -> Color(0xFFE53935)
                    isLiveActive || isTranscribing -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                shadowElevation = ElevationTokens.Fab,
                modifier = Modifier
                    .size(76.dp)
                    .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                    .clickable {
                        if (isRecording) {
                            viewModel.stopAudioRecordingAndTranscribe { spokenText ->
                                viewModel.sendLiveVoiceTurn(rawText = spokenText)
                            }
                        } else if (!isLiveActive && !isTranscribing) {
                            viewModel.startAudioRecording()
                        }
                    }
                    .testTag("live_voice_orb_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLiveActive || isTranscribing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(34.dp),
                            strokeWidth = 3.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = t("speak_in_live_voice_mode"),
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(SpacingTokens.s))
            Text(
                if (isRecording) "Tap to send voice turn" else "Tap microphone to speak",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
