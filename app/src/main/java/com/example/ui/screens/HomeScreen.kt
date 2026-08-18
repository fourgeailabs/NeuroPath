package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.model.DEFAULT_AVATAR_SHOP_ITEMS
import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

@Composable
fun HomeScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val lessonRecords by viewModel.lessonRecords.collectAsState()
    val theme = viewModel.getActiveTheme()

    val avatarItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.currentAvatarId }
    val hatItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedHatId }
    val petItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedPetId }
    val badgeItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedBadgeId }

    val gradeObj = GradeLevel.values().find { it.name == profile.gradeLevel } ?: GradeLevel.KINDERGARTEN

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header with Parent Lock & Avatar Shop Shortcut
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(theme.emoji, fontSize = 22.sp)
                            }
                        }
                        Column {
                            Text(
                                "NeuroPath",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${profile.name} • ${gradeObj.displayName} (${profile.stateStandard})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Shop Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.AVATAR_SHOP) }
                                .testTag("avatar_shop_header_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🛍️", fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Shop",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Parent Gate Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.PARENT_PIN_GATE) }
                                .testTag("parent_gate_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Parent Dashboard",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Parents",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hero Card: Interest World & Equipped Avatar Stage
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    theme.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    theme.greeting,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Avatar Showcase with Pet & Hat
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatarItem?.emoji ?: "🤖", fontSize = 36.sp)
                                if (hatItem != null) {
                                    Text(
                                        hatItem.emoji,
                                        fontSize = 20.sp,
                                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp)
                                    )
                                }
                                if (petItem != null) {
                                    Text(
                                        petItem.emoji,
                                        fontSize = 18.sp,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Rewards Chips Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RewardBadge(
                                icon = "⭐",
                                label = "${profile.totalStars} Stars",
                                bg = Color(0xFFFFF3CD),
                                textColor = Color(0xFF856404)
                            )
                            RewardBadge(
                                icon = "💎",
                                label = "${profile.totalGems} Gems",
                                bg = Color(0xFFD1ECF1),
                                textColor = Color(0xFF0C5460)
                            )
                            RewardBadge(
                                icon = "🔥",
                                label = "${profile.currentStreakDays} Day Streak",
                                bg = Color(0xFFFFE5D0),
                                textColor = Color(0xFFD84315)
                            )
                        }
                    }
                }
            }

            // 3. Interactive Games & Creative Expression Showcase
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "🎮 Interactive Games & Creative Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Ocean Reading Quest Game Card
                        ElevatedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(AppScreen.OCEAN_GAME) }
                                .testTag("ocean_game_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color(0xFFE0F7FA)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0077B6),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🌊", fontSize = 24.sp)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Ocean Reading",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF004977)
                                )
                                Text(
                                    "Word Search & Cloze",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00629D)
                                )
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFB2EBF2)
                                ) {
                                    Text(
                                        "⭐ +5 Stars",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF004977),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Creative Studio Card
                        ElevatedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(AppScreen.CREATIVE_STUDIO) }
                                .testTag("creative_studio_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color(0xFFF3E5F5)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF7B1FA2),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🎨", fontSize = 24.sp)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Creative Studio",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4A148C)
                                )
                                Text(
                                    "Art Canvas & Stories",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6A1B9A)
                                )
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE1BEE7)
                                ) {
                                    Text(
                                        "⭐ +5 Stars",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A148C),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. 20-Node Adaptive Journey Map Preview
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🎯 20-Step Adaptive Mastery Path",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Visual Scaffolding",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..20) {
                                val isMilestone = i % 5 == 0
                                Surface(
                                    shape = CircleShape,
                                    color = if (isMilestone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(if (isMilestone) 36.dp else 30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            if (isMilestone) "⭐" else "$i",
                                            fontSize = if (isMilestone) 14.sp else 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMilestone) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                if (i < 20) {
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(2.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Core Curriculum Workbooks
            item {
                Text(
                    "📚 State & National Curriculum Workbooks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // List of Workbooks
            items(EducationalSubject.values()) { subject ->
                val lessons = CurriculumCatalog.getLessonsForSubjectAndGrade(
                    subject = subject,
                    gradeLevel = gradeObj,
                    stateStandardCode = profile.stateStandard,
                    themeWorldId = profile.activeThemeId
                )
                val activeLesson = lessons.firstOrNull()
                val record = lessonRecords.find { it.subjectId == subject.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable {
                            if (activeLesson != null) {
                                viewModel.startLesson(activeLesson)
                            }
                        }
                        .testTag("subject_card_${subject.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(subject.emoji, fontSize = 28.sp)
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    subject.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (record?.completed == true) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFD4EDDA)
                                    ) {
                                        Text(
                                            "✅ Mastered",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF155724),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                activeLesson?.title ?: subject.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "⚡ Standard: ${activeLesson?.stateStandardCode ?: "Aligned"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "• 20 Q's Adaptive",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Play / Launch Lesson Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Lesson",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating NeuroBuddy AI Tutor Button
        FloatingActionButton(
            onClick = { viewModel.navigateTo(AppScreen.NEURO_BUDDY_CHAT) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("floating_neurobuddy_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(theme.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("AI Buddy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RewardBadge(icon: String, label: String, bg: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}
