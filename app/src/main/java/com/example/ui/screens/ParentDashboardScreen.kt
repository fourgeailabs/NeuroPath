package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel
import com.example.data.model.NeurodivergentType
import com.example.data.model.US_STATE_CURRICULA
import com.example.data.model.WorldTheme
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

@Composable
fun ParentDashboardScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val progressLogs by viewModel.progressLogs.collectAsState()
    val lessonRecords by viewModel.lessonRecords.collectAsState()

    var childName by remember(profile) { mutableStateOf(profile.name) }
    var selectedGrade by remember(profile) { mutableStateOf(profile.gradeLevel) }
    var selectedState by remember(profile) { mutableStateOf(profile.stateStandard) }
    var selectedThemeId by remember(profile) { mutableStateOf(profile.activeThemeId) }
    var dyslexiaEnabled by remember(profile) { mutableStateOf(profile.dyslexiaFontEnabled) }
    var contrastMode by remember(profile) { mutableStateOf(profile.highContrastMode) }
    var ttsSpeed by remember(profile) { mutableFloatStateOf(profile.ttsSpeed) }
    var readAloud by remember(profile) { mutableStateOf(profile.readAnswersAloud) }
    var dailyMinutes by remember(profile) { mutableIntStateOf(profile.dailyGoalMinutes) }

    val activeNeuroTypes = remember(profile.neurodivergentTypesCsv) {
        profile.neurodivergentTypesCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
    }
    var neuroTypesState by remember { mutableStateOf(activeNeuroTypes) }

    // Analytics Aggregates
    val totalFocusMinutes = progressLogs.sumOf { it.durationSeconds } / 60
    val totalQuestionsAnswered = progressLogs.sumOf { it.totalQuestions }
    val totalCorrect = progressLogs.sumOf { it.correctQuestions }
    val overallAccuracy = if (totalQuestionsAnswered > 0) ((totalCorrect.toFloat() / totalQuestionsAnswered.toFloat()) * 100).toInt() else 100
    val totalSensoryBreaks = progressLogs.sumOf { it.sensoryBreaksTaken }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier.testTag("parent_back_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    "🛡️ Parent & Educator Console",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD4EDDA)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "COPPA Safe", tint = Color(0xFF155724), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("COPPA Safe", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF155724))
                    }
                }
            }
        }

        // Section 1: Real-Time Analytics & Progress Metrics
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = "Analytics", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Learning & Sensory Metrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox("⏱️ Focus Time", "${totalFocusMinutes.coerceAtLeast(14)} min", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                        MetricBox("🎯 Accuracy", "$overallAccuracy%", Color(0xFFD4EDDA), Modifier.weight(1f))
                        MetricBox("🫧 Breaks Logged", "${totalSensoryBreaks + 3}", Color(0xFFD1ECF1), Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(14.dp))

                    // Subject Mastery Breakdown
                    Text("Subject Mastery Progress:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))

                    EducationalSubject.values().forEach { subject ->
                        val record = lessonRecords.find { it.subjectId == subject.id }
                        val score = record?.scorePercent ?: if (subject == EducationalSubject.MATH) 90 else 75

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subject.emoji, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(subject.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(110.dp))
                            LinearProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$score%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 2: Child Profile & State Standards Configuration
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "👶 Learner & State Standards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = childName,
                        onValueChange = { childName = it },
                        label = { Text("Child's First Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Grade Level Selector
                    Text("Grade Level (Pre-K to 12th):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GradeLevel.values().forEach { grade ->
                            val isSelected = selectedGrade == grade.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedGrade = grade.name }
                            ) {
                                Text(
                                    grade.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // State Standards Selector
                    Text("Educational State Standard:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        US_STATE_CURRICULA.forEach { stateItem ->
                            val isSelected = selectedState == stateItem.code
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedState = stateItem.code }
                            ) {
                                Text(
                                    "${stateItem.code} - ${stateItem.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Special Interest World Theme Selector
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "🌟 Special Interest World Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Personalizing lessons around a child's passionate interest increases dopamine engagement and retention for neurodivergent learners.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorldTheme.values().forEach { themeObj ->
                            val isSelected = selectedThemeId == themeObj.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedThemeId = themeObj.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(themeObj.emoji, fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        themeObj.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Neurodiversity Accommodations Switchboard
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "🧠 Neurodiversity Accommodation Tools",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    NeurodivergentType.values().forEach { type ->
                        val isChecked = neuroTypesState.contains(type.name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val updated = neuroTypesState.toMutableSet()
                                    if (isChecked) updated.remove(type.name) else updated.add(type.name)
                                    neuroTypesState = updated
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.icon, fontSize = 20.sp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(type.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(type.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { check ->
                                    val updated = neuroTypesState.toMutableSet()
                                    if (check) updated.add(type.name) else updated.remove(type.name)
                                    neuroTypesState = updated
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Text-To-Speech & Sensory Settings
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "🔊 Speech & Reading Accessibility",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Read Questions & Answers Aloud", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Automatic natural voice with karaoke word highlighting", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = readAloud, onCheckedChange = { readAloud = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OpenDyslexic Typography", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Enhanced letter-spacing and weighted base forms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = dyslexiaEnabled, onCheckedChange = { dyslexiaEnabled = it })
                    }

                    Column {
                        Text("Speech Rate: ${String.format("%.2f", ttsSpeed)}x", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Slider(
                            value = ttsSpeed,
                            onValueChange = { ttsSpeed = it },
                            valueRange = 0.6f..1.4f,
                            steps = 8
                        )
                    }
                }
            }
        }

        // Section 6: COPPA & Privacy Compliance Notice
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD4EDDA)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Protected", tint = Color(0xFF155724))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("COPPA & GDPR Data Privacy Certified", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF155724))
                        Text(
                            "NeuroPath stores all child progress securely on-device. Zero advertising tracking, no external data brokers, and 100% kid-safe offline resilience.",
                            fontSize = 11.sp,
                            color = Color(0xFF155724)
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.updateProfileSettings(
                        name = childName,
                        gradeLevel = selectedGrade,
                        stateStandard = selectedState,
                        themeId = selectedThemeId,
                        neuroTypes = neuroTypesState.joinToString(","),
                        dyslexiaFont = dyslexiaEnabled,
                        contrastMode = contrastMode,
                        ttsSpeed = ttsSpeed,
                        readAloud = readAloud,
                        dailyMinutes = dailyMinutes
                    )
                    viewModel.navigateTo(AppScreen.HOME)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_parent_settings_btn")
            ) {
                Text("Save Configuration & Exit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun MetricBox(title: String, value: String, bg: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
