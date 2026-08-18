package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.ClozeSentence
import com.example.data.model.DEFAULT_OCEAN_PASSAGES
import com.example.data.model.OceanReadingPassage
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

enum class OceanGameMode(val title: String, val icon: String) {
    FILL_IN_BLANKS("Cloze Reading", "📝"),
    WORD_SEARCH("Word Search Hunt", "🔍")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OceanReadingGameScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val passages = DEFAULT_OCEAN_PASSAGES
    var selectedPassageIndex by remember { mutableIntStateOf(0) }
    val currentPassage = passages.getOrElse(selectedPassageIndex) { passages.first() }

    var gameMode by remember { mutableStateOf(OceanGameMode.FILL_IN_BLANKS) }

    // State for Fill-In-The-Blank (Cloze)
    val userAnswers = remember(currentPassage.id) { mutableStateMapOf<Int, String>() }
    var activeSentenceIndex by remember(currentPassage.id) { mutableIntStateOf(0) }
    var showHintForSentenceId by remember(currentPassage.id) { mutableStateOf<Int?>(null) }
    var isClozeCompleted by remember(currentPassage.id) { mutableStateOf(false) }

    // State for Word Search
    val foundWords = remember(currentPassage.id) { mutableStateMapOf<String, Boolean>() }
    var selectedCells by remember(currentPassage.id) { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var isWordSearchCompleted by remember(currentPassage.id) { mutableStateOf(false) }

    val oceanBluePrimary = Color(0xFF0077B6)
    val oceanTeal = Color(0xFF48CAE4)
    val oceanDeepBg = Color(0xFFE8F6FA)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(oceanDeepBg)
    ) {
        // 1. Header with Back Button & Score Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("ocean_game_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = oceanBluePrimary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌊 ", fontSize = 18.sp)
                            Text(
                                "Ocean Reading Quest",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = oceanBluePrimary
                            )
                        }
                        Text(
                            "Marine Science Comprehension",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3CD)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐ +5 Stars", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF856404))
                    }
                }
            }
        }

        // 2. Passage Selector Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(passages) { idx, pass ->
                val isSelected = idx == selectedPassageIndex
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) oceanBluePrimary else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clickable {
                            selectedPassageIndex = idx
                            viewModel.speechManager.stop()
                        }
                        .testTag("passage_tab_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (idx == 0) "🪸 " else "💡 ",
                            fontSize = 14.sp
                        )
                        Text(
                            pass.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Game Mode Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OceanGameMode.values().forEach { mode ->
                val isSelected = gameMode == mode
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) oceanBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { gameMode = mode }
                        .testTag("game_mode_${mode.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mode.icon, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            mode.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Main Activity Area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Science Passage Card with TTS
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    currentPassage.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = oceanBluePrimary
                                )
                                Text(
                                    currentPassage.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.speechManager.speak(currentPassage.passageText)
                                },
                                modifier = Modifier
                                    .background(oceanTeal.copy(alpha = 0.2f), CircleShape)
                                    .testTag("read_passage_tts_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read passage aloud",
                                    tint = oceanBluePrimary
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            currentPassage.passageText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Mode Content
            if (gameMode == OceanGameMode.FILL_IN_BLANKS) {
                // Cloze Fill-In-The-Blank Module
                item {
                    Text(
                        "📝 Fill in the Scientific Blanks:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(currentPassage.clozeSentences) { sentence ->
                    val userAnswer = userAnswers[sentence.id]
                    val isCorrect = userAnswer?.equals(sentence.correctWord, ignoreCase = true) == true
                    val isWrong = userAnswer != null && !isCorrect

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cloze_card_${sentence.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCorrect -> Color(0xFFE8F5E9)
                                isWrong -> Color(0xFFFFEBEE)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4CAF50)) else null
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sentence.visualEmoji, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Clue ${sentence.id}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = oceanBluePrimary
                                )
                                Spacer(Modifier.weight(1f))
                                if (isCorrect) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFC8E6C9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Correct",
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(2.dp))
                                            Text("Mastered", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Interactive Sentence with Blank Button
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    sentence.textBeforeBlank,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 26.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (userAnswer != null) oceanBluePrimary else oceanTeal.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .clickable {
                                            // Reset answer for this sentence
                                            userAnswers.remove(sentence.id)
                                        }
                                ) {
                                    Text(
                                        userAnswer ?: " [ Tap Word Below ] ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (userAnswer != null) Color.White else oceanBluePrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    sentence.textAfterBlank,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 26.sp
                                )
                            }

                            // Hint / Explanation if available
                            if (showHintForSentenceId == sentence.id) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF8E1)
                                ) {
                                    Text(
                                        "💡 Hint: ${sentence.hint}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF795548),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        showHintForSentenceId = if (showHintForSentenceId == sentence.id) null else sentence.id
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Hint",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Hint", fontSize = 12.sp)
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.speechManager.speak("${sentence.textBeforeBlank} blank ${sentence.textAfterBlank}")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Read sentence",
                                        tint = oceanBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Word Bank Chips
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            "🏷️ Marine Vocabulary Bank (Tap to place in active blank):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentPassage.vocabulary.forEach { vocab ->
                                val isPlaced = userAnswers.values.contains(vocab.word)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isPlaced) MaterialTheme.colorScheme.surfaceVariant else oceanBluePrimary,
                                    modifier = Modifier
                                        .clickable(enabled = !isPlaced) {
                                            // Find first unanswered sentence or replace
                                            val unanswered = currentPassage.clozeSentences.find { userAnswers[it.id] == null }
                                            if (unanswered != null) {
                                                userAnswers[unanswered.id] = vocab.word
                                                viewModel.triggerHapticPop()
                                                // Check if all correct
                                                val allCorrect = currentPassage.clozeSentences.all {
                                                    userAnswers[it.id]?.equals(it.correctWord, ignoreCase = true) == true
                                                }
                                                if (allCorrect && !isClozeCompleted) {
                                                    isClozeCompleted = true
                                                    viewModel.awardMiniGameRewards(5, 2, "OCEAN_CLOZE")
                                                }
                                            }
                                        }
                                        .testTag("vocab_chip_${vocab.word}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(vocab.emoji, fontSize = 14.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            vocab.word,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPlaced) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isClozeCompleted) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFD4EDDA),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉 Marine Science Champion! 🎉", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF155724))
                                Text("You completed all vocabulary comprehension blanks accurately!", fontSize = 13.sp, color = Color(0xFF155724))
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { gameMode = OceanGameMode.WORD_SEARCH },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
                                ) {
                                    Text("Play Ocean Word Search 🔍")
                                }
                            }
                        }
                    }
                }
            } else {
                // Word Search Hunt Module
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🔍 Deep Sea Word Search Grid:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    selectedCells = emptyList()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Selection",
                                    tint = oceanBluePrimary
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Target Words to Find
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentPassage.wordSearchWords.forEach { word ->
                                val isFound = foundWords[word] == true
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isFound) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.surface,
                                    border = if (isFound) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)) else null,
                                    modifier = Modifier
                                        .clickable {
                                            // Tap to mark found for accessibility / playful discovery
                                            foundWords[word] = true
                                            viewModel.triggerHapticSuccess()
                                            viewModel.speechManager.speak("Found $word!")
                                            if (currentPassage.wordSearchWords.all { foundWords[it] == true } && !isWordSearchCompleted) {
                                                isWordSearchCompleted = true
                                                viewModel.awardMiniGameRewards(6, 2, "OCEAN_WORD_SEARCH")
                                            }
                                        }
                                        .testTag("target_word_$word")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isFound) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Found",
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            word,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFound) Color(0xFF2E7D32) else oceanBluePrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // 8x8 Grid
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                currentPassage.letterGrid.forEachIndexed { rowIdx, rowStr ->
                                    val letters = rowStr.split(" ").filter { it.isNotBlank() }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        letters.forEachIndexed { colIdx, letter ->
                                            val isCellSelected = selectedCells.contains(rowIdx to colIdx)
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isCellSelected) oceanTeal else oceanDeepBg,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clickable {
                                                        viewModel.triggerHapticPop()
                                                        val cell = rowIdx to colIdx
                                                        selectedCells = if (selectedCells.contains(cell)) {
                                                            selectedCells - cell
                                                        } else {
                                                            selectedCells + cell
                                                        }
                                                    }
                                                    .testTag("grid_cell_${rowIdx}_$colIdx")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        letter,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 15.sp,
                                                        color = if (isCellSelected) Color.White else oceanBluePrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isWordSearchCompleted) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFD4EDDA),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏆 Oceanic Explorer Completed! 🏆", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF155724))
                                Text("You found all marine vocabulary words in the deep ocean grid!", fontSize = 13.sp, color = Color(0xFF155724))
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                    colors = ButtonDefaults.buttonColors(containerColor = oceanBluePrimary)
                                ) {
                                    Text("Return Home 🏠")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
