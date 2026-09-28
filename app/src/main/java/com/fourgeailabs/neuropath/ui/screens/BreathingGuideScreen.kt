package com.fourgeailabs.neuropath.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.audio.AmbientSoundType
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.components.BreathingVisualMode
import com.fourgeailabs.neuropath.ui.components.FourSevenEightBreathingVisualizer
import com.fourgeailabs.neuropath.util.rememberReducedMotion
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf
import com.fourgeailabs.neuropath.ui.components.BuddyAvatar
import com.fourgeailabs.neuropath.ui.components.BuddyPose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingGuideScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val phase by viewModel.breathingPhase.collectAsState()
    val secondsRemaining by viewModel.breathingSecondsRemaining.collectAsState()
    val completedCycles by viewModel.completedBreathCycles.collectAsState()
    val isPaused by viewModel.isBreathingPaused.collectAsState()
    val visualMode by viewModel.breathingVisualMode.collectAsState()
    val activeAmbientSound by viewModel.soundManager.activeSound.collectAsState()
    val activeProfile by viewModel.currentProfile.collectAsState()

    var targetCycleGoal by remember { mutableIntStateOf(4) }
    var showExplanationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BuddyAvatar(themeId = "wildlife", pose = BuddyPose.COMFORTING, size = 34.dp)
                        Column {
                            Text(
                                t("4_7_8_breathing_guide"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("vagus_nerve_sensory_regulation"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("breathing_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("back_to_home")
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExplanationDialog = true },
                        modifier = Modifier.testTag("breathing_info_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = t("how_4_7_8_works"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target Cycles Goal Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("cycle_target"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 4, 8).forEach { count ->
                        val isSelected = targetCycleGoal == count
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("target_cycles_$count")
                        ) {
                            Text(
                                text = tf("cycles", count),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // PRIMARY 4-7-8 BREATHING VISUALIZER COMPONENT
            FourSevenEightBreathingVisualizer(
                currentPhase = phase,
                secondsRemaining = secondsRemaining,
                completedCycles = completedCycles,
                targetCycles = targetCycleGoal,
                isSessionActive = !isPaused,
                visualMode = visualMode,
                onVisualModeChanged = { viewModel.setBreathingVisualMode(it) },
                onTogglePlayPause = { viewModel.toggleBreathingPlayPause() },
                onResetCycles = { viewModel.resetBreathingSession() },
                onSpeakCue = { viewModel.speakBreathingCue(it) },
                activeAmbientSound = activeAmbientSound,
                onSelectAmbientSound = { viewModel.toggleAmbientSound(it) },
                enableHaptics = true,
                isReducedMotion = rememberReducedMotion(),
                modifier = Modifier.fillMaxWidth()
            )

            // Goal Completed Congratulatory Banner
            if (completedCycles >= targetCycleGoal) {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("breathing_goal_completed_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = t("success"),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tf("cycles_complete", targetCycleGoal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = t("your_parasympathetic_nervous_system_has_been_gen"),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Finish & Return to Learning Button
            Button(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("finish_breathing_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = t("breathing_btn"),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = t("i_feel_calmer_now"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Educational Explainer Dialog
    if (showExplanationDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(t("how_4_7_8_breathing_works"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        t("the_4_7_8_technique_is_a_clinically_proven_rhyth"),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(t("1_inhale_4s_oxygenates_the_blood_through_deep_na"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(t("2_hold_7s_allows_oxygen_to_diffuse_into_the_bloo"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(t("3_exhale_8s_stimulates_the_vagus_nerve_immediate"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Text(
                        t("ideal_for_neurodivergent_learners_during_transit"),
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExplanationDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(t("got_it"))
                }
            }
        )
    }
}
