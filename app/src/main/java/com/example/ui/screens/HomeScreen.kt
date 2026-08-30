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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.model.AgeGroupTier
import com.example.data.model.AvatarItem
import com.example.data.model.DEFAULT_AVATAR_SHOP_ITEMS
import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel
import com.example.data.model.WorldTheme
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
    val tier = AgeGroupTier.values().find { it.id == profile.ageGroupTier } ?: AgeGroupTier.ELEMENTARY

    val avatarItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.currentAvatarId }
    val hatItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedHatId }
    val petItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedPetId }
    val badgeItem = DEFAULT_AVATAR_SHOP_ITEMS.find { it.id == profile.equippedBadgeId }

    val gradeObj = GradeLevel.values().find { it.name == profile.gradeLevel } ?: GradeLevel.KINDERGARTEN
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
                                    "Downloading Curriculum for Offline Use...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF856404)
                                )
                                Text(
                                    "Saving interactive lessons & videos. You can learn anywhere!",
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
                                text = "${gradeObj.displayName} • ${tier.title}",
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
                                    "Profiles",
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
                                    "Shop",
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
                                    contentDescription = "Parent Dashboard",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "Parents",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 1.5 Location Services & Daily Curriculum Sync Status Bar
            item {
                val latestCurriculum by viewModel.latestCurriculum.collectAsState()
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Location Indicator
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📍", fontSize = 12.sp)
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Location Active",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2E7D32)
                                        ) {
                                            Text(
                                                "LOCALE ONLY",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "${profile.schoolDistrict} (${profile.stateOrProvince})",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Daily Curriculum Sync Status & Button
                            OutlinedButton(
                                onClick = { viewModel.syncDailyCurriculumForLocale(forceRefresh = true) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("daily_curriculum_sync_btn")
                            ) {
                                Text(
                                    text = if (isDownloadingCurriculum) "Syncing..." else "Sync Daily",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (latestCurriculum != null) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📚", fontSize = 13.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            "Standards: ${latestCurriculum?.standardTitle ?: profile.stateStandard}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Source: ${latestCurriculum?.officialSourceAgency ?: "Accredited Educational Standards"}",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read Quote",
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Spark of Inspiration",
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
    profile: com.example.data.local.entity.ChildProfileEntity,
    theme: WorldTheme,
    avatarItem: AvatarItem?,
    hatItem: AvatarItem?,
    petItem: AvatarItem?,
    gradeObj: GradeLevel,
    lessonRecords: List<com.example.data.local.entity.LessonRecordEntity>
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
                "🎮 Learning Games & Studio",
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
                        Text("Ocean Reading", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF004977))
                        Text("Word Safari", fontSize = 11.sp, color = Color(0xFF00629D))
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
                        Text("Art Studio", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF4A148C))
                        Text("Draw & Create", fontSize = 11.sp, color = Color(0xFF6A1B9A))
                    }
                }
            }
        }
    }

    // 20-Step Visual Safari Map
    item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "🎯 20-Step Quest Map",
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
            "📚 Learning Adventure Paths",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(EducationalSubject.values()) { subject ->
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
                        "Interactive Exploration • 20 adaptive steps",
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
                            contentDescription = "Start Lesson",
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
    profile: com.example.data.local.entity.ChildProfileEntity,
    theme: WorldTheme,
    avatarItem: AvatarItem?,
    gradeObj: GradeLevel,
    lessonRecords: List<com.example.data.local.entity.LessonRecordEntity>
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
                                "Quest Command Center",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Level ${1 + profile.totalStars / 10} Explorer • Streak: ${profile.currentStreakDays} Days",
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
                            "${profile.totalStars * 50} XP",
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
                "🎯 Daily Focus Missions",
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
                        Text("🧘 Reset & Calm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("4-7-8 Breathing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("🫧 Sensory Fidget", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Tactile Focus Loop", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Subject Mastery Modules
    item {
        Text(
            "📖 Academic Quest Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(EducationalSubject.values()) { subject ->
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
                    Text("Launch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    profile: com.example.data.local.entity.ChildProfileEntity,
    theme: WorldTheme,
    gradeObj: GradeLevel,
    lessonRecords: List<com.example.data.local.entity.LessonRecordEntity>
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
                            "🎓 Academic Productivity Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Concept Mastery • Socratic Tutoring • Deep Work",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "${profile.totalStars} Masteries",
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
                    Text("💡 Socratic AI", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Step-by-step guidance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("🧘 Focus Pacing", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("Decompress & align", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Advanced Academic Discipline Modules
    item {
        Text(
            "📚 Academic Disciplines",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(EducationalSubject.values()) { subject ->
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
                    Text("Study", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
