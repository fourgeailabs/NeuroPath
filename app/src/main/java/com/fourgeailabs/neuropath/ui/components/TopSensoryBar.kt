package com.fourgeailabs.neuropath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.audio.AmbientSoundType
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.components.BuddyAvatar
import com.fourgeailabs.neuropath.ui.components.BuddyPose
import androidx.compose.foundation.layout.sizeIn
import com.fourgeailabs.neuropath.ui.theme.ElevationTokens
import com.fourgeailabs.neuropath.ui.theme.ShapeTokens
import com.fourgeailabs.neuropath.ui.theme.SpacingTokens

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopSensoryBar(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val activeSound by viewModel.soundManager.activeSound.collectAsState()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsState()

    var showSoundDialog by remember { mutableStateOf(false) }
    var showContrastDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = ElevationTokens.Card,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xs)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.s),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.s)
            ) {
                // Sensory Badge
                Surface(
                    shape = ShapeTokens.Large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = SpacingTokens.xxs)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BuddyAvatar(themeId = "wildlife", pose = BuddyPose.IDLE, size = SpacingTokens.xxl)
                        Spacer(Modifier.width(SpacingTokens.xxs))
                        Text(
                            t("sensory_suite"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 1. Pop-It Fidget Quick Launch
                SensoryPillButton(
                    buddyThemeId = "games",
                    buddyPose = BuddyPose.HAPPY,
                    label = t("sensory_popit"),
                    onClick = {
                        viewModel.recordSensoryBreakTaken()
                        viewModel.navigateTo(AppScreen.FIDGET_POPIT)
                    },
                    testTag = "sensory_popit_pill"
                )

                // 2. 4-7-8 Breathing Quick Launch
                SensoryPillButton(
                    buddyThemeId = "wildlife",
                    buddyPose = BuddyPose.COMFORTING,
                    label = t("sensory_breathing_calm"),
                    onClick = {
                        viewModel.recordSensoryBreakTaken()
                        viewModel.navigateTo(AppScreen.BREATHING_GUIDE)
                    },
                    testTag = "sensory_breathing_pill"
                )

                // 3. Ambient Soundscapes Picker (procedural, on-device)
                val isMusicActive = activeSound != AmbientSoundType.OFF
                SensoryPillButton(
                    icon = if (isMusicActive) activeSound.emoji else "🔇",
                    label = if (isMusicActive) activeSound.title else t("sensory_soundscapes"),
                    isActive = isMusicActive,
                    onClick = { showSoundDialog = true },
                    testTag = "sensory_soundscape_pill"
                )

                // 4. Dyslexia Font Toggle
                SensoryPillButton(
                    icon = "📖",
                    label = if (profile.dyslexiaFontEnabled) t("dyslexic_on") else t("dyslexic_off"),
                    isActive = profile.dyslexiaFontEnabled,
                    onClick = {
                        viewModel.updateProfileSettings(
                            name = profile.name,
                            gradeLevel = profile.gradeLevel,
                            stateStandard = profile.stateStandard,
                            themeId = profile.activeThemeId,
                            neuroTypes = profile.neurodivergentTypesCsv,
                            dyslexiaFont = !profile.dyslexiaFontEnabled,
                            contrastMode = profile.highContrastMode,
                            ttsSpeed = profile.ttsSpeed,
                            readAloud = profile.readAnswersAloud,
                            dailyMinutes = profile.dailyGoalMinutes
                        )
                    },
                    testTag = "sensory_dyslexia_toggle"
                )

                // 5. Contrast Palette Selector
                SensoryPillButton(
                    buddyThemeId = "magic",
                    buddyPose = BuddyPose.WAVING,
                    label = t("sensory_theme_mode"),
                    onClick = { showContrastDialog = true },
                    testTag = "sensory_contrast_picker"
                )

                // 6. Stop TTS if currently speaking
                if (isSpeaking) {
                    Surface(
                        shape = ShapeTokens.Large,
                        color = Color(0xFFEF5350),
                        modifier = Modifier
                            .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                            .clickable { viewModel.speechManager.stop() }
                            .padding(horizontal = SpacingTokens.xxs)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                                contentDescription = t("stop_speech"),
                                tint = Color.White,
                                modifier = Modifier.size(SpacingTokens.xl)
                            )
                            Spacer(Modifier.width(SpacingTokens.xxs))
                            Text(t("stop_audio_btn"), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Calm Soundscapes picker — every sound is synthesized procedurally on-device.
    // No downloads, no AI generation, no accounts.
    if (showSoundDialog) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showSoundDialog = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.xxl),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.l)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("calm_soundscapes"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { showSoundDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = t("close"))
                    }
                }

                Text(
                    t("soothing_background_sounds_synthesized_live_on_y"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AmbientSoundType.entries.filter { it != AmbientSoundType.OFF }.forEach { sound ->
                    val isPlaying = activeSound == sound
                    Surface(
                        shape = ShapeTokens.Medium,
                        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.toggleAmbientSound(sound)
                                if (!isPlaying) showSoundDialog = false
                            }
                            .testTag("soundscape_pick_${sound.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = SpacingTokens.l),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sound.emoji, fontSize = 22.sp)
                            Spacer(Modifier.width(SpacingTokens.l))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sound.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    sound.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isPlaying) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = t("playing_tap_to_stop"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(SpacingTokens.xxl)
                                )
                            }
                        }
                    }
                }

                if (activeSound != AmbientSoundType.OFF) {
                    OutlinedButton(
                        onClick = { viewModel.toggleAmbientSound(activeSound) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeTokens.Medium
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeMute, contentDescription = null, modifier = Modifier.size(SpacingTokens.xl))
                        Spacer(Modifier.width(SpacingTokens.s))
                        Text(t("stop_soundscape"))
                    }
                }

                Spacer(Modifier.height(SpacingTokens.xxxl))
            }
        }
    }

    // Contrast Palette Dialog
    if (showContrastDialog) {
        AlertDialog(
            onDismissRequest = { showContrastDialog = false },
            title = { Text(t("sensory_color_contrast_palettes"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.s)) {
                    val modes = listOf(
                        ContrastMode("PASTEL", "contrast_pastel_title", "contrast_pastel_desc", "magic"),
                        ContrastMode("BUTTERCREAM", "contrast_buttercream_title", "contrast_buttercream_desc", "trains"),
                        ContrastMode("TWILIGHT_DARK", "contrast_twilight_title", "contrast_twilight_desc", "space"),
                        ContrastMode("MINT", "contrast_mint_title", "contrast_mint_desc", "wildlife"),
                        ContrastMode("HIGH_CONTRAST", "contrast_high_contrast_title", "contrast_high_contrast_desc", "knights")
                    )

                    modes.forEach { mode ->
                        val isSelected = profile.highContrastMode == mode.code
                        Surface(
                            shape = ShapeTokens.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateProfileSettings(
                                        name = profile.name,
                                        gradeLevel = profile.gradeLevel,
                                        stateStandard = profile.stateStandard,
                                        themeId = profile.activeThemeId,
                                        neuroTypes = profile.neurodivergentTypesCsv,
                                        dyslexiaFont = profile.dyslexiaFontEnabled,
                                        contrastMode = mode.code,
                                        ttsSpeed = profile.ttsSpeed,
                                        readAloud = profile.readAnswersAloud,
                                        dailyMinutes = profile.dailyGoalMinutes
                                    )
                                    showContrastDialog = false
                                }
                                .padding(SpacingTokens.xxxs)
                        ) {
                            Row(
                                modifier = Modifier.padding(SpacingTokens.l),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BuddyAvatar(themeId = mode.buddyThemeId, pose = BuddyPose.IDLE, size = 28.dp)
                                Spacer(Modifier.width(SpacingTokens.s))
                                Column {
                                    Text(
                                        t(mode.titleKey),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(t(mode.descKey), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContrastDialog = false }) {
                    Text(t("close"))
                }
            }
        )
    }
}

private data class ContrastMode(
    val code: String,
    val titleKey: String,
    val descKey: String,
    val buddyThemeId: String
)

@Composable
private fun SensoryPillButton(
    icon: String = "",
    buddyThemeId: String? = null,
    buddyPose: BuddyPose = BuddyPose.IDLE,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (isActive) 2.dp else 0.dp,
        modifier = Modifier
            .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (buddyThemeId != null) {
                BuddyAvatar(themeId = buddyThemeId, pose = buddyPose, size = SpacingTokens.xxl)
            } else {
                Text(icon, fontSize = 14.sp)
            }
            Spacer(Modifier.width(SpacingTokens.xs))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
