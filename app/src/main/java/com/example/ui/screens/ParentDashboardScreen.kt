package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppLanguageDictionary
import com.example.data.model.EducationalLocaleManager
import com.example.data.model.EducationalSubject
import com.example.data.model.GLOBAL_EDUCATIONAL_LOCALES
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.model.AgeGroupTier
import com.example.data.model.GradeLevel
import com.example.data.model.LocaleLegalComplianceManager
import com.example.data.model.NeuroThemeCatalog
import com.example.data.model.NeuroThemeData
import com.example.data.model.NeurodivergentType
import com.example.data.model.ThemeRotationSchedule
import com.example.data.model.WorldTheme
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.ui.components.ThemePreviewModal
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh

enum class ParentDashboardTab(val title: String, val iconEmoji: String) {
    PROFILES("Child Profiles", "👥"),
    SETTINGS("Settings & AI", "⚙️"),
    STANDARDS("Standards & Locale", "🏛️"),
    DISCLAIMERS("Disclaimers & Legal", "⚖️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProfiles by viewModel.allProfiles.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()
    val progressLogs by viewModel.progressLogs.collectAsState()
    val lessonRecords by viewModel.lessonRecords.collectAsState()
    val isVerifyingLocation by viewModel.isVerifyingLocation.collectAsState()
    val locationComplianceResult by viewModel.locationComplianceResult.collectAsState()
    val oerUnits by viewModel.oerCurriculumUnits.collectAsState()
    val isOerSyncing by viewModel.isOerSyncing.collectAsState()
    val oerSyncResult by viewModel.oerSyncResult.collectAsState()
    val uriHandler = LocalUriHandler.current

    var selectedTab by remember { mutableStateOf(ParentDashboardTab.PROFILES) }
    var editingProfileId by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmForProfile by remember { mutableStateOf<ChildProfileEntity?>(null) }

    // Parent PIN Change State
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var pinMessageIsError by remember { mutableStateOf(false) }

    if (editingProfileId != null) {
        ChildProfileSetupScreen(
            viewModel = viewModel,
            editingProfileId = editingProfileId,
            onFinished = { editingProfileId = null }
        )
        return
    }

    var childName by remember(profile) { mutableStateOf(profile.name) }
    var selectedGrade by remember(profile) { mutableStateOf(profile.gradeLevel) }
    var selectedLanguageCode by remember(profile) { mutableStateOf(profile.appLanguageCode) }

    // Granular Educational Location State
    var country by remember(profile) { mutableStateOf(profile.country) }
    var stateOrProvince by remember(profile) { mutableStateOf(profile.stateOrProvince) }
    var city by remember(profile) { mutableStateOf(profile.city) }
    var schoolDistrict by remember(profile) { mutableStateOf(profile.schoolDistrict) }

    var selectedThemeId by remember(profile) { mutableStateOf(profile.activeThemeId) }
    var dyslexiaEnabled by remember(profile) { mutableStateOf(profile.dyslexiaFontEnabled) }
    var contrastMode by remember(profile) { mutableStateOf(profile.highContrastMode) }
    var ttsSpeed by remember(profile) { mutableFloatStateOf(profile.ttsSpeed) }
    var readAloud by remember(profile) { mutableStateOf(profile.readAnswersAloud) }
    var dailyMinutes by remember(profile) { mutableIntStateOf(profile.dailyGoalMinutes) }

    var expandedGrade by remember { mutableStateOf(false) }
    var expandedLanguage by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateStatusMessage by remember { mutableStateOf<String?>(null) }
    var showThemePreviewModal by remember { mutableStateOf(false) }
    var previewTargetThemeId by remember { mutableStateOf(selectedThemeId) }

    val activeNeuroTypes = remember(profile.neurodivergentTypesCsv) {
        profile.neurodivergentTypesCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
    }
    var neuroTypesState by remember { mutableStateOf(activeNeuroTypes) }

    // Analytics Aggregates
    val totalFocusMinutes = progressLogs.sumOf { it.durationSeconds } / 60
    val totalQuestionsAnswered = progressLogs.sumOf { it.totalQuestions }
    val totalCorrect = progressLogs.sumOf { it.correctQuestions }
    val overallAccuracy = if (totalQuestionsAnswered > 0) ((totalCorrect.toFloat() / totalQuestionsAnswered.toFloat()) * 100).toInt() else 0
    val totalSensoryBreaks = progressLogs.sumOf { it.sensoryBreaksTaken }

    val activeLang = AppLanguage.fromCode(selectedLanguageCode)
    val legalNotice = remember(country) { LocaleLegalComplianceManager.getComplianceNotice(country) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
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
                    "🛡️ ${AppLanguageDictionary.getString("parent_console", selectedLanguageCode)}",
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

        // Tab Navigation Bar
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
            ) {
                ParentDashboardTab.values().forEach { tab ->
                    val tabTitle = when (tab) {
                        ParentDashboardTab.PROFILES -> AppLanguageDictionary.getString("tab_profiles", selectedLanguageCode)
                        ParentDashboardTab.SETTINGS -> AppLanguageDictionary.getString("tab_settings", selectedLanguageCode)
                        ParentDashboardTab.STANDARDS -> AppLanguageDictionary.getString("tab_standards", selectedLanguageCode)
                        ParentDashboardTab.DISCLAIMERS -> AppLanguageDictionary.getString("tab_disclaimers", selectedLanguageCode)
                    }
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                "${tab.iconEmoji} $tabTitle",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            // TAB: CHILD PROFILES MANAGEMENT (LOCAL SAVE ONLY)
            ParentDashboardTab.PROFILES -> {
                // Section Header & Privacy Assurance
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "On-Device Child Profiles & Privacy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "All child profiles, hyper-fixations, strengths, and IEP records are stored strictly on this device. Create, edit, or remove profiles anytime.",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Child Profiles List
                items(allProfiles) { p ->
                    val isActive = p.id == profile.id
                    val pTheme = WorldTheme.values().find { it.id == p.activeThemeId } ?: WorldTheme.DINOSAURS
                    val pTier = AgeGroupTier.values().find { it.id == p.ageGroupTier } ?: AgeGroupTier.ELEMENTARY

                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isActive) Color(pTheme.surfaceHex) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(pTheme.primaryHex),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(pTheme.emoji, fontSize = 22.sp)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (p.name.isNotBlank()) p.name else "Learner",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = if (isActive) Color(pTheme.primaryHex) else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isActive) {
                                                Spacer(Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        "ACTIVE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "Age ${p.age} • ${pTier.title} • ${pTheme.title}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingProfileId = p.id },
                                        modifier = Modifier.testTag("parent_edit_profile_${p.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    if (allProfiles.size > 1) {
                                        IconButton(
                                            onClick = { showDeleteConfirmForProfile = p },
                                            modifier = Modifier.testTag("parent_delete_profile_${p.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Profile", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }

                            // Strengths & Diagnoses summary
                            if (p.strengthsCsv.isNotBlank() || p.hyperFixationsCsv.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Passions: ${p.hyperFixationsCsv.ifBlank { "Curiosity & Learning" }}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⭐ ${p.totalStars} Stars • 💎 ${p.totalGems} Gems • 🔥 ${p.currentStreakDays}d Streak",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!isActive) {
                                    Button(
                                        onClick = { viewModel.selectChildProfile(p) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("switch_to_profile_${p.id}")
                                    ) {
                                        Text("Select Active", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Child Profile Button
                item {
                    OutlinedButton(
                        onClick = {
                            viewModel.navigateTo(AppScreen.CHILD_PROFILE_SETUP)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("parent_add_profile_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Another Child Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Parent PIN Management Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Change Parent Passcode (PIN)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Set a secure 4-digit code required to access this dashboard and switch critical settings.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = newPinText,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinText = it },
                                    label = { Text("New PIN (4 digits)") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f).testTag("new_parent_pin_input")
                                )

                                OutlinedTextField(
                                    value = confirmPinText,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinText = it },
                                    label = { Text("Confirm PIN") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f).testTag("confirm_parent_pin_input")
                                )
                            }

                            if (pinMessage != null) {
                                Text(
                                    text = pinMessage!!,
                                    color = if (pinMessageIsError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    when {
                                        newPinText.length != 4 -> {
                                            pinMessage = "PIN must be exactly 4 digits."
                                            pinMessageIsError = true
                                        }
                                        newPinText != confirmPinText -> {
                                            pinMessage = "PINs do not match. Please re-enter."
                                            pinMessageIsError = true
                                        }
                                        else -> {
                                            viewModel.updateParentPin(newPinText)
                                            pinMessage = "Passcode updated successfully across all profiles!"
                                            pinMessageIsError = false
                                            newPinText = ""
                                            confirmPinText = ""
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End).testTag("save_parent_pin_btn")
                            ) {
                                Text("Update PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // TAB 1: SETTINGS & GENERAL CONFIGURATION
            ParentDashboardTab.SETTINGS -> {
                // Global App Language Selector
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "🌐 ${AppLanguageDictionary.getString("app_language", selectedLanguageCode)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "Multi-language support for UI, Voice Assist tutoring, speech synthesis, and hints.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            ExposedDropdownMenuBox(
                                expanded = expandedLanguage,
                                onExpandedChange = { expandedLanguage = !expandedLanguage }
                            ) {
                                OutlinedTextField(
                                    value = "${activeLang.flagEmoji} ${activeLang.displayName} (${activeLang.nativeName})",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedLanguage,
                                    onDismissRequest = { expandedLanguage = false }
                                ) {
                                    AppLanguage.values().forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text("${lang.flagEmoji} ${lang.displayName} - ${lang.nativeName}") },
                                            onClick = {
                                                selectedLanguageCode = lang.code
                                                expandedLanguage = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Child Profile Configuration
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "👶 Learner Profile",
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
                            ExposedDropdownMenuBox(
                                expanded = expandedGrade,
                                onExpandedChange = { expandedGrade = !expandedGrade }
                            ) {
                                OutlinedTextField(
                                    value = GradeLevel.values().find { it.name == selectedGrade }?.displayName ?: selectedGrade,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGrade) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedGrade,
                                    onDismissRequest = { expandedGrade = false }
                                ) {
                                    GradeLevel.values().forEach { grade ->
                                        DropdownMenuItem(
                                            text = { Text(grade.displayName) },
                                            onClick = {
                                                selectedGrade = grade.name
                                                expandedGrade = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Special Interest Theme Selector & Live Palette Visualizer
                item {
                    val activeThemeData = remember(selectedThemeId) { NeuroThemeCatalog.findThemeById(selectedThemeId) }
                    val themePrimaryColor = Color(activeThemeData.primaryHex)
                    val themeSecondaryColor = Color(activeThemeData.secondaryHex)

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme Palette", tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "🌟 Special Interest Neuro-Theme",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "100 Themes Library",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                "Personalizing lessons around a child's passionate interest increases dopamine engagement and retention for neurodivergent learners.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Active Theme Showcase Card with Color Swatches & Preview Action
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(activeThemeData.cardHex),
                                border = androidx.compose.foundation.BorderStroke(2.dp, themePrimaryColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(activeThemeData.emoji, fontSize = 28.sp)
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    activeThemeData.title,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = themePrimaryColor
                                                )
                                                Text(
                                                    "${activeThemeData.buddyName} • ${activeThemeData.category.title}",
                                                    fontSize = 11.sp,
                                                    color = themePrimaryColor.copy(alpha = 0.85f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        // Mini Palette Swatches
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Surface(shape = CircleShape, color = themePrimaryColor, modifier = Modifier.size(16.dp)) {}
                                            Surface(shape = CircleShape, color = themeSecondaryColor, modifier = Modifier.size(16.dp)) {}
                                            Surface(shape = CircleShape, color = Color(activeThemeData.surfaceHex), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), modifier = Modifier.size(16.dp)) {}
                                        }
                                    }

                                    Text(
                                        "\"${activeThemeData.greeting}\"",
                                        fontSize = 11.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF1E212B)
                                    )

                                    // Main Preview Action Button
                                    Button(
                                        onClick = {
                                            previewTargetThemeId = selectedThemeId
                                            showThemePreviewModal = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = themePrimaryColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("open_theme_preview_modal_btn")
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Preview Palette, Atmosphere & Assets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            Text("Quick Switch or Preview Popular Theme Worlds:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val topThemes = remember { NeuroThemeCatalog.getRecommendedThemesForProfile(profile, limit = 12) }
                                topThemes.forEach { themeObj ->
                                    val isSelected = selectedThemeId == themeObj.id
                                    val itemColor = Color(themeObj.primaryHex)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, itemColor) else null,
                                        modifier = Modifier.clickable {
                                            selectedThemeId = themeObj.id
                                            previewTargetThemeId = themeObj.id
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(themeObj.emoji, fontSize = 16.sp)
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                themeObj.title.substringBefore(":"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable {
                                                        previewTargetThemeId = themeObj.id
                                                        showThemePreviewModal = true
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Default.Visibility,
                                                        contentDescription = "Preview Theme",
                                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
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

                // Neurodiversity Accommodations
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

                // Speech & Accessibility
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

                // AI Engine Service Info (Google AI Studio Gemini)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Settings", tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "🤖 AI Engine Service",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Google AI Studio Gemini Integration Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Text(
                                "NeuroPath is powered directly by Google AI Studio Gemini API offerings (gemini-1.5-flash and gemini-1.5-pro) provided natively by the AI Studio environment.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Analytics & Progress Metrics
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
                                MetricBox("⏱️ Focus Time", "$totalFocusMinutes min", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                                MetricBox("🎯 Accuracy", "$overallAccuracy%", Color(0xFFD4EDDA), Modifier.weight(1f))
                                MetricBox("🫧 Breaks Logged", "$totalSensoryBreaks", Color(0xFFD1ECF1), Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(14.dp))

                            Text("Subject Mastery Progress:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))

                            EducationalSubject.values().forEach { subject ->
                                val record = lessonRecords.find { it.subjectId == subject.id }
                                val score = record?.scorePercent ?: 0

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
            }

            // TAB 2: EDUCATIONAL STANDARDS & LOCALE VERIFICATION
            ParentDashboardTab.STANDARDS -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Public, contentDescription = "Locale", tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "🏛️ ${AppLanguageDictionary.getString("educational_locale", selectedLanguageCode)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                "Pulls and strictly locks educational curriculum requirements tailored to your child's home country, province/state, city, and district. Ensures the child cannot access foreign curricula or knowledge meant for other countries.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Auto-Detect Location Button
                            Button(
                                onClick = {
                                    viewModel.detectLocationCompliance(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isVerifyingLocation) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Verifying Home Country Standards...", fontSize = 13.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("📍 Auto-Detect & Lock Home Country Locale", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            locationComplianceResult?.let { res ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("✅ Verified Locale: ${res.detectedCountry} (${res.detectedState})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                        Text("Academic Framework: ${res.educationalStandard}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        Text("Source: ${res.verificationSource}", fontSize = 10.sp, color = Color(0xFF388E3C))
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Quick Presets
                            Text("Quick-Select Global District Presets:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(GLOBAL_EDUCATIONAL_LOCALES) { loc ->
                                    val isSelected = schoolDistrict.contains(loc.schoolDistrict, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            country = loc.country
                                            stateOrProvince = loc.stateOrProvince
                                            city = loc.city
                                            schoolDistrict = loc.schoolDistrict
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(loc.flagEmoji, fontSize = 14.sp)
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "${loc.city} - ${loc.schoolDistrict.take(18)}...",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Granular Dropdowns
                            var expandedCountryList by remember { mutableStateOf(false) }
                            var expandedStateList by remember { mutableStateOf(false) }
                            var expandedCityList by remember { mutableStateOf(false) }
                            var expandedDistrictList by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expandedCountryList,
                                onExpandedChange = { expandedCountryList = !expandedCountryList }
                            ) {
                                OutlinedTextField(
                                    value = country,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Country") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountryList) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCountryList,
                                    onDismissRequest = { expandedCountryList = false }
                                ) {
                                    EducationalLocaleManager.getCountries().forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c) },
                                            onClick = {
                                                country = c
                                                stateOrProvince = ""
                                                city = ""
                                                schoolDistrict = ""
                                                expandedCountryList = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = expandedStateList,
                                onExpandedChange = { expandedStateList = !expandedStateList }
                            ) {
                                OutlinedTextField(
                                    value = stateOrProvince,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("State / Province / Region") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStateList) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedStateList,
                                    onDismissRequest = { expandedStateList = false }
                                ) {
                                    EducationalLocaleManager.getStatesForCountry(country).forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s) },
                                            onClick = {
                                                stateOrProvince = s
                                                city = ""
                                                schoolDistrict = ""
                                                expandedStateList = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = expandedCityList,
                                onExpandedChange = { expandedCityList = !expandedCityList }
                            ) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("City") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCityList) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCityList,
                                    onDismissRequest = { expandedCityList = false }
                                ) {
                                    EducationalLocaleManager.getCitiesForState(country, stateOrProvince).forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c) },
                                            onClick = {
                                                city = c
                                                schoolDistrict = ""
                                                expandedCityList = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = expandedDistrictList,
                                onExpandedChange = { expandedDistrictList = !expandedDistrictList }
                            ) {
                                OutlinedTextField(
                                    value = schoolDistrict,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("School District / Local Education Authority") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrictList) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDistrictList,
                                    onDismissRequest = { expandedDistrictList = false }
                                ) {
                                    EducationalLocaleManager.getDistrictsForCity(country, stateOrProvince, city).forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d.schoolDistrict) },
                                            onClick = {
                                                schoolDistrict = d.schoolDistrict
                                                expandedDistrictList = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Active District Framework Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Active District", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Locked Curriculum Standard:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "$schoolDistrict ($city, $stateOrProvince, $country)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "🔒 Foreign Curriculum Restricted: Only educational content aligned with $country standards is active.",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Pre-Installed OER Commons Curated Collection
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("oer_commons_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("📚", fontSize = 18.sp)
                                        }
                                    }
                                    Column {
                                        Text(
                                            "OER Commons Curated Collection",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Pre-installed K-12 Open Educational Resources",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "${oerUnits.size} Units Cached",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                "All K-12 core curriculum benchmarks (Mathematics, English Language Arts, Sciences, Social Studies & Civics) are pre-installed offline and accessible to the AI tutor. Sourced from the curated collection at oercommons.org/curated-collections.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (oerSyncResult != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (oerSyncResult!!.isSuccess) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (oerSyncResult!!.isSuccess) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                            contentDescription = "Sync Status",
                                            tint = if (oerSyncResult!!.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            oerSyncResult!!.message,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.syncOerCommonsCurriculum() },
                                    enabled = !isOerSyncing,
                                    modifier = Modifier.weight(1f).testTag("sync_oer_btn")
                                ) {
                                    if (isOerSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Syncing OER...", fontSize = 12.sp)
                                    } else {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Sync OER Collection", fontSize = 12.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { uriHandler.openUri("https://oercommons.org/curated-collections") },
                                    modifier = Modifier.testTag("open_oer_commons_url_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Public, contentDescription = "Website", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("OER Hub", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: DISCLAIMERS & LEGAL COMPLIANCE
            ParentDashboardTab.DISCLAIMERS -> {
                // Section: AI Mistakes Warning
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "AI Disclaimer",
                                    tint = Color(0xFF856404)
                                )
                                Text(
                                    "⚠️ AI Accuracy & Fallibility Warning",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF856404)
                                )
                            }
                            Text(
                                legalNotice.aiMistakesWarning,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }

                // Section: Internet & Cloud AI Connection Notice
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Internet Notice",
                                    tint = Color(0xFF0D47A1)
                                )
                                Text(
                                    "🌐 Internet Access & Cloud AI Disclaimer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1)
                                )
                            }
                            Text(
                                legalNotice.internetAccessNotice,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF0D47A1)
                            )
                        }
                    }
                }

                // Section: Location & Strict Curriculum Restriction Notice
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Location Compliance",
                                    tint = Color(0xFF1B5E20)
                                )
                                Text(
                                    "📍 Location Data & Localized Standards Assurance",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Text(
                                legalNotice.locationCurriculumNotice,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }

                // Section: Dynamic Locale Regulatory Framework
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = "Legal", tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "📜 ${legalNotice.countryName} Terms & Conditions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                "Governing Legal Framework: ${legalNotice.governingLaw}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                "Review the localized Terms of Service, Educational AI Disclaimer, COPPA/GDPR compliance, and student data protection policies at any time.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = { showTermsDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("view_terms_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("View Full ${legalNotice.countryName} Terms & Conditions", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // About FourgeAI LABS Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            AppLanguageDictionary.getString("about_app", selectedLanguageCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text("App Version: v${com.example.BuildConfig.VERSION_NAME} • Locale: $country", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showWhatsNewDialog = true },
                            modifier = Modifier.weight(1f).testTag("whats_new_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(AppLanguageDictionary.getString("whats_new", selectedLanguageCode), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        Button(
                            onClick = {
                                updateStatusMessage = "Checking GitHub Releases..."
                                showUpdateDialog = true
                            },
                            modifier = Modifier.weight(1f).testTag("check_updates_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(AppLanguageDictionary.getString("check_updates", selectedLanguageCode), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }

                    Text(
                        "Audio Soundscapes: Audio sample arrangements provided via Epidemic Sound (https://www.epidemicsound.com/sound-effects/)",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        "AI Pedagogical Guidance Rule: AI Learning Buddy adheres to strict Socratic scaffolding (never gives direct answers immediately) and cites the official curriculum framework reference document used.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        "Created by FourgeAI LABS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://github.com/fourgeailabs")
                            }
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        "App Repository: https://github.com/fourgeailabs/neuropath",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://github.com/fourgeailabs/neuropath")
                            }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Save & Apply Button
        item {
            Button(
                onClick = {
                    viewModel.updateProfileSettingsWithLocale(
                        name = childName,
                        gradeLevel = selectedGrade,
                        stateStandard = "DISTRICT",
                        country = country,
                        stateOrProvince = stateOrProvince,
                        city = city,
                        schoolDistrict = schoolDistrict,
                        appLanguageCode = selectedLanguageCode,
                        themeId = selectedThemeId,
                        neuroTypes = neuroTypesState.joinToString(","),
                        dyslexiaFont = dyslexiaEnabled,
                        contrastMode = contrastMode,
                        ttsSpeed = ttsSpeed,
                        readAloud = readAloud,
                        dailyMinutes = dailyMinutes,
                        customAiPlatform = "Google Gemini AI",
                        customApiKey = ""
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
                Text(AppLanguageDictionary.getString("save_config", selectedLanguageCode), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showTermsDialog) {
        TermsAndConditionsDialog(
            country = country,
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showWhatsNewDialog) {
        WhatsNewDialog(
            onDismiss = { showWhatsNewDialog = false }
        )
    }

    if (showUpdateDialog) {
        UpdateCheckDialog(
            currentVersion = "1.12.00",
            statusMessage = updateStatusMessage,
            onDismiss = { showUpdateDialog = false },
            onRemindLater = {
                showUpdateDialog = false
            },
            onSkipVersion = {
                showUpdateDialog = false
            }
        )
    }

    if (showThemePreviewModal) {
        ThemePreviewModal(
            initialThemeId = previewTargetThemeId,
            currentActiveThemeId = selectedThemeId,
            initialRotationSchedule = ThemeRotationSchedule.fromId(profile.themeRotationSchedule),
            onDismiss = { showThemePreviewModal = false },
            onApplyTheme = { themeId, schedule ->
                selectedThemeId = themeId
                viewModel.setActiveNeuroTheme(themeId)
                viewModel.setThemeRotationSchedule(schedule)
                showThemePreviewModal = false
            }
        )
    }

    if (showDeleteConfirmForProfile != null) {
        val target = showDeleteConfirmForProfile!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmForProfile = null },
            title = {
                Text("Delete Profile?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to permanently delete the profile for \"${target.name.ifBlank { "this learner" }}\"? All local progress and stars for this child will be erased.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChildProfile(target.id)
                        showDeleteConfirmForProfile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmForProfile = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TermsAndConditionsDialog(
    country: String,
    onDismiss: () -> Unit
) {
    val legalNotice = remember(country) {
        LocaleLegalComplianceManager.getComplianceNotice(country)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("${legalNotice.countryName} Terms & Conditions", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // AI Disclaimer Banner
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(legalNotice.aiMistakesWarning, fontSize = 11.sp, color = Color(0xFF856404), lineHeight = 15.sp)
                    }
                }

                // Internet Access Banner
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF0D47A1), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(legalNotice.internetAccessNotice, fontSize = 11.sp, color = Color(0xFF0D47A1), lineHeight = 15.sp)
                    }
                }

                // Location & Curriculum Restriction
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(legalNotice.locationCurriculumNotice, fontSize = 11.sp, color = Color(0xFF1B5E20), lineHeight = 15.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                legalNotice.termsSections.forEach { sec ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(sec.sectionTitle, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary)
                        Text(sec.content, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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

data class ReleaseUpdateNote(
    val version: String,
    val date: String,
    val highlights: List<String>
)

@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit
) {
    val updates = remember {
        listOf(
            ReleaseUpdateNote(
                version = "v1.10.00 (Current)",
                date = "Latest Update",
                highlights = listOf(
                    "🎬 OER Commons Video Player & Animated Visualizer: Interactive educational video player with animated Canvas visual simulations, speed toggles (0.5x - 2.0x), and scrub bar.",
                    "🎧 OER Auditory Curriculum & Story Podcast: Audio lecture player with real-time audio waveform spectrum visualizer and synchronized Karaoke TTS read-aloud.",
                    "💡 In-Video Socratic Checkpoints: Video and audio playback automatically pauses at key timestamps for interactive conceptual check-ins and explanations.",
                    "🚀 K-12 Course Integration: 1-tap direct launch from TeachLessonScreen and the OER Curated Collections Browser Sheet across all K-12 subjects."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.09.00",
                date = "Previous Update",
                highlights = listOf(
                    "💬 Rebuilt Educational Chat Interface: Powered by the Gemini API with full Free Model Gemini Chatbot support (gemini-3.5-flash & gemini-3.1-flash-lite).",
                    "🌐 Full OER Commons Curated Access: Interactive browser modal with direct links to https://oercommons.org/curated-collections, standard code alignment, and one-tap Socratic practice problem solving.",
                    "📜 Persistent Message History: Room-backed session management, keyword searching, bookmarks, and topic branching.",
                    "🎯 Personalized Explanation Modes: Instant switching between Step-by-Step (Socratic), Simpler Analogy (ELI5), Visual Breakdown, Deep Concept, and Direct Answer styles."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.08.00",
                date = "Previous Update",
                highlights = listOf(
                    "🧘 4-7-8 Breathing Exercise Visualizer: Built an interactive animated breathing component on Jetpack Compose Canvas featuring blooming lotus petals, fluid concentric waves, and cosmic orbital spheres.",
                    "⏱️ Rhythmic Real-Time Pacing: Guided feedback HUD with dynamic color shifts (Teal Inhale 4s, Golden Hold 7s, Violet Exhale 8s) and continuous smooth circular progress arc.",
                    "📊 Segmented Phase Timeline: Horizontal rhythm timeline tracking exact elapsed progress across 4s, 7s, and 8s intervals.",
                    "🎶 Ambient Soundscapes & Vagus Calm Meter: Background soothing audio with live Vagus Nerve Calm Index tracking."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.07.00",
                date = "Previous Update",
                highlights = listOf(
                    "🧘 4-7-8 Breathing Exercise Visualizer: Built an interactive animated breathing component on Jetpack Compose Canvas.",
                    "⏱️ Rhythmic Real-Time Pacing: Guided feedback HUD with dynamic color shifts."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.06.00",
                date = "Previous Update",
                highlights = listOf(
                    "📚 Pre-Installed OER Commons K-12 Collection: Integrated curated open educational resources pre-installed and cached locally in Room Database covering Kindergarten through Grade 12 (Math, ELA, Sciences, Social Studies & Civics).",
                    "🔍 Online & Offline OER Service: Real-time synchronization and offline fallback parser for oercommons.org curated collections.",
                    "🧠 Curriculum-Aware AI Tutor: Gemini AI tutor and Voice Assist automatically query OER Commons materials to tailor explanations and practice to exact grade benchmarks in all 21 supported languages."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.05.00",
                date = "Previous Update",
                highlights = listOf(
                    "🌐 21-Language Global Localization: Complete end-to-end multi-language dictionary across English (US/UK), Spanish, French, German, Mandarin, Japanese, Korean, Portuguese, Italian, Dutch, Swedish, Russian, Turkish, Polish, Greek, Vietnamese, Thai, Indonesian, Hindi, and Arabic.",
                    "⚡ Instant Reactive Switching: Switching languages in Setup, Parent Settings, or Dashboard immediately adapts every UI screen, dialog, button, and educational instruction.",
                    "🎯 Full Coverage: All menus, dialogs, 'What's New', 'Check for Updates', fidgets, sensory suites, and Socratic tutoring components are seamlessly translated."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.04.00",
                date = "Previous Update",
                highlights = listOf(
                    "🌐 Global Language Change: Instant reactive localization for all screens, settings, setup flows, sensory tools, and lessons across initial languages.",
                    "🧠 Socratic AI Research Assistant: Deep curriculum retrieval engine grounded in official localized standards and downloaded offline materials.",
                    "📚 OER Commons K-12 Curated Collections: Integrated curated open educational resources across elementary, middle, and high school grades."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.03.00",
                date = "Previous Update",
                highlights = listOf(
                    "🔬 Socratic AI Scaffolding Engine: AI Learning Buddy guides students step-by-step through inquiry rather than providing direct answers.",
                    "🏫 Full K-12 Grade Tier Routing: Dynamic UI modes for Elementary Explorer, Middle School Command Center, and High School Academic Studio."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.02.00",
                date = "Previous Update",
                highlights = listOf(
                    "📍 Granular Location & Educational Standards: Country, state/province, city, and school district alignment.",
                    "⚖️ Multi-Jurisdiction Compliance: Dedicated legal frameworks for US COPPA/FERPA, EU GDPR-K, UK Children's Code, Australia Privacy Act, and more."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.01.00",
                date = "Previous Update",
                highlights = listOf(
                    "🎵 Google Lyria AI Music Generation: Real-time procedural ambient soundscapes tailored to student sensory needs.",
                    "📖 Dyslexia-friendly typography & high contrast sensory modes."
                )
            ),
            ReleaseUpdateNote(
                version = "v1.00.00",
                date = "Initial Launch",
                highlights = listOf(
                    "🎉 Initial NeuroPath Learning Release: Multi-profile neurodivergent education suite with local on-device database.",
                    "🫧 Sensory Fidget Suite: Pop-It, 4-7-8 Breathing, Ocean Explorer, and Creative Studio."
                )
            )
        )
    }

    var openedIndex by remember { mutableStateOf<Int?>(null) } // starts closed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("What's New in NeuroPath", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Explore the latest enhancements and version history below. Tap any version to view its release notes.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                updates.forEachIndexed { index, item ->
                    val isExpanded = openedIndex == index
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                openedIndex = if (isExpanded) null else index
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        item.version,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(item.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isExpanded) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                item.highlights.forEach { highlight ->
                                    Text(
                                        highlight,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun UpdateCheckDialog(
    currentVersion: String,
    statusMessage: String?,
    onDismiss: () -> Unit,
    onRemindLater: () -> Unit,
    onSkipVersion: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("App Update Status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Current Installed Version: v$currentVersion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD4EDDA),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF155724), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "You are running the latest build available from GitHub Actions & Releases.",
                            fontSize = 12.sp,
                            color = Color(0xFF155724)
                        )
                    }
                }
                Text(
                    "GitHub Releases & Actions automatically build APK releases on repository push.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    uriHandler.openUri("https://github.com/fourgeailabs/neuropath/releases")
                    onDismiss()
                }
            ) {
                Text("View Releases")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onRemindLater) {
                    Text("Remind Later")
                }
                TextButton(onClick = onSkipVersion) {
                    Text("Skip Version")
                }
            }
        }
    )
}
