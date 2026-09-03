package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AmbientSoundType
import com.example.ui.BreathingPhase
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Visual themes for the 4-7-8 rhythm animation canvas.
 */
enum class BreathingVisualMode(val label: String, val emoji: String, val description: String) {
    BLOSSOM("Lotus Blossom", "🌸", "Blooming multi-layer petals expanding and folding"),
    RIPPLE_WAVES("Fluid Waves", "🌊", "Concentric resonant wave rings with soft glow"),
    COSMIC_ORB("Cosmic Orbit", "✨", "Pulsating stellar diaphragm with revolving starlight beads")
}

/**
 * High-performance, sensory-conscious 4-7-8 Breathing Exercise Visualizer Component.
 * Implements clinical vagus-nerve pacing (4s Inhale, 7s Hold, 8s Exhale) with continuous
 * multi-phase Canvas rendering, customizable visual modes, audio cues, and calming affirmations.
 */
@Composable
fun FourSevenEightBreathingVisualizer(
    currentPhase: BreathingPhase,
    secondsRemaining: Int,
    completedCycles: Int,
    targetCycles: Int = 4,
    isSessionActive: Boolean = true,
    visualMode: BreathingVisualMode = BreathingVisualMode.BLOSSOM,
    onVisualModeChanged: (BreathingVisualMode) -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onResetCycles: () -> Unit = {},
    onSpeakCue: (String) -> Unit = {},
    activeAmbientSound: AmbientSoundType = AmbientSoundType.OFF,
    onSelectAmbientSound: (AmbientSoundType) -> Unit = {},
    enableHaptics: Boolean = true,
    isReducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Phase Progress Fraction calculation
    val totalPhaseSec = currentPhase.durationSec.toFloat()
    val elapsedPhaseSec = (totalPhaseSec - secondsRemaining.toFloat()).coerceAtLeast(0f)
    val phaseProgressFraction = (elapsedPhaseSec / totalPhaseSec).coerceIn(0f, 1f)

    // Overall cycle progress (4s + 7s + 8s = 19s total per cycle)
    val cycleElapsedSec = when (currentPhase) {
        BreathingPhase.INHALE -> elapsedPhaseSec
        BreathingPhase.HOLD -> 4f + elapsedPhaseSec
        BreathingPhase.EXHALE -> 11f + elapsedPhaseSec
    }
    val cycleTotalProgress = (cycleElapsedSec / 19f).coerceIn(0f, 1f)

    // Animated Phase Colors & Palettes
    val targetPrimaryColor = when (currentPhase) {
        BreathingPhase.INHALE -> Color(0xFF0D9488) // Turquoise / Deep Teal
        BreathingPhase.HOLD -> Color(0xFFD97706)   // Warm Amber / Golden Sun
        BreathingPhase.EXHALE -> Color(0xFF7C3AED) // Radiant Violet / Lavender
    }
    val targetSecondaryColor = when (currentPhase) {
        BreathingPhase.INHALE -> Color(0xFF5EEAD4) // Pale Turquoise Glow
        BreathingPhase.HOLD -> Color(0xFFFDE68A)   // Golden Radiance
        BreathingPhase.EXHALE -> Color(0xFFDDD6FE) // Soft Lilac Mist
    }
    val targetDeepColor = when (currentPhase) {
        BreathingPhase.INHALE -> Color(0xFF134E4A)
        BreathingPhase.HOLD -> Color(0xFF78350F)
        BreathingPhase.EXHALE -> Color(0xFF4C1D95)
    }

    val animatedPrimaryColor by animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = tween(800, easing = LinearEasing),
        label = "phasePrimaryColor"
    )
    val animatedSecondaryColor by animateColorAsState(
        targetValue = targetSecondaryColor,
        animationSpec = tween(800, easing = LinearEasing),
        label = "phaseSecondaryColor"
    )
    val animatedDeepColor by animateColorAsState(
        targetValue = targetDeepColor,
        animationSpec = tween(800, easing = LinearEasing),
        label = "phaseDeepColor"
    )

    // Smooth Breathing Scale Transition
    val targetScale = when (currentPhase) {
        BreathingPhase.INHALE -> 1.35f
        BreathingPhase.HOLD -> 1.35f
        BreathingPhase.EXHALE -> 0.82f
    }
    val animatedScale by animateFloatAsState(
        targetValue = if (isReducedMotion) 1.0f else targetScale,
        animationSpec = tween(
            durationMillis = (currentPhase.durationSec * 1000),
            easing = FastOutSlowInEasing
        ),
        label = "breathingScaleAnim"
    )

    // Continuous Infinite Transitions for micro-ripples and rotation
    val infiniteTransition = rememberInfiniteTransition(label = "ambientRipples")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slowRotation"
    )

    // Sensory Affirmations that cycle gently
    val affirmations = remember {
        listOf(
            "🌱 Mind calming, body settling.",
            "🌊 Riding the wave of your breath.",
            "✨ Notice the tension melting from your shoulders.",
            "🕊️ You are safe, present, and centered.",
            "🌿 Inhaling calm clarity, exhaling worry.",
            "💛 Every full cycle resets your nervous system."
        )
    }
    val currentAffirmation = affirmations[(completedCycles) % affirmations.size]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("four_seven_eight_visualizer"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP STATS & RHYTHMIC PHASE BADGE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = animatedPrimaryColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, animatedPrimaryColor.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(animatedPrimaryColor)
                    )
                    Text(
                        text = "4-7-8 VAGUS RHYTHM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedPrimaryColor,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Completed Cycles Counter Badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.testTag("cycle_counter_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Cycles",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$completedCycles / $targetCycles Cycles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 2. SEGMENTED PHASE TIMELINE BAR (4s - 7s - 8s)
        RhythmicSegmentedPhaseBar(
            currentPhase = currentPhase,
            secondsRemaining = secondsRemaining,
            primaryColor = animatedPrimaryColor,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. CORE ANIMATED BREATHING CANVAS (Lotus Blossom / Concentric Waves / Cosmic Orb)
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp)
                .testTag("breathing_canvas_container"),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas: Multi-layer Organic Shapes & Orbit Particles
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (isReducedMotion) 0f else slowRotation)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2.6f) * animatedScale

                when (visualMode) {
                    BreathingVisualMode.BLOSSOM -> {
                        drawLotusBlossom(
                            center = center,
                            radius = baseRadius,
                            primaryColor = animatedPrimaryColor,
                            secondaryColor = animatedSecondaryColor,
                            deepColor = animatedDeepColor,
                            phaseProgress = phaseProgressFraction,
                            ambientScale = ambientPulse
                        )
                    }
                    BreathingVisualMode.RIPPLE_WAVES -> {
                        drawFluidWaveRipples(
                            center = center,
                            radius = baseRadius,
                            primaryColor = animatedPrimaryColor,
                            secondaryColor = animatedSecondaryColor,
                            phase = currentPhase,
                            phaseProgress = phaseProgressFraction,
                            ambientScale = ambientPulse
                        )
                    }
                    BreathingVisualMode.COSMIC_ORB -> {
                        drawCosmicOrbitalSphere(
                            center = center,
                            radius = baseRadius,
                            primaryColor = animatedPrimaryColor,
                            secondaryColor = animatedSecondaryColor,
                            phaseProgress = phaseProgressFraction,
                            ambientScale = ambientPulse
                        )
                    }
                }
            }

            // Outer Smooth Circular Progress Indicator Arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val diameter = size.minDimension - strokeWidth * 2.5f
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

                // Track Background
                drawArc(
                    color = animatedPrimaryColor.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Phase Progress Sweep
                val sweep = 360f * (1f - (secondsRemaining.toFloat() / currentPhase.durationSec.toFloat())).coerceIn(0.01f, 1f)
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            animatedSecondaryColor,
                            animatedPrimaryColor,
                            animatedDeepColor,
                            animatedSecondaryColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth + 2f, cap = StrokeCap.Round)
                )

                // Orbiting Bead on the progress tip
                val angleRad = ((-90f + sweep) * PI / 180f).toFloat()
                val radius = diameter / 2f
                val beadCenter = Offset(
                    x = size.width / 2f + radius * cos(angleRad),
                    y = size.height / 2f + radius * sin(angleRad)
                )

                // Glowing halo behind bead
                drawCircle(
                    color = animatedSecondaryColor.copy(alpha = 0.6f),
                    radius = 9.dp.toPx(),
                    center = beadCenter
                )
                // Solid core bead
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = beadCenter
                )
            }

            // Central Diaphragm HUD Core with Countdown & Action Label
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, animatedPrimaryColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(150.dp)
                    .testTag("breathing_center_core")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Countdown Number
                    Text(
                        text = "$secondsRemaining",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = animatedPrimaryColor,
                        lineHeight = 48.sp
                    )

                    // Phase Title
                    Text(
                        text = currentPhase.label.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.1.sp,
                        textAlign = TextAlign.Center
                    )

                    // Phase Duration Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = animatedPrimaryColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "${currentPhase.durationSec}s Phase",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedPrimaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 4. GUIDANCE INSTRUCTION & SENSORY AFFIRMATION CARD
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentPhase.instruction,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = animatedPrimaryColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = currentAffirmation,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Vagus Nerve Calm Meter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Calm Regulation Progress",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val calmScore = ((completedCycles.toFloat() / targetCycles.coerceAtLeast(1).toFloat()) * 100).toInt().coerceAtMost(100)
                        Text(
                            text = "$calmScore%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = animatedPrimaryColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (completedCycles.toFloat() / targetCycles.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                        color = animatedPrimaryColor,
                        trackColor = animatedPrimaryColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // 5. VISUAL MODE SELECTOR TABS
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BreathingVisualMode.values().forEach { mode ->
                    val isSelected = visualMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) animatedPrimaryColor else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onVisualModeChanged(mode) }
                            .testTag("mode_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mode.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = mode.label.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. AMBIENT SOUNDSCAPE QUICK BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Ambient Sound",
                    tint = animatedPrimaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Background Soundscape",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Quick ambient sound toggle pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    AmbientSoundType.OFF,
                    AmbientSoundType.OCEAN,
                    AmbientSoundType.RAIN,
                    AmbientSoundType.FOREST
                ).forEach { sound ->
                    val isPlaying = activeAmbientSound == sound
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPlaying) animatedPrimaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectAmbientSound(sound) }
                            .testTag("ambient_sound_${sound.name.lowercase()}")
                    ) {
                        Text(
                            text = sound.emoji,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 7. SESSION CONTROLS (Pause/Resume, Reset, Speech Cue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onTogglePlayPause,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("breathing_play_pause_btn")
            ) {
                Icon(
                    imageVector = if (isSessionActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isSessionActive) "Pause" else "Resume",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isSessionActive) "Pause Rhythm" else "Resume",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onResetCycles,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("breathing_reset_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Cycles",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Segmented 4s Inhale - 7s Hold - 8s Exhale horizontal rhythm progression bar.
 */
@Composable
fun RhythmicSegmentedPhaseBar(
    currentPhase: BreathingPhase,
    secondsRemaining: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            BreathingPhase.INHALE to "4s Inhale",
            BreathingPhase.HOLD to "7s Hold",
            BreathingPhase.EXHALE to "8s Exhale"
        ).forEach { (phase, label) ->
            val isActive = currentPhase == phase
            val phaseFraction = if (isActive) {
                (1f - (secondsRemaining.toFloat() / phase.durationSec.toFloat())).coerceIn(0f, 1f)
            } else if (currentPhase.ordinal > phase.ordinal) {
                1f
            } else {
                0f
            }

            Box(
                modifier = Modifier
                    .weight(phase.durationSec.toFloat())
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isActive) primaryColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                // Progress fill for active segment
                if (phaseFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(phaseFraction)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isActive) primaryColor.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isActive && phaseFraction > 0.4f) Color.White
                        else if (isActive) primaryColor
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Visual Mode 1: Drawing blooming multi-petal lotus blossom on Canvas.
 */
private fun DrawScope.drawLotusBlossom(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    deepColor: Color,
    phaseProgress: Float,
    ambientScale: Float
) {
    val petalCount = 8
    val petalRadius = radius * 0.75f * ambientScale

    // Draw Outer Petal Layer
    for (i in 0 until petalCount) {
        val angle = (i * (360f / petalCount) + (phaseProgress * 15f)) * PI / 180.0
        val petalCenterX = center.x + (petalRadius * 0.45f * cos(angle)).toFloat()
        val petalCenterY = center.y + (petalRadius * 0.45f * sin(angle)).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.45f),
                    primaryColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(petalCenterX, petalCenterY),
                radius = petalRadius * 0.65f
            ),
            radius = petalRadius * 0.65f,
            center = Offset(petalCenterX, petalCenterY)
        )
    }

    // Draw Inner Blooming Petal Ring
    val innerPetalCount = 6
    val innerRadius = petalRadius * 0.55f
    for (i in 0 until innerPetalCount) {
        val angle = (i * (360f / innerPetalCount) - (phaseProgress * 20f)) * PI / 180.0
        val pX = center.x + (innerRadius * 0.4f * cos(angle)).toFloat()
        val pY = center.y + (innerRadius * 0.4f * sin(angle)).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.5f),
                    secondaryColor.copy(alpha = 0.5f),
                    deepColor.copy(alpha = 0.2f)
                ),
                center = Offset(pX, pY),
                radius = innerRadius * 0.5f
            ),
            radius = innerRadius * 0.5f,
            center = Offset(pX, pY)
        )
    }

    // Ambient floating nectar sparkle particles
    val random = Random(42)
    for (p in 0..12) {
        val pAngle = random.nextDouble(0.0, 2 * PI)
        val pDist = random.nextDouble(petalRadius * 0.3, petalRadius * 1.1)
        val sparkleX = center.x + (pDist * cos(pAngle)).toFloat()
        val sparkleY = center.y + (pDist * sin(pAngle)).toFloat()

        drawCircle(
            color = Color.White.copy(alpha = 0.4f + 0.4f * sin((phaseProgress * 6.28f) + p).toFloat().coerceIn(0f, 0.6f)),
            radius = random.nextDouble(2.0, 4.5).toFloat(),
            center = Offset(sparkleX, sparkleY)
        )
    }
}

/**
 * Visual Mode 2: Concentric fluid wave ripples with dynamic amplitude.
 */
private fun DrawScope.drawFluidWaveRipples(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    phase: BreathingPhase,
    phaseProgress: Float,
    ambientScale: Float
) {
    val ringCount = 5
    for (r in 1..ringCount) {
        val currentRingRadius = (radius * (r.toFloat() / ringCount.toFloat())) * ambientScale
        val alpha = (1f - (r.toFloat() / (ringCount + 1).toFloat())) * 0.45f

        drawCircle(
            color = primaryColor.copy(alpha = alpha),
            radius = currentRingRadius,
            center = center,
            style = Stroke(
                width = (4f - (r * 0.5f)).coerceAtLeast(1.5f),
                cap = StrokeCap.Round
            )
        )

        // Soft Radial gradient aura for inner rings
        if (r <= 3) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.2f),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRingRadius
                ),
                radius = currentRingRadius,
                center = center
            )
        }
    }
}

/**
 * Visual Mode 3: Cosmic orbital sphere with revolving energy beads.
 */
private fun DrawScope.drawCosmicOrbitalSphere(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    phaseProgress: Float,
    ambientScale: Float
) {
    // Central Celestial Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                secondaryColor.copy(alpha = 0.5f),
                primaryColor.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.85f * ambientScale
        ),
        radius = radius * 0.85f * ambientScale,
        center = center
    )

    // Orbit Ring
    val orbitRadius = radius * 0.95f
    drawCircle(
        color = secondaryColor.copy(alpha = 0.35f),
        radius = orbitRadius,
        center = center,
        style = Stroke(width = 2f)
    )

    // Revolving Starlight Planet Beads
    val beadCount = 6
    for (b in 0 until beadCount) {
        val angle = (b * (360f / beadCount) + (phaseProgress * 360f)) * PI / 180.0
        val bX = center.x + (orbitRadius * cos(angle)).toFloat()
        val bY = center.y + (orbitRadius * sin(angle)).toFloat()

        drawCircle(
            color = Color.White,
            radius = 4.5f,
            center = Offset(bX, bY)
        )
        drawCircle(
            color = secondaryColor.copy(alpha = 0.5f),
            radius = 8.5f,
            center = Offset(bX, bY)
        )
    }
}
