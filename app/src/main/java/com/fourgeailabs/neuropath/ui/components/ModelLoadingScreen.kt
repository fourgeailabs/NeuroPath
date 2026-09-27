package com.fourgeailabs.neuropath.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.R
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState

private val TypewriterFont = FontFamily(Font(R.font.special_elite))
private val InkColor = Color(0xFF2E2A26)
private val FadedInkColor = Color(0xFF6B645C)

private const val FADE_MS = 900

/**
 * Typewriter-style loading screen shown while the on-device AI model loads.
 *
 * White paper background, worn-typewriter font, and a rotating "nibble" of
 * learning facts (one per [LOADING_FACT_ROTATION_MS]) that crossfades like a
 * screensaver while the loading bar fills below.
 */
@Composable
fun ModelLoadingScreen(
    buddyName: String,
    themeEmoji: String,
    themeId: String,
    progress: Float,
    stage: String,
    onSkip: () -> Unit,
    loadError: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val facts = remember(themeId) { loadingFactsForTheme(themeId).shuffled() }
    var factIndex by remember(themeId) { mutableIntStateOf(0) }
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(themeId) {
        while (true) {
            delay(LOADING_FACT_ROTATION_MS)
            factIndex = (factIndex + 1) % facts.size
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "modelLoadProgress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DID YOU KNOW?",
                fontFamily = TypewriterFont,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                color = FadedInkColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = facts[factIndex % facts.size],
                    transitionSpec = {
                        fadeIn(tween(FADE_MS)) togetherWith fadeOut(tween(FADE_MS))
                    },
                    label = "loadingFact"
                ) { fact ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${fact.module.uppercase()} NIBBLE",
                            fontFamily = TypewriterFont,
                            fontSize = 12.sp,
                            letterSpacing = 3.sp,
                            color = FadedInkColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = fact.text + if (cursorVisible) " \u258C" else "",
                            fontFamily = TypewriterFont,
                            fontSize = 19.sp,
                            lineHeight = 30.sp,
                            color = InkColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "$themeEmoji $buddyName is warming up…",
                fontFamily = TypewriterFont,
                fontSize = 15.sp,
                color = InkColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = InkColor,
                trackColor = Color(0xFFE8E2D8)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stage,
                fontFamily = TypewriterFont,
                fontSize = 13.sp,
                color = FadedInkColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            if (loadError != null) {
                Text(
                    text = loadError,
                    fontFamily = TypewriterFont,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFB3261E),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry) {
                    Text(
                        text = "try again",
                        fontFamily = TypewriterFont,
                        fontSize = 14.sp,
                        color = InkColor
                    )
                }
            }
            TextButton(onClick = onSkip) {
                Text(
                    text = "skip",
                    fontFamily = TypewriterFont,
                    fontSize = 13.sp,
                    color = FadedInkColor
                )
            }
        }
    }
}
