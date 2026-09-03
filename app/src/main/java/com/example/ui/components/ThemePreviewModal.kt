package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AgeGroupTier
import com.example.data.model.NeuroThemeCatalog
import com.example.data.model.NeuroThemeCategory
import com.example.data.model.NeuroThemeData
import com.example.data.model.ThemeRotationSchedule
import kotlin.math.cos
import kotlin.math.sin

enum class ThemePreviewTab(val title: String, val iconEmoji: String) {
    SIMULATION("Live Screen", "📱"),
    PALETTE("Color Palette", "🎨"),
    CURRICULUM("Curriculum & Buddy", "📚")
}

/**
 * Interactive Theme Preview Modal for the Settings menu.
 * Allows users to inspect and visualize the dynamic color palette, background atmospheric assets,
 * UI components, companion learning buddy, and curriculum integrations before applying the theme globally.
 */
@Composable
fun ThemePreviewModal(
    initialThemeId: String,
    currentActiveThemeId: String,
    initialRotationSchedule: ThemeRotationSchedule = ThemeRotationSchedule.MANUAL,
    onDismiss: () -> Unit,
    onApplyTheme: (themeId: String, schedule: ThemeRotationSchedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val allThemes = remember { NeuroThemeCatalog.getAllThemes() }
    var selectedThemeIndex by remember {
        val found = allThemes.indexOfFirst { it.id.equals(initialThemeId, ignoreCase = true) }
        mutableIntStateOf(if (found >= 0) found else 0)
    }

    val previewTheme = allThemes[selectedThemeIndex]
    val isCurrentlyActive = previewTheme.id.equals(currentActiveThemeId, ignoreCase = true)

    var previewTab by remember { mutableStateOf(ThemePreviewTab.SIMULATION) }
    var selectedRotationSchedule by remember { mutableStateOf(initialRotationSchedule) }
    var showQuickThemePicker by remember { mutableStateOf(false) }
    var themeSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<NeuroThemeCategory?>(null) }
    var appliedNotification by remember { mutableStateOf<String?>(null) }

    val primaryColor = Color(previewTheme.primaryHex)
    val secondaryColor = Color(previewTheme.secondaryHex)
    val surfaceColor = Color(previewTheme.surfaceHex)
    val cardColor = Color(previewTheme.cardHex)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header: Title, Controls, Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(previewTheme.emoji, fontSize = 20.sp)
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Theme Preview & Palette",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                "Visualize colors, background atmosphere & assets",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_theme_preview_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Theme Carousel Navigator & Quick Jump Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                selectedThemeIndex = if (selectedThemeIndex > 0) selectedThemeIndex - 1 else allThemes.size - 1
                            },
                            modifier = Modifier.testTag("prev_theme_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Theme")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showQuickThemePicker = !showQuickThemePicker }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "${previewTheme.emoji} ${previewTheme.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = primaryColor
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor
                                ) {
                                    Text(
                                        "${selectedThemeIndex + 1}/100",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                "${previewTheme.category.title} • Tap to switch",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedThemeIndex = if (selectedThemeIndex < allThemes.size - 1) selectedThemeIndex + 1 else 0
                            },
                            modifier = Modifier.testTag("next_theme_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Theme")
                        }
                    }
                }

                // Quick Theme Browser Dropdown / Drawer
                AnimatedVisibility(visible = showQuickThemePicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                            .padding(10.dp)
                    ) {
                        OutlinedTextField(
                            value = themeSearchQuery,
                            onValueChange = { themeSearchQuery = it },
                            placeholder = { Text("Search 100 theme worlds...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(6.dp))

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("All Categories", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                            NeuroThemeCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = {
                                        selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                    },
                                    label = { Text("${cat.emoji} ${cat.title}", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        val filteredThemes = remember(themeSearchQuery, selectedCategoryFilter) {
                            allThemes.filter { t ->
                                val matchCat = selectedCategoryFilter == null || t.category == selectedCategoryFilter
                                val matchQuery = themeSearchQuery.isBlank() ||
                                        t.title.contains(themeSearchQuery, ignoreCase = true) ||
                                        t.buddyName.contains(themeSearchQuery, ignoreCase = true) ||
                                        t.mathIntegration.contains(themeSearchQuery, ignoreCase = true) ||
                                        t.scienceIntegration.contains(themeSearchQuery, ignoreCase = true)
                                matchCat && matchQuery
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredThemes) { itemTheme ->
                                val isChosen = itemTheme.id == previewTheme.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChosen) primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = if (isChosen) BorderStroke(1.dp, primaryColor) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val index = allThemes.indexOfFirst { it.id == itemTheme.id }
                                            if (index >= 0) selectedThemeIndex = index
                                            showQuickThemePicker = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(itemTheme.emoji, fontSize = 16.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            itemTheme.title,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isChosen) primaryColor else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isChosen) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = previewTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    ThemePreviewTab.values().forEach { tab ->
                        Tab(
                            selected = previewTab == tab,
                            onClick = { previewTab = tab },
                            text = {
                                Text(
                                    "${tab.iconEmoji} ${tab.title}",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (previewTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Scrollable Content Pane
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (previewTab) {
                        ThemePreviewTab.SIMULATION -> {
                            LiveScreenSimulationView(
                                theme = previewTheme,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                cardColor = cardColor
                            )
                        }
                        ThemePreviewTab.PALETTE -> {
                            ColorPaletteInspectorView(
                                theme = previewTheme,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                cardColor = cardColor
                            )
                        }
                        ThemePreviewTab.CURRICULUM -> {
                            CurriculumAndBuddyView(
                                theme = previewTheme,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                cardColor = cardColor
                            )
                        }
                    }
                }

                // Success Notification Banner
                AnimatedVisibility(visible = appliedNotification != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFD4EDDA),
                        border = BorderStroke(1.dp, Color(0xFF28A745)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF155724), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                appliedNotification ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF155724)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                // Bottom Action Bar: Global Apply & Rotation Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔄 Rotation:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                selectedRotationSchedule.title.substringBefore("(").trim(),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Schedule Selector Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                ThemeRotationSchedule.MANUAL,
                                ThemeRotationSchedule.DAILY,
                                ThemeRotationSchedule.WEEKLY
                            ).forEach { sched ->
                                val isSel = selectedRotationSchedule == sched
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { selectedRotationSchedule = sched }
                                ) {
                                    Text(
                                        sched.icon,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                onApplyTheme(previewTheme.id, selectedRotationSchedule)
                                appliedNotification = "✨ ${previewTheme.title} applied globally!"
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentlyActive) Color(0xFF2E7D32) else primaryColor
                            ),
                            modifier = Modifier
                                .weight(1.8f)
                                .testTag("apply_theme_globally_btn")
                        ) {
                            Icon(
                                imageVector = if (isCurrentlyActive) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isCurrentlyActive) "Active Everywhere" else "Apply Globally",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Live Interactive Screen Simulation View.
 * Renders an authentic preview of how the child will experience this theme's
 * background assets, ambient canvas patterns, speech bubbles, and lesson cards.
 */
@Composable
private fun LiveScreenSimulationView(
    theme: NeuroThemeData,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    cardColor: Color
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "theme_ambient_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Simulated Mobile Device Frame
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = surfaceColor,
            border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Multi-layer Ambient Canvas Background
                ThemedBackgroundCanvas(
                    theme = theme,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    surfaceColor = surfaceColor,
                    pulseAlpha = pulseAlpha,
                    modifier = Modifier.matchParentSize()
                )

                // Simulated App Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Simulated Top Sensory Header Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.92f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(theme.emoji, fontSize = 18.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    theme.title.substringBefore(":"),
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = secondaryColor) {
                                    Text("⭐ 450 XP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text("🔥 5-Day", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    // Simulated Companion Buddy Greeting Bubble
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = cardColor,
                            border = BorderStroke(2.dp, primaryColor),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(theme.emoji, fontSize = 24.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp),
                            color = cardColor,
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        theme.buddyName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp,
                                        color = primaryColor
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "• ${theme.buddyRole}",
                                        fontSize = 9.5.sp,
                                        color = primaryColor.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "\"${theme.greeting}\"",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E212B)
                                )
                            }
                        }
                    }

                    // Simulated Active Quest / Lesson Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.95f)),
                        border = BorderStroke(1.5.dp, primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = primaryColor) {
                                    Text(
                                        "ACTIVE LESSON ADVENTURE",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("Step 2 of 5", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                "📐 Themed Challenge: ${theme.mathIntegration.take(80)}...",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E212B)
                            )

                            Spacer(Modifier.height(10.dp))

                            // Action Button Simulation
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Explore Quest Mission", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Background Assets & Atmosphere Insights
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sensory Atmosphere & Background Assets", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text(
                    "• Ambient Palette: ${categoryToSensoryDescriptor(theme.category)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "• Tactile Interactive Cue: ${theme.interactiveIdea}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "• Sensory Modulation: High readability contrast with reduced blue-light eye strain canvas.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Tab 2: Color Palette Inspector View.
 * Displays all color swatches, hex values, contrast ratios, and styled preview tokens.
 */
@Composable
private fun ColorPaletteInspectorView(
    theme: NeuroThemeData,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    cardColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Color Swatches & Material 3 Token Hierarchy",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Palette Swatch Cards
        PaletteSwatchRow(
            title = "Primary Accent",
            role = "Main actions, active milestones, header banners",
            color = primaryColor,
            hex = "#" + theme.primaryHex.toString(16).uppercase().takeLast(6)
        )

        PaletteSwatchRow(
            title = "Secondary Accent",
            role = "Badges, progress sparkles, buddy companion trims",
            color = secondaryColor,
            hex = "#" + theme.secondaryHex.toString(16).uppercase().takeLast(6)
        )

        PaletteSwatchRow(
            title = "Surface Background",
            role = "Screen canvas backdrop, low visual clutter",
            color = surfaceColor,
            hex = "#" + theme.surfaceHex.toString(16).uppercase().takeLast(6),
            isLight = true
        )

        PaletteSwatchRow(
            title = "Card Container",
            role = "Lesson question trays, dialog surfaces, chat bubbles",
            color = cardColor,
            hex = "#" + theme.cardHex.toString(16).uppercase().takeLast(6),
            isLight = true
        )

        Spacer(Modifier.height(4.dp))

        // Contrast & Accessibility Scorecard
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("♿ WCAG & Dyslexia Readability", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF28A745)) {
                        Text(
                            "AAA Certified",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "All high-frequency reading elements maintain a contrast ratio >= 4.5:1 against the ${theme.title} surface canvas, preventing eye strain during hyperfocus sessions.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive Live Component Samples
        Text(
            "Live Styled Components in Theme Palette:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Primary Button", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, primaryColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Outlined Action", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaletteSwatchRow(
    title: String,
    role: String,
    color: Color,
    hex: String,
    isLight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f)),
                modifier = Modifier.size(38.dp)
            ) {}

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            hex,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(role, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Tab 3: Curriculum & Companion Buddy View.
 * Displays cross-disciplinary subject mapping and companion details.
 */
@Composable
private fun CurriculumAndBuddyView(
    theme: NeuroThemeData,
    primaryColor: Color,
    secondaryColor: Color,
    cardColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Buddy Card Spotlight
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            border = BorderStroke(1.5.dp, primaryColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(theme.emoji, fontSize = 32.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            theme.buddyName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                        Text(
                            theme.buddyRole,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor.copy(alpha = 0.85f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"${theme.greeting}\"",
                    fontSize = 11.5.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFF1E212B)
                )
            }
        }

        Text(
            "📚 Cross-Disciplinary Subject Adaptations:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.primary
        )

        SubjectAdaptationTile(
            subjectEmoji = "🔢",
            subjectName = "Mathematics & Logic",
            integration = theme.mathIntegration,
            accentColor = primaryColor
        )

        SubjectAdaptationTile(
            subjectEmoji = "📖",
            subjectName = "Reading, ELA & Phonics",
            integration = theme.readingIntegration,
            accentColor = primaryColor
        )

        SubjectAdaptationTile(
            subjectEmoji = "🔬",
            subjectName = "Science & Physics",
            integration = theme.scienceIntegration,
            accentColor = primaryColor
        )

        SubjectAdaptationTile(
            subjectEmoji = "🏛️",
            subjectName = "Social Studies & Civics",
            integration = theme.socialStudiesIntegration,
            accentColor = primaryColor
        )

        // Best for Diagnoses & Strengths
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🧠 Neurodivergent Alignment:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    theme.bestForDiagnoses.forEach { diag ->
                        Surface(shape = RoundedCornerShape(6.dp), color = primaryColor.copy(alpha = 0.15f)) {
                            Text(diag, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectAdaptationTile(
    subjectEmoji: String,
    subjectName: String,
    integration: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(subjectEmoji, fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(subjectName, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = accentColor)
                Spacer(Modifier.height(2.dp))
                Text(integration, fontSize = 10.5.sp, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * Custom Canvas drawing for background ambient gradients and decorative sensory patterns.
 */
@Composable
private fun ThemedBackgroundCanvas(
    theme: NeuroThemeData,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Ambient Mesh Gradient Backdrop
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    surfaceColor,
                    surfaceColor.copy(alpha = 0.95f),
                    primaryColor.copy(alpha = 0.08f * pulseAlpha),
                    surfaceColor
                )
            )
        )

        // Draw Category-Specific Atmospheric Geometry
        when (theme.category) {
            NeuroThemeCategory.HISTORY_CIVILIZATION -> {
                // Architectural Columns & Sun Rays
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.12f * pulseAlpha),
                    radius = width * 0.35f,
                    center = Offset(width * 0.85f, height * 0.15f)
                )
                drawLine(
                    color = primaryColor.copy(alpha = 0.1f),
                    start = Offset(0f, height * 0.7f),
                    end = Offset(width, height * 0.7f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            NeuroThemeCategory.AI_ROBOTICS_TECH -> {
                // Circuit Grid Traces
                val step = 40.dp.toPx()
                for (x in 0 until (width / step).toInt()) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.06f * pulseAlpha),
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.15f * pulseAlpha),
                    radius = 24.dp.toPx(),
                    center = Offset(width * 0.8f, height * 0.25f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            NeuroThemeCategory.MYTH_FANTASY -> {
                // Celestial Magic Rings
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.18f * pulseAlpha),
                    radius = width * 0.28f,
                    center = Offset(width * 0.5f, height * 0.3f),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.10f),
                    radius = width * 0.42f,
                    center = Offset(width * 0.5f, height * 0.3f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            NeuroThemeCategory.NATURE_ECOLOGY -> {
                // Organic Wave Curves
                val path = Path().apply {
                    moveTo(0f, height * 0.85f)
                    cubicTo(
                        width * 0.3f, height * 0.78f,
                        width * 0.7f, height * 0.92f,
                        width, height * 0.85f
                    )
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(path, color = primaryColor.copy(alpha = 0.08f * pulseAlpha))
            }
            NeuroThemeCategory.MUSIC_AUDIO_ARTS -> {
                // Acoustic Wave Spectrum bars
                val barCount = 12
                val barWidth = width / (barCount * 2)
                for (i in 0 until barCount) {
                    val barH = (20 + (i % 5) * 12) * pulseAlpha
                    drawRoundRect(
                        color = secondaryColor.copy(alpha = 0.15f),
                        topLeft = Offset(i * (barWidth * 2) + 16.dp.toPx(), height * 0.88f - barH),
                        size = androidx.compose.ui.geometry.Size(barWidth, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }
            else -> {
                // Universal Calming Spheres
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f * pulseAlpha),
                    radius = width * 0.25f,
                    center = Offset(width * 0.9f, height * 0.2f)
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.07f * pulseAlpha),
                    radius = width * 0.20f,
                    center = Offset(width * 0.1f, height * 0.75f)
                )
            }
        }
    }
}

private fun categoryToSensoryDescriptor(cat: NeuroThemeCategory): String {
    return when (cat) {
        NeuroThemeCategory.HISTORY_CIVILIZATION -> "Warm parchment, gold leaf highlights & archaeological earth tones."
        NeuroThemeCategory.AI_ROBOTICS_TECH -> "Cool cybernetics, soft neon cyan & circuit-board low-stimulation contrast."
        NeuroThemeCategory.MYTH_FANTASY -> "Enchanted amethyst, emerald dragons & celestial glow."
        NeuroThemeCategory.CULINARY_FOOD_SCIENCE -> "Warm terracotta, appetizing saffron & clean kitchen geometry."
        NeuroThemeCategory.MUSIC_AUDIO_ARTS -> "Deep velvet acoustic resonance, brass highlights & rhythmic wave patterns."
        NeuroThemeCategory.SPORTS_KINETIC -> "Dynamic velocity accents, energetic turf greens & high focus clarity."
        NeuroThemeCategory.NATURE_ECOLOGY -> "Soothing forest canopy, deep ocean azure & parasympathetic relaxation greens."
        NeuroThemeCategory.ART_DESIGN -> "Creative pastel canvas, vibrant color theory & balanced negative space."
        NeuroThemeCategory.TRANSPORT_ENGINEERING -> "Industrial slate, steel locomotives & aerodynamic motion curves."
        NeuroThemeCategory.MYSTERY_SPY -> "Midnight noir, cryptography glow & secretive magnifying clarity."
    }
}
