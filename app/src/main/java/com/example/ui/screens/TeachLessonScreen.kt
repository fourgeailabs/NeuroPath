package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.curriculum.oer.OerMediaResource
import com.example.data.curriculum.oer.OerMediaType
import com.example.data.curriculum.oer.PreinstalledOerCurriculumCatalog
import com.example.data.curriculum.oer.PreinstalledOerMediaCatalog
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.ui.components.HighlightedSpeechText
import com.example.ui.components.OerMultimediaPlayerBottomSheet

@Composable
fun TeachLessonScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val activeLesson by viewModel.activeLesson.collectAsState()
    val currentStepIndex by viewModel.currentTeachStep.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()
    val theme = viewModel.getActiveTheme()

    var activeMediaResource by remember { mutableStateOf<OerMediaResource?>(null) }

    if (activeLesson == null) {
        viewModel.navigateTo(AppScreen.HOME)
        return
    }

    val lesson = activeLesson!!
    val totalSteps = lesson.teachSteps.size
    val currentStep = lesson.teachSteps.getOrNull(currentStepIndex) ?: return

    var interactiveSelectedChoice by remember(currentStepIndex) { mutableStateOf<Int?>(null) }
    var interactiveSubmitted by remember(currentStepIndex) { mutableStateOf(false) }

    val oerMediaList = remember(lesson) {
        val direct = PreinstalledOerMediaCatalog.getMediaForUnit(lesson.id, lesson.subject)
        if (direct.isNotEmpty()) direct else {
            // Generate contextual media resources for this lesson
            listOf(
                OerMediaResource(
                    id = "oer_vid_${lesson.id}",
                    title = "${lesson.title} - Video Lesson",
                    mediaType = OerMediaType.VIDEO_LESSON,
                    durationSeconds = 150,
                    description = "Accredited OER Commons video walkthrough covering '${lesson.title}' (${lesson.stateStandardCode}).",
                    creatorOrSource = "OER Commons Curated Video Lab",
                    visualSceneKey = when (lesson.subject) {
                        com.example.data.model.EducationalSubject.SCIENCE -> "PLATE_TECTONICS"
                        com.example.data.model.EducationalSubject.MATH -> "QUADRATIC_PARABOLA"
                        else -> "DEFAULT"
                    },
                    transcript = lesson.teachSteps.mapIndexed { idx, step ->
                        com.example.data.curriculum.oer.OerTranscriptLine(
                            timestampSeconds = idx * 30,
                            speaker = "Instructor",
                            text = "${step.title}: ${step.text}"
                        )
                    },
                    keyTakeaways = listOf(
                        "Standard: ${lesson.stateStandardCode}",
                        lesson.standardDescription,
                        "Apply interactive reasoning in everyday life."
                    )
                ),
                OerMediaResource(
                    id = "oer_audio_${lesson.id}",
                    title = "${lesson.title} - Audio Lecture",
                    mediaType = OerMediaType.AUDIO_LECTURE,
                    durationSeconds = 120,
                    description = "Auditory curriculum explainer for '${lesson.title}' with acoustic pacing and transcript.",
                    creatorOrSource = "OER Commons Auditory Series",
                    transcript = lesson.teachSteps.mapIndexed { idx, step ->
                        com.example.data.curriculum.oer.OerTranscriptLine(
                            timestampSeconds = idx * 25,
                            speaker = "Audio Narrator",
                            text = "${step.title}. ${step.text} Fun Fact: ${step.tipOrFunFact}"
                        )
                    },
                    keyTakeaways = listOf(
                        "Listen and follow along with closed captions.",
                        "Neuro-affirming bite-sized conceptual chunks."
                    )
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.testTag("teach_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Concept Discovery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Step ${currentStepIndex + 1} of $totalSteps",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Skip straight to 20-Q Adaptive Test
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clickable { viewModel.startMasteryJourney() }
                    .testTag("jump_to_assessment_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("20 Q's", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Assessment",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentStepIndex + 1).toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Lesson Title & Standard Banner
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(lesson.subject.emoji, fontSize = 24.sp)
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Standard: ${lesson.stateStandardCode} • ${lesson.standardDescription}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                // OER Commons Curated Video & Audio Media Lab Row
                if (oerMediaList.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "OER Multimedia Learning Lab",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val videoItem = oerMediaList.find { it.mediaType == OerMediaType.VIDEO_LESSON }
                                val audioItem = oerMediaList.find { it.mediaType == OerMediaType.AUDIO_LECTURE }

                                if (videoItem != null) {
                                    Button(
                                        onClick = { activeMediaResource = videoItem },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f).testTag("watch_video_lesson_btn")
                                    ) {
                                        Text("Watch Video 🎬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (audioItem != null) {
                                    OutlinedButton(
                                        onClick = { activeMediaResource = audioItem },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f).testTag("listen_audio_lecture_btn")
                                    ) {
                                        Text("Audio Lecture 🎧", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step Card with Visual Emoji Aid and Karaoke Highlighted TTS
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Step Title & TTS Speaker Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        currentStep.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Visual Representation Stage
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentStep.visualEmoji,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Main Explanation with Karaoke Highlighting
                HighlightedSpeechText(
                    text = currentStep.text,
                    speechManager = viewModel.speechManager,
                    utteranceKey = "teach_step_${currentStep.stepNumber}",
                    fontSize = 18,
                    isDyslexiaEnabled = profile.dyslexiaFontEnabled
                )

                Spacer(Modifier.height(16.dp))

                // Neuro-Tip / Fun Fact Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tip",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            currentStep.tipOrFunFact,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Get AI Help Button
                Button(
                    onClick = { viewModel.getAiHelpForCurrentTeachStep() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(theme.emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("I'm confused, Learning Buddy help!", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Interactive Practice Check-In (if present)
        if (currentStep.interactivePrompt != null && currentStep.interactiveAnswers != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "✨ Quick Interactive Practice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        currentStep.interactivePrompt,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))

                    currentStep.interactiveAnswers.forEachIndexed { idx, ans ->
                        val isChosen = interactiveSelectedChoice == idx
                        val isCorrect = idx == currentStep.interactiveCorrectIndex
                        val btnColor = when {
                            !interactiveSubmitted && isChosen -> MaterialTheme.colorScheme.primaryContainer
                            interactiveSubmitted && isCorrect -> Color(0xFFD4EDDA)
                            interactiveSubmitted && isChosen && !isCorrect -> Color(0xFFF8D7DA)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = btnColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (!interactiveSubmitted) {
                                        interactiveSelectedChoice = idx
                                        interactiveSubmitted = true
                                        if (isCorrect) {
                                            viewModel.speechManager.speak("Spot on! That's correct.")
                                        } else {
                                            viewModel.speechManager.speak("Nice try! Look closely at the clues above.")
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${('A' + idx)}.",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(ans, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Navigation Buttons (Prev / Next or Start 20-Q Journey)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStepIndex > 0) {
                OutlinedButton(
                    onClick = { viewModel.prevTeachStep() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("teach_prev_step_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step")
                    Spacer(Modifier.width(6.dp))
                    Text("Previous")
                }
            } else {
                Spacer(Modifier.width(10.dp))
            }

            Button(
                onClick = { viewModel.nextTeachStep() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("teach_next_step_btn")
            ) {
                Text(
                    if (currentStepIndex < totalSteps - 1) "Next Concept" else "Start 20-Q Journey 🚀",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentStepIndex < totalSteps - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.PlayArrow,
                    contentDescription = "Next"
                )
            }
        }

        if (activeMediaResource != null) {
            OerMultimediaPlayerBottomSheet(
                resource = activeMediaResource!!,
                onDismiss = { activeMediaResource = null },
                viewModel = viewModel
            )
        }
    }
}
