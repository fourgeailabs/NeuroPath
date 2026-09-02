package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.model.AgeGroupTier
import com.example.data.model.AppLanguage
import com.example.data.model.AppLanguageDictionary
import com.example.data.model.WorldTheme
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val allProfiles by viewModel.allProfiles.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val langCode = currentProfile.appLanguageCode

    var editingProfileId by remember { mutableStateOf<Long?>(null) }

    if (editingProfileId != null) {
        ChildProfileSetupScreen(
            viewModel = viewModel,
            editingProfileId = editingProfileId,
            onFinished = { editingProfileId = null }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header & Logo
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🧠", fontSize = 36.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = AppLanguageDictionary.getString("app_title", langCode),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = AppLanguageDictionary.getString("profile_selection_sub", langCode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Child Profile Tiles List
        items(allProfiles) { profile ->
            val tier = AgeGroupTier.values().find { it.id == profile.ageGroupTier } ?: AgeGroupTier.ELEMENTARY
            val theme = WorldTheme.values().find { it.id == profile.activeThemeId } ?: WorldTheme.DINOSAURS

            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(theme.surfaceHex)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectChildProfile(profile)
                    }
                    .testTag("profile_tile_${profile.id}")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar / Theme Icon
                        Surface(
                            shape = CircleShape,
                            color = Color(theme.primaryHex),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(theme.emoji, fontSize = 28.sp)
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (profile.name.isNotBlank()) profile.name else "Learner",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(theme.primaryHex)
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${tier.icon} ${tier.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "Age ${profile.age} • ${theme.title}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Edit Profile Icon
                        IconButton(
                            onClick = { editingProfileId = profile.id },
                            modifier = Modifier.testTag("edit_profile_${profile.id}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Hyperfixations & Strengths Chips
                    if (profile.hyperFixationsCsv.isNotBlank() || profile.strengthsCsv.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tags = (profile.hyperFixationsCsv.split(",") + profile.strengthsCsv.split(","))
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .take(3)

                            tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(theme.cardHex)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(theme.primaryHex),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Progress Stats & Play Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 14.sp)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "${profile.totalStars} Stars",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💎", fontSize = 14.sp)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "${profile.totalGems} Gems",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (profile.currentStreakDays > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔥", fontSize = 14.sp)
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        "${profile.currentStreakDays}d",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.selectChildProfile(profile) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(theme.primaryHex)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(AppLanguageDictionary.getString("start_lesson", langCode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Add Another Child Profile Button
        item {
            OutlinedButton(
                onClick = {
                    viewModel.navigateTo(AppScreen.CHILD_PROFILE_SETUP)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_child_profile_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(AppLanguageDictionary.getString("add_learner_profile", langCode), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Divider Spacer
        item {
            Spacer(Modifier.height(10.dp))
        }

        // Parent Dashboard Button Protected by PIN
        item {
            Button(
                onClick = {
                    viewModel.navigateTo(AppScreen.PARENT_PIN_GATE)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("parent_dashboard_gate_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = AppLanguageDictionary.getString("parent_console", langCode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Language & Privacy Footer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.LANGUAGE_SELECTION) }
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${AppLanguageDictionary.getString("app_language", langCode)}: ${AppLanguage.fromCode(langCode).displayName}",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
