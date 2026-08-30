package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_STORY_PROMPTS
import com.example.data.model.PlacedSticker
import com.example.data.model.ThemeStoryPrompt
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import kotlinx.coroutines.launch

enum class CreativeMode(val title: String, val icon: String) {
    DRAWING_CANVAS("Art Studio", "🎨"),
    STORY_WRITER("Story Writer", "✍️")
}

data class DrawStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreativeStudioScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    var creativeMode by remember { mutableStateOf(CreativeMode.DRAWING_CANVAS) }
    val profile by viewModel.currentProfile.collectAsState()
    val activeTheme = viewModel.getActiveTheme()
    val scope = rememberCoroutineScope()

    // Drawing State
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    var currentStrokePoints = remember { mutableStateListOf<Offset>() }
    val placedStickers = remember { mutableStateListOf<PlacedSticker>() }
    var selectedColor by remember { mutableStateOf(Color(0xFF00629D)) }
    var selectedBrushWidth by remember { mutableFloatStateOf(10f) }
    var selectedStickerEmoji by remember { mutableStateOf<String?>(null) }
    var isArtworkSaved by remember { mutableStateOf(false) }

    // Story State
    val storyPrompts = DEFAULT_STORY_PROMPTS
    var selectedPrompt by remember { mutableStateOf<ThemeStoryPrompt?>(storyPrompts.firstOrNull { it.themeId == activeTheme.id } ?: storyPrompts.first()) }
    var storyTitle by remember { mutableStateOf(selectedPrompt?.title ?: "My Adventure Story") }
    var storyContent by remember { mutableStateOf(selectedPrompt?.promptStarter ?: "") }
    var isStoryGeneratingIdea by remember { mutableStateOf(false) }
    var aiSparkedIdea by remember { mutableStateOf<String?>(null) }
    var isStorySaved by remember { mutableStateOf(false) }

    val paletteColors = listOf(
        Color(0xFF00629D), // Cobalt
        Color(0xFF0077B6), // Ocean Blue
        Color(0xFF2D6A4F), // Forest Green
        Color(0xFFD84315), // Dragon Flame Orange
        Color(0xFFC70039), // Mythic Ruby
        Color(0xFF7B1FA2), // Magic Violet
        Color(0xFFFBC02D), // Sun Gold
        Color(0xFF1E232A), // Obsidian
        Color(0xFFE0E0E0)  // Eraser Light
    )

    val stickerCatalog = when (activeTheme.id) {
        "mythical" -> listOf("🐉", "🦅", "🦄", "🧚‍♀️", "🏰", "✨", "🔥", "📜", "👑", "🛡️")
        "ocean" -> listOf("🐬", "🪸", "🐠", "🐙", "🦈", "🌊", "🫧", "💎", "🏝️", "🤿")
        "space" -> listOf("🚀", "👨‍🚀", "🪐", "⭐", "🛸", "👾", "✨", "🛰️", "🌌", "☄️")
        "dino" -> listOf("🦖", "🦕", "🌴", "🦴", "🌋", "🥚", "🌿", "⭐", "🐾", "🪵")
        else -> listOf("⭐", "🌟", "💖", "🎨", "🚀", "🦄", "🐉", "🐬", "🦖", "🏆")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Top Navigation Bar
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
                        modifier = Modifier.testTag("creative_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨 ", fontSize = 18.sp)
                            Text(
                                "Creative Expression",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "Theme: ${activeTheme.title}",
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐ +5 Stars", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF856404))
                    }
                }
            }
        }

        // 2. Mode Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CreativeMode.values().forEach { mode ->
                val isSelected = creativeMode == mode
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { creativeMode = mode }
                        .testTag("creative_mode_${mode.name}")
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

        // 3. Main Workspace Content
        if (creativeMode == CreativeMode.DRAWING_CANVAS) {
            // ART STUDIO CANVAS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Tool Control Bar (Color + Brush + Actions)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color Palette
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(paletteColors) { color ->
                            val isColorSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isColorSelected) 3.dp else 1.dp,
                                        color = if (isColorSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColor = color
                                        viewModel.triggerHapticPop()
                                    }
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Brush Width Toggle
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(4f to "Fine", 12f to "Med", 24f to "Bold").forEach { (width, label) ->
                            val isBrushSelected = selectedBrushWidth == width
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBrushSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    selectedBrushWidth = width
                                    viewModel.triggerHapticPop()
                                }
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBrushSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Sticker Drawer
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        "✨ Themed Stamps (Tap a stamp, then tap on the canvas):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stickerCatalog) { sticker ->
                            val isStickerActive = selectedStickerEmoji == sticker
                            Surface(
                                shape = CircleShape,
                                color = if (isStickerActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (isStickerActive) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        selectedStickerEmoji = if (selectedStickerEmoji == sticker) null else sticker
                                        viewModel.triggerHapticPop()
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(sticker, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                // Drawing Canvas Area
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = maxWidth
                        val canvasHeight = maxHeight

                        // Drawing surface
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(selectedColor, selectedBrushWidth, selectedStickerEmoji) {
                                    detectTapGestures { tapOffset ->
                                        if (selectedStickerEmoji != null) {
                                            val xRatio = if (size.width > 0) (tapOffset.x / size.width.toFloat()).coerceIn(0f, 1f) else 0f
                                            val yRatio = if (size.height > 0) (tapOffset.y / size.height.toFloat()).coerceIn(0f, 1f) else 0f
                                            placedStickers.add(
                                                PlacedSticker(
                                                    id = System.currentTimeMillis().toString(),
                                                    emoji = selectedStickerEmoji!!,
                                                    xRatio = xRatio,
                                                    yRatio = yRatio
                                                )
                                            )
                                            viewModel.triggerHapticPop()
                                        }
                                    }
                                }
                                .pointerInput(selectedColor, selectedBrushWidth) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentStrokePoints.clear()
                                            currentStrokePoints.add(offset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentStrokePoints.add(change.position)
                                        },
                                        onDragEnd = {
                                            if (currentStrokePoints.isNotEmpty()) {
                                                strokes.add(
                                                    DrawStroke(
                                                        points = currentStrokePoints.toList(),
                                                        color = selectedColor,
                                                        strokeWidth = selectedBrushWidth
                                                    )
                                                )
                                                currentStrokePoints.clear()
                                            }
                                        }
                                    )
                                }
                        ) {
                            // Draw past strokes
                            strokes.forEach { stroke ->
                                if (stroke.points.size > 1) {
                                    val path = Path().apply {
                                        moveTo(stroke.points.first().x, stroke.points.first().y)
                                        for (i in 1 until stroke.points.size) {
                                            lineTo(stroke.points[i].x, stroke.points[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = stroke.color,
                                        style = Stroke(
                                            width = stroke.strokeWidth,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // Draw current ongoing stroke
                            if (currentStrokePoints.size > 1) {
                                val currentPath = Path().apply {
                                    moveTo(currentStrokePoints.first().x, currentStrokePoints.first().y)
                                    for (i in 1 until currentStrokePoints.size) {
                                        lineTo(currentStrokePoints[i].x, currentStrokePoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = currentPath,
                                    color = selectedColor,
                                    style = Stroke(
                                        width = selectedBrushWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Render placed stickers
                        placedStickers.forEach { placed ->
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = (placed.xRatio * canvasWidth.value).dp.coerceAtLeast(0.dp),
                                        top = (placed.yRatio * canvasHeight.value).dp.coerceAtLeast(0.dp)
                                    )
                            ) {
                                Text(placed.emoji, fontSize = 28.sp)
                            }
                        }
                    }
                }

                // Canvas Actions Row (Undo, Clear, Save)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                                else if (placedStickers.isNotEmpty()) placedStickers.removeAt(placedStickers.lastIndex)
                                viewModel.triggerHapticPop()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo")
                        }
                        IconButton(
                            onClick = {
                                strokes.clear()
                                placedStickers.clear()
                                viewModel.triggerHapticPop()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Canvas")
                        }
                    }

                    Button(
                        onClick = {
                            isArtworkSaved = true
                            viewModel.awardMiniGameRewards(5, 2, "CREATIVE_ART")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                        Spacer(Modifier.width(6.dp))
                        Text(if (isArtworkSaved) "Saved! ⭐" else "Save Artwork")
                    }
                }
            }
        } else {
            // STORY WRITER MODULE
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Story Prompt Starter Selector
                item {
                    Text(
                        "📖 Choose an Adventure Prompt Starter:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(storyPrompts) { prompt ->
                            val isPromptSelected = selectedPrompt?.id == prompt.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isPromptSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .clickable {
                                        selectedPrompt = prompt
                                        storyTitle = prompt.title
                                        storyContent = prompt.promptStarter
                                        viewModel.triggerHapticPop()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(prompt.emoji, fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        prompt.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPromptSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Story Sparker with Learning Buddy
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🤖", fontSize = 20.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Spark Ideas with Learning Buddy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Button(
                                    onClick = {
                                        isStoryGeneratingIdea = true
                                        scope.launch {
                                            val idea = viewModel.sparkStoryIdea(
                                                themeTitle = activeTheme.title,
                                                promptTopic = storyTitle
                                            )
                                            aiSparkedIdea = idea
                                            isStoryGeneratingIdea = false
                                            viewModel.speechManager.speak("Here is an idea: $idea")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = !isStoryGeneratingIdea
                                ) {
                                    if (isStoryGeneratingIdea) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Spark Idea", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Spark!", fontSize = 12.sp)
                                    }
                                }
                            }

                            if (aiSparkedIdea != null) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            "💡 Idea: $aiSparkedIdea",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                storyContent += "\n\n${aiSparkedIdea ?: ""}"
                                                aiSparkedIdea = null
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Text("Add to My Story ➕", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Story Title Input
                item {
                    OutlinedTextField(
                        value = storyTitle,
                        onValueChange = { storyTitle = it },
                        label = { Text("Story Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // Story Body Input
                item {
                    Column {
                        OutlinedTextField(
                            value = storyContent,
                            onValueChange = { storyContent = it },
                            label = { Text("Write your adventure story...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Word Count: ${storyContent.split("\\s+".toRegex()).filter { it.isNotBlank() }.size} words",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Story Action Controls (Read Aloud + Save)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.speechManager.speak("$storyTitle. $storyContent")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Read Aloud")
                            Spacer(Modifier.width(6.dp))
                            Text("Read Aloud 🔊")
                        }

                        Button(
                            onClick = {
                                isStorySaved = true
                                viewModel.awardMiniGameRewards(6, 2, "CREATIVE_STORY")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save Story")
                            Spacer(Modifier.width(6.dp))
                            Text(if (isStorySaved) "Saved! ⭐" else "Save Story")
                        }
                    }
                }
            }
        }
    }
}
