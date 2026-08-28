package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.ui.components.HighlightedSpeechText

@Composable
fun MasteryJourneyScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val activeLesson by viewModel.activeLesson.collectAsState()
    val qIndex by viewModel.journeyQuestionIndex.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val isSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val isCorrect by viewModel.isAnswerCorrect.collectAsState()
    val showErrorCoach by viewModel.showErrorCoach.collectAsState()
    val errorCoachText by viewModel.errorCoachText.collectAsState()
    val correctCount by viewModel.lessonCorrectCount.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()
    val theme = viewModel.getActiveTheme()

    if (activeLesson == null) {
        viewModel.navigateTo(AppScreen.HOME)
        return
    }

    val lesson = activeLesson!!
    val totalQuestions = lesson.questions.size
    val currentQuestion = lesson.questions.getOrNull(qIndex) ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar: Back, Title & Sensory Break Quick Launcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.testTag("journey_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Adaptive Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Question ${qIndex + 1} of $totalQuestions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Sensory Break Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .clickable {
                        viewModel.recordSensoryBreakTaken()
                        viewModel.navigateTo(AppScreen.FIDGET_POPIT)
                    }
                    .testTag("journey_sensory_break_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🫧", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("Break", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        // 20-Node Progress Path Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalQuestions) {
                    val isPast = i < qIndex
                    val isCurrent = i == qIndex
                    val isMilestone = (i + 1) % 5 == 0

                    val bubbleBg = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isPast -> Color(0xFF52B788)
                        isMilestone -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val textColor = when {
                        isCurrent || isPast -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = CircleShape,
                        color = bubbleBg,
                        modifier = Modifier.size(if (isCurrent) 32.dp else 26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (isPast) "✓" else "${i + 1}",
                                fontSize = if (isCurrent) 12.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        // Question Card with Visual Emoji & Highlighted Text
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentQuestion.visualAidEmoji, fontSize = 16.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Question ${qIndex + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Score pill
                    Text(
                        "⭐ $correctCount Correct",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Highlighted Speech Question Text
                HighlightedSpeechText(
                    text = currentQuestion.questionText,
                    speechManager = viewModel.speechManager,
                    utteranceKey = "question_${currentQuestion.id}",
                    fontSize = 18,
                    isDyslexiaEnabled = profile.dyslexiaFontEnabled
                )
            }
        }

        // Answer Choices (with individual read-aloud buttons for non-readers)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            currentQuestion.options.forEachIndexed { optIndex, optionText ->
                val isSelected = selectedOption == optIndex
                val isCorrectOption = optIndex == currentQuestion.correctIndex

                val cardBg = when {
                    isSubmitted && isCorrectOption -> Color(0xFFD4EDDA)
                    isSubmitted && isSelected && !isCorrectOption -> Color(0xFFF8D7DA)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderStroke = when {
                    isSubmitted && isCorrectOption -> Color(0xFF28A745)
                    isSubmitted && isSelected && !isCorrectOption -> Color(0xFFDC3545)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitted) {
                            viewModel.selectOption(optIndex)
                        }
                        .testTag("option_card_$optIndex"),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderStroke)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter Badge (A, B, C, D)
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${('A' + optIndex)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // Option text
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (profile.dyslexiaFontEnabled) 17.sp else 16.sp,
                                letterSpacing = if (profile.dyslexiaFontEnabled) 1.1.sp else 0.5.sp
                            ),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // Dedicated option read-aloud TTS button
                        IconButton(
                            onClick = { viewModel.readSingleOption(optIndex) },
                            modifier = Modifier.size(32.dp).testTag("read_option_${optIndex}_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read option aloud",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (!isSubmitted) {
            Button(
                onClick = { viewModel.getAiHelpForCurrentQuestion() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(theme.emoji, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("I'm confused, AI Buddy help!", fontWeight = FontWeight.Bold)
            }
        }

        // Action Button: Check Answer or Next Question
        Spacer(Modifier.height(8.dp))

        if (!isSubmitted) {
            Button(
                onClick = { viewModel.submitAnswer() },
                enabled = selectedOption != null,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_answer_btn")
            ) {
                Text("Check Answer ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = { viewModel.nextQuestionOrComplete() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("next_question_btn")
            ) {
                Text(
                    if (qIndex < totalQuestions - 1) "Next Step 🚀" else "Complete Journey 🏆",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }

    // Growth-Mindset Error Coaching Dialog
    if (showErrorCoach) {
        AlertDialog(
            onDismissRequest = { /* forces user to read hint */ },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(theme.emoji, fontSize = 26.sp)
                    }
                }
            },
            title = {
                Text(
                    "Gentle Coaching Hint",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Mistakes help our brain form new neural pathways! Here is a friendly clue to guide you:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            errorCoachText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.nextQuestionOrComplete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Got It! Continue", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
