package com.fourgeailabs.neuropath.ui.screens

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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.data.curriculum.CurriculumCatalog
import com.fourgeailabs.neuropath.data.model.AgeGroupTier
import com.fourgeailabs.neuropath.data.model.AvatarItem
import com.fourgeailabs.neuropath.data.model.DEFAULT_AVATAR_SHOP_ITEMS
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.GradeLevel
import com.fourgeailabs.neuropath.data.model.WorldTheme
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf

@Composable
fun HomeScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val lessonRecords by viewModel.lessonRecords.collectAsState()
    val theme = viewModel.getActiveTheme()
    val tier = AgeGroupTier.entries.find { it.id == profile.ageGroupTier } ?: AgeGroupTier.ELEMENTARY

    val avatarItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.currentAvatarId }
    val hatItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedHatId }
    val petItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedPetId }
    val badgeItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedBadgeId }

    val gradeObj = GradeLevel.entries.find { it.name == profile.gradeLevel } ?: GradeLevel.KINDERGARTEN
    val isDownloadingCurriculum by viewModel.isDownloadingCurriculum.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Offline Sync Banner
            item {
                AnimatedVisibility(visible = isDownloadingCurriculum) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3CD),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE8A1))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("☁️", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    t("downloading_curriculum_for_offline_use"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF856404)
                                )
                                Text(
                                    t("saving_interactive_lessons_videos_you_can_learn"),
                                    fontSize = 11.sp,
                                    color = Color(0xFF856404)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Universal Top Header: Switch Profile + Avatar Shop + Parent Lock
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(theme.emoji, fontSize = 22.sp)
                            }
                        }
                        Column {
                            Text(
                                text = if (profile.name.isNotBlank()) profile.name else "Learner",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = tf("str_7", gradeObj.displayName, tier.title),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Switch Profile Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.PROFILE_SELECTION) }
                                .testTag("switch_profile_header_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    t("profiles"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Avatar Shop Shortcut
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.AVATAR_SHOP) }
                                .testTag("avatar_shop_header_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🛍️", fontSize = 13.sp)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    t("shop"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Parent PIN Gate Shortcut
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.PARENT_PIN_GATE) }
                                .testTag("parent_gate_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = t("parent_dashboard"),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    t("parents"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }



            // DYNAMIC LAYOUT ACCORDING TO AGE TIER
            when (tier) {
                AgeGroupTier.ELEMENTARY -> {
                    renderElementaryLayout(
                        viewModel = viewModel,
                        profile = profile,
                        theme = theme,
                        avatarItem = avatarItem,
                        hatItem = hatItem,
                        petItem = petItem,
                        gradeObj = gradeObj,
                        lessonRecords = lessonRecords
                    )
                }
                AgeGroupTier.MIDDLE_SCHOOL -> {
                    renderMiddleSchoolLayout(
                        viewModel = viewModel,
                        profile = profile,
                        theme = theme,
                        avatarItem = avatarItem,
                        gradeObj = gradeObj,
                        lessonRecords = lessonRecords
                    )
                }
                AgeGroupTier.HIGH_SCHOOL -> {
                    renderHighSchoolLayout(
                        viewModel = viewModel,
                        profile = profile,
                        theme = theme,
                        gradeObj = gradeObj,
                        lessonRecords = lessonRecords
                    )
                }
            }

            // Daily Motivation Quote
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { viewModel.readDailyQuote() }
                                .testTag("read_quote_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = t("read_quote"),
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                t("daily_spark_of_inspiration"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                dailyQuote,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.refreshDailyQuote() },
                            modifier = Modifier.testTag("refresh_quote_btn").size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = t("refresh_quote"),
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating AI Tutor / Learning Companion Button
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
                Text(
                    if (tier == AgeGroupTier.HIGH_SCHOOL) "AI Socratic Tutor" else "Learning Buddy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. ELEMENTARY LAYOUT (Ages 4-10)
// Bright, Playful, Companion-Driven, Tactile
// -------------------------------------------------------------
private fun androidx.compose.foundation.lazy.LazyListScope.renderElementaryLayout(
    viewModel: NeuroPathViewModel,
    profile: com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity,
    theme: WorldTheme,
    avatarItem: AvatarItem?,
    hatItem: AvatarItem?,
    petItem: AvatarItem?,
    gradeObj: GradeLevel,
    lessonRecords: List<com.fourgeailabs.neuropath.data.local.entity.LessonRecordEntity>
) {
    // Companion Hero Stage
    item {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color(theme.surfaceHex)
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
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(theme.primaryHex)
                        )
                        Text(
                            theme.greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(theme.cardHex)),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RewardBadge(icon = "⭐", label = "${profile.totalStars} Stars", bg = Color(0xFFFFF3CD), textColor = Color(0xFF856404))
                    RewardBadge(icon = "💎", label = "${profile.totalGems} Gems", bg = Color(0xFFD1ECF1), textColor = Color(0xFF0C5460))
                    RewardBadge(icon = "🔥", label = "${profile.currentStreakDays}d Streak", bg = Color(0xFFFFE5D0), textColor = Color(0xFFD84315))
                }
            }
        }
    }

    // Games & Creative Expression
    item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                t("learning_games_studio"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.OCEAN_GAME) }
                        .testTag("ocean_game_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE0F7FA))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF0077B6), modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("🌊", fontSize = 22.sp) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(t("ocean_reading"), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF004977))
                        Text(t("word_safari"), fontSize = 11.sp, color = Color(0xFF00629D))
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.CREATIVE_STUDIO) }
                        .testTag("creative_studio_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF3E5F5))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF7B1FA2), modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("🎨", fontSize = 22.sp) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(t("art_studio"), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF4A148C))
                        Text(t("draw_create"), fontSize = 11.sp, color = Color(0xFF6A1B9A))
                    }
                }
            }
        }
    }

    // 20-Step Visual Safari Map
    item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                t("20_step_quest_map"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                            color = if (isMilestone) Color(theme.primaryHex) else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(if (isMilestone) 34.dp else 28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isMilestone) "⭐" else "$i",
                                    fontSize = if (isMilestone) 13.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMilestone) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Elementary Subject Workbooks (Standard codes hidden from child)
    item {
        Text(
            t("learning_adventure_paths"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(EducationalSubject.entries) { subject ->
        val lessons = CurriculumCatalog.getLessonsForSubjectAndGrade(
            subject = subject,
            gradeLevel = gradeObj,
            stateStandardCode = profile.stateStandard,
            themeWorldId = profile.activeThemeId,
            country = profile.country
        )
        val activeLesson = lessons.firstOrNull()
        val record = lessonRecords.find { it.subjectId == subject.id }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    if (activeLesson != null) {
                        viewModel.startLesson(activeLesson)
                    }
                }
                .testTag("subject_card_${subject.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(subject.emoji, fontSize = 24.sp)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        subject.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        activeLesson?.title ?: subject.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                    Text(
                        t("interactive_exploration_20_adaptive_steps"),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = t("start_lesson"),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. MIDDLE SCHOOL LAYOUT (Ages 11-14)
// Quest Command Center, XP Progression, Challenge Labs
// -------------------------------------------------------------
private fun androidx.compose.foundation.lazy.LazyListScope.renderMiddleSchoolLayout(
    viewModel: NeuroPathViewModel,
    profile: com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity,
    theme: WorldTheme,
    avatarItem: AvatarItem?,
    gradeObj: GradeLevel,
    lessonRecords: List<com.fourgeailabs.neuropath.data.local.entity.LessonRecordEntity>
) {
    // XP & Quest Rank Banner
    item {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                t("quest_command_center"),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                tf("level_explorer_streak_days", 1 + profile.totalStars / 10, profile.currentStreakDays),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            tf("xp", profile.totalStars * 50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // XP Progress Bar to next level
                val progressToNextLevel = (profile.totalStars % 10) / 10f
                LinearProgressIndicator(
                    progress = { progressToNextLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }

    // Daily Mission Cards
    item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                t("daily_focus_missions"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Focus Sprint Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.BREATHING_GUIDE) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(t("reset_calm"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(t("4_7_8_breathing_2"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Interactive Fidget Pop-It
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.FIDGET_POPIT) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(t("sensory_fidget"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(t("tactile_focus_loop"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Subject Mastery Modules
    item {
        Text(
            t("academic_quest_modules"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(EducationalSubject.entries) { subject ->
        val lessons = CurriculumCatalog.getLessonsForSubjectAndGrade(
            subject = subject,
            gradeLevel = gradeObj,
            stateStandardCode = profile.stateStandard,
            themeWorldId = profile.activeThemeId,
            country = profile.country
        )
        val activeLesson = lessons.firstOrNull()
        val record = lessonRecords.find { it.subjectId == subject.id }

        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    if (activeLesson != null) {
                        viewModel.startLesson(activeLesson)
                    }
                },
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(subject.emoji, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(subject.title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Text(activeLesson?.title ?: subject.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = {
                        if (activeLesson != null) viewModel.startLesson(activeLesson)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(t("launch"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. HIGH SCHOOL LAYOUT (Ages 15-18+)
// Productivity Studio, Concept Trees, Pomodoro Sprint, Flashcard Mastery
// -------------------------------------------------------------
private fun androidx.compose.foundation.lazy.LazyListScope.renderHighSchoolLayout(
    viewModel: NeuroPathViewModel,
    profile: com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity,
    theme: WorldTheme,
    gradeObj: GradeLevel,
    lessonRecords: List<com.fourgeailabs.neuropath.data.local.entity.LessonRecordEntity>
) {
    // Focus Study Dashboard
    item {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            t("academic_productivity_studio"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            t("concept_mastery_socratic_tutoring_deep_work"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            tf("masteries", profile.totalStars),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // High School Deep Work Utilities (Pomodoro, Socratic AI, Sensory Reset)
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Socratic Chat Tool
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(AppScreen.NEURO_BUDDY_CHAT) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(t("socratic_ai"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(t("step_by_step_guidance"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Sensory Decompression
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(AppScreen.BREATHING_GUIDE) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(t("focus_pacing"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(t("decompress_align"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Advanced Academic Discipline Modules
    item {
        Text(
            t("academic_disciplines"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(EducationalSubject.entries) { subject ->
        val lessons = CurriculumCatalog.getLessonsForSubjectAndGrade(
            subject = subject,
            gradeLevel = gradeObj,
            stateStandardCode = profile.stateStandard,
            themeWorldId = profile.activeThemeId,
            country = profile.country
        )
        val activeLesson = lessons.firstOrNull()
        val record = lessonRecords.find { it.subjectId == subject.id }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    if (activeLesson != null) {
                        viewModel.startLesson(activeLesson)
                    }
                },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(subject.emoji, fontSize = 20.sp)
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(subject.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        activeLesson?.title ?: subject.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (activeLesson != null) viewModel.startLesson(activeLesson)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(t("study"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
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
