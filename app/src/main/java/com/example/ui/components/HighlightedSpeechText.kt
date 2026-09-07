package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.SpeechManager

@Composable
fun HighlightedSpeechText(
    text: String,
    speechManager: SpeechManager,
    modifier: Modifier = Modifier,
    utteranceKey: String = "text_highlight",
    showTtsButton: Boolean = true,
    fontSize: Int = 18,
    isDyslexiaEnabled: Boolean = false
) {
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    val activeId by speechManager.activeUtteranceId.collectAsState()
    val highlightRange by speechManager.highlightRange.collectAsState()
    val currentRate by speechManager.speechRateFlow.collectAsState()

    val isThisActive = isSpeaking && activeId == utteranceKey

    val annotatedText = buildAnnotatedString {
        if (isThisActive && highlightRange != null) {
            val (start, end) = highlightRange!!
            val safeStart = start.coerceIn(0, text.length)
            val safeEnd = end.coerceIn(safeStart, text.length)

            // Before highlight
            if (safeStart > 0) {
                append(text.substring(0, safeStart))
            }
            // Highlighted word span (karaoke effect)
            withStyle(
                SpanStyle(
                    background = Color(0xFFFFE082),
                    color = Color(0xFF1E232A),
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append(text.substring(safeStart, safeEnd))
            }
            // After highlight
            if (safeEnd < text.length) {
                append(text.substring(safeEnd))
            }
        } else {
            append(text)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showTtsButton) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isThisActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            if (isThisActive) {
                                speechManager.stop()
                            } else {
                                speechManager.speak(text, utteranceKey)
                            }
                        }
                        .testTag("tts_read_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read aloud with word highlight",
                            tint = if (isThisActive) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // TTS Speed Slider Control
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Speed: ${String.format(java.util.Locale.US, "%.2f", currentRate)}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Slider(
                            value = currentRate,
                            onValueChange = { speechManager.setSpeechRate(it) },
                            valueRange = 0.5f..1.5f,
                            steps = 19,
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .testTag("tts_speed_slider")
                        )
                    }
                }
            }
        }

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                letterSpacing = if (isDyslexiaEnabled) 1.2.sp else 0.5.sp,
                lineHeight = (fontSize * 1.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
