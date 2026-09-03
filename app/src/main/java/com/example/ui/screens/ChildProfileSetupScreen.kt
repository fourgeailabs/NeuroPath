package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.model.AgeGroupTier
import com.example.data.model.DIAGNOSIS_OPTIONS
import com.example.data.model.GLOBAL_EDUCATIONAL_LOCALES
import com.example.data.model.GradeLevel
import com.example.data.model.HYPER_FIXATION_OPTIONS
import com.example.data.model.NeuroThemeCatalog
import com.example.data.model.NeuroThemeCategory
import com.example.data.model.NeuroThemeData
import com.example.data.model.STRENGTH_OPTIONS
import com.example.data.model.STRUGGLE_OPTIONS
import com.example.data.model.ThemeRotationSchedule
import com.example.data.model.WorldTheme
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.util.LocationComplianceHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChildProfileSetupScreen(
    viewModel: NeuroPathViewModel,
    editingProfileId: Long? = null,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProfile by viewModel.currentProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val isVerifyingLocation by viewModel.isVerifyingLocation.collectAsState()

    val targetProfile = if (editingProfileId != null) {
        allProfiles.find { it.id == editingProfileId } ?: currentProfile
    } else {
        currentProfile
    }

    var childName by remember { mutableStateOf(if (editingProfileId != null) targetProfile.name else "") }
    var childAgeText by remember { mutableStateOf(if (editingProfileId != null) targetProfile.age.toString() else "7") }
    var selectedGrade by remember { mutableStateOf(GradeLevel.values().find { it.name == targetProfile.gradeLevel } ?: GradeLevel.GRADE_1) }
    var selectedAgeTier by remember {
        mutableStateOf(
            AgeGroupTier.values().find { it.id == targetProfile.ageGroupTier } ?: AgeGroupTier.ELEMENTARY
        )
    }

    val initialDiagnoses = remember {
        targetProfile.neurodivergentTypesCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
    }
    var selectedDiagnoses by remember { mutableStateOf(initialDiagnoses) }

    val initialStruggles = remember {
        targetProfile.strugglesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
    }
    var selectedStruggles by remember { mutableStateOf(initialStruggles) }

    val initialStrengths = remember {
        targetProfile.strengthsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
    }
    var selectedStrengths by remember { mutableStateOf(initialStrengths) }

    val initialHyperFixations = remember {
        targetProfile.hyperFixationsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
    }
    var selectedHyperFixations by remember { mutableStateOf(initialHyperFixations) }

    var activeThemeId by remember { mutableStateOf(targetProfile.activeThemeId) }
    var themeRotationSchedule by remember {
        mutableStateOf(ThemeRotationSchedule.fromId(targetProfile.themeRotationSchedule))
    }
    var showAllThemesDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<NeuroThemeCategory?>(null) }
    var themeSearchQuery by remember { mutableStateOf("") }
    var inspectingThemeData by remember { mutableStateOf<NeuroThemeData?>(null) }

    var postalCodeOverride by remember { mutableStateOf(targetProfile.zipOrPostalCodeOverride) }
    var configuredCountry by remember { mutableStateOf(targetProfile.country) }
    var configuredState by remember { mutableStateOf(targetProfile.stateOrProvince) }
    var configuredDistrict by remember { mutableStateOf(targetProfile.schoolDistrict) }
    var configuredStandard by remember { mutableStateOf(targetProfile.stateStandard) }
    var postalLookupMessage by remember { mutableStateOf<String?>(null) }

    var dyslexiaFont by remember { mutableStateOf(targetProfile.dyslexiaFontEnabled) }
    var highContrastMode by remember { mutableStateOf(targetProfile.highContrastMode) }
    var readAloudTts by remember { mutableStateOf(targetProfile.readAnswersAloud) }
    var ambientSound by remember { mutableStateOf(targetProfile.ambientSound) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val previewProfile = remember(
        childName, childAgeText, selectedGrade, selectedAgeTier,
        selectedDiagnoses, selectedStruggles, selectedStrengths, selectedHyperFixations
    ) {
        targetProfile.copy(
            name = childName.ifBlank { "Student" },
            age = childAgeText.toIntOrNull() ?: 7,
            gradeLevel = selectedGrade.name,
            ageGroupTier = selectedAgeTier.id,
            neurodivergentTypesCsv = selectedDiagnoses.joinToString(","),
            strugglesCsv = selectedStruggles.joinToString(", "),
            strengthsCsv = selectedStrengths.joinToString(", "),
            hyperFixationsCsv = selectedHyperFixations.joinToString(", ")
        )
    }

    val recommendedThemes = remember(previewProfile) {
        NeuroThemeCatalog.getRecommendedThemesForProfile(previewProfile, limit = 8)
    }

    val currentActiveThemeData = remember(activeThemeId, recommendedThemes) {
        NeuroThemeCatalog.findThemeById(
            if (activeThemeId.isNotBlank()) activeThemeId else (recommendedThemes.firstOrNull()?.id ?: "ancient_egypt")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editingProfileId != null || currentProfile.isInitialSetupComplete) {
                    IconButton(
                        onClick = {
                            if (editingProfileId != null) onFinished() else viewModel.navigateTo(AppScreen.PROFILE_SELECTION)
                        },
                        modifier = Modifier.testTag("profile_setup_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (editingProfileId != null) "Edit Learner Profile" else "Create Learner Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "🔒 100% Local On-Device Storage (Private & Safe)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Privacy & Local Save Safety Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "To guarantee absolute safety and privacy, all profiles and learning telemetry are stored strictly on this device in an encrypted local database. Cloud sync is disabled until certified secure servers are configured.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 1: Learner Identity & Age Tier
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "1. Learner Identity & School Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = childName,
                        onValueChange = {
                            childName = it
                            errorMessage = null
                        },
                        label = { Text("Learner's First Name / Nickname") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("child_name_input")
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = childAgeText,
                            onValueChange = {
                                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                    childAgeText = it
                                    val ageNum = it.toIntOrNull() ?: 7
                                    // Auto-suggest age tier
                                    selectedAgeTier = when {
                                        ageNum >= 14 -> AgeGroupTier.HIGH_SCHOOL
                                        ageNum >= 11 -> AgeGroupTier.MIDDLE_SCHOOL
                                        else -> AgeGroupTier.ELEMENTARY
                                    }
                                }
                            },
                            label = { Text("Age (Years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("child_age_input")
                        )

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Current Grade", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GradeLevel.values().forEach { g ->
                                    val isSelected = selectedGrade == g
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedGrade = g
                                            selectedAgeTier = when (g) {
                                                GradeLevel.HIGH_SCHOOL -> AgeGroupTier.HIGH_SCHOOL
                                                GradeLevel.GRADE_6, GradeLevel.GRADE_7, GradeLevel.GRADE_8 -> AgeGroupTier.MIDDLE_SCHOOL
                                                else -> AgeGroupTier.ELEMENTARY
                                            }
                                        },
                                        label = { Text(g.displayName, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Design Language & Interface Scale:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Choose the layout tailored to your child's age group so they are never stuck with an interface that feels too young or too complex.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgeGroupTier.values().forEach { tier ->
                            val isSelected = selectedAgeTier == tier
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAgeTier = tier }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tier.icon, fontSize = 24.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                tier.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "(${tier.ageRange})",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            tier.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Learning Disabilities & Differences (Diagnoses)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🧩", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "2. Learning Differences & Diagnoses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Select all that apply to customize AI scaffolding & pacing",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DIAGNOSIS_OPTIONS.forEach { opt ->
                            val isSelected = selectedDiagnoses.contains(opt.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDiagnoses = if (isSelected) {
                                        (selectedDiagnoses - opt.id).toMutableSet()
                                    } else {
                                        (selectedDiagnoses + opt.id).toMutableSet()
                                    }
                                },
                                label = { Text("${opt.emoji} ${opt.title}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: What Does The Learner Struggle With?
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "3. Key Learning Challenges / Focus Areas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "AI tutors scaffold these exact areas with targeted Socratic hints",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        STRUGGLE_OPTIONS.forEach { opt ->
                            val isSelected = selectedStruggles.contains(opt.title)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStruggles = if (isSelected) {
                                        (selectedStruggles - opt.title).toMutableSet()
                                    } else {
                                        (selectedStruggles + opt.title).toMutableSet()
                                    }
                                },
                                label = { Text("${opt.emoji} ${opt.title}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Learner's Strengths & Superpowers
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌟", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "4. Learner's Strengths & Superpowers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Lessons will leverage these natural gifts to teach new concepts",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        STRENGTH_OPTIONS.forEach { opt ->
                            val isSelected = selectedStrengths.contains(opt.title)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStrengths = if (isSelected) {
                                        (selectedStrengths - opt.title).toMutableSet()
                                    } else {
                                        (selectedStrengths + opt.title).toMutableSet()
                                    }
                                },
                                label = { Text("${opt.emoji} ${opt.title}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Hyper-Fixations & Passion Topics
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🦖", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "5. Hyper-Fixations & Favorite Topics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "The app themes problems & stories directly around their passions!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HYPER_FIXATION_OPTIONS.forEach { opt ->
                            val isSelected = selectedHyperFixations.contains(opt.title)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedHyperFixations = if (isSelected) {
                                        (selectedHyperFixations - opt.title).toMutableSet()
                                    } else {
                                        (selectedHyperFixations + opt.title).toMutableSet()
                                    }
                                    activeThemeId = opt.recommendedThemeId
                                },
                                label = { Text("${opt.emoji} ${opt.title}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 5B: 100-Theme Selection & Periodic Rotation Schedule
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎨", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "5B. Theme World & Rotation Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "100 immersive themes tailored to their personality profile & growth",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Active Theme Spotlight Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(currentActiveThemeData.cardHex),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(currentActiveThemeData.primaryHex)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentActiveThemeData.emoji, fontSize = 34.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            currentActiveThemeData.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color(currentActiveThemeData.primaryHex)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(currentActiveThemeData.primaryHex)
                                        ) {
                                            Text(
                                                currentActiveThemeData.category.title,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Companion Buddy: ${currentActiveThemeData.buddyName} (${currentActiveThemeData.buddyRole})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(currentActiveThemeData.primaryHex).copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                "\"${currentActiveThemeData.greeting}\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E212B)
                            )

                            Spacer(Modifier.height(10.dp))
                            // Subject Integration Badges
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.7f))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "🔢 Math: ${currentActiveThemeData.mathIntegration}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E212B)
                                )
                                Text(
                                    "📖 Reading: ${currentActiveThemeData.readingIntegration}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E212B)
                                )
                                Text(
                                    "🔬 Science: ${currentActiveThemeData.scienceIntegration}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E212B)
                                )
                                Text(
                                    "🏛️ Social Studies: ${currentActiveThemeData.socialStudiesIntegration}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E212B)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Theme Rotation Frequency Configuration
                    Text(
                        "Theme Rotation Preference:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Keep permanent or periodically rotate to fresh profile-matched worlds:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeRotationSchedule.values().forEach { schedule ->
                            val isSelected = themeRotationSchedule == schedule
                            FilterChip(
                                selected = isSelected,
                                onClick = { themeRotationSchedule = schedule },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(schedule.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(schedule.description, fontSize = 9.5.sp)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // AI Recommended Themes for Profile
                    Text(
                        "AI Recommended Themes For This Profile:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Synthesized based on diagnoses, strengths, struggles & interests:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recommendedThemes.forEach { theme ->
                            val isSelected = activeThemeId == theme.id || (activeThemeId.isBlank() && theme.id == recommendedThemes.firstOrNull()?.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { activeThemeId = theme.id },
                                label = { Text("${theme.emoji} ${theme.title}", fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Button to open full 100-Theme Catalog Browser
                    OutlinedButton(
                        onClick = { showAllThemesDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Browse Full Catalog (100 Themes)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 6: Regional Educational Jurisdiction, Location Services & Postal Code Override
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📍", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "6. Standards Jurisdiction & Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Detects state/province & school district curriculum standards",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Location Services Active Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Location Services: In Use",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF2E7D32)
                                    ) {
                                        Text(
                                            "LOCALE ONLY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Used only to identify educational jurisdiction for official standards.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Active Standards Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Current Educational Standards Mapping:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "🏛️ Jurisdiction: $configuredDistrict ($configuredState, $configuredCountry)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "📚 Standard: $configuredStandard Framework",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Manual Postal / Zip Code Override
                    Text(
                        "Postal Code / ZIP Override (Optional):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Set a specific zip or postal code if setting up outside the child's home school district.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = postalCodeOverride,
                            onValueChange = {
                                postalCodeOverride = it
                                postalLookupMessage = null
                            },
                            label = { Text("ZIP / Postal Code") },
                            placeholder = { Text("e.g. 90210, SW1A 1AA, M5V 2T6") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("postal_code_input")
                        )

                        Button(
                            onClick = {
                                if (postalCodeOverride.isNotBlank()) {
                                    viewModel.resolvePostalOrZipCode(context, postalCodeOverride) { res ->
                                        configuredCountry = res.matchedEducationalLocale?.countryName ?: res.detectedCountry
                                        configuredState = if (res.detectedState.isNotBlank()) res.detectedState else res.matchedEducationalLocale?.defaultStateOrProvince ?: configuredState
                                        configuredDistrict = if (res.detectedDistrict.isNotBlank()) res.detectedDistrict else res.matchedEducationalLocale?.schoolDistricts?.firstOrNull() ?: configuredDistrict
                                        configuredStandard = res.matchedEducationalLocale?.stateCurriculumStandards?.firstOrNull() ?: configuredStandard
                                        postalLookupMessage = "✅ Standards mapped to ${res.detectedDistrict} (${res.detectedState})"
                                    }
                                }
                            },
                            modifier = Modifier.testTag("apply_postal_code_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply")
                        }
                    }

                    if (postalLookupMessage != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = postalLookupMessage!!,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Privacy & Locale Disclaimer
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("🛡️", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    LocationComplianceHelper.PRIVACY_DISCLAIMER_TITLE,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    LocationComplianceHelper.PRIVACY_DISCLAIMER_TEXT,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Sensory & Accessibility Controls
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "7. Accessibility & Sensory Comfort",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    // Read Answers Aloud Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Text-To-Speech (Read Aloud)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Read question prompts aloud automatically (Default: OFF)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = readAloudTts,
                            onCheckedChange = { readAloudTts = it },
                            modifier = Modifier.testTag("setup_tts_switch")
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Dyslexia Font Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("OpenDyslexic Font Typography", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Weighted bottom-heavy letters for easier letter tracking", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = dyslexiaFont,
                            onCheckedChange = { dyslexiaFont = it },
                            modifier = Modifier.testTag("setup_dyslexia_switch")
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Save & Finish Action Button
        item {
            Button(
                onClick = {
                    if (childName.isBlank()) {
                        errorMessage = "Please enter the child's first name."
                        return@Button
                    }
                    val age = childAgeText.toIntOrNull() ?: 7
                    val finalProfile = targetProfile.copy(
                        name = childName.trim(),
                        age = age,
                        gradeLevel = selectedGrade.name,
                        ageGroupTier = selectedAgeTier.id,
                        neurodivergentTypesCsv = selectedDiagnoses.joinToString(","),
                        strugglesCsv = selectedStruggles.joinToString(", "),
                        strengthsCsv = selectedStrengths.joinToString(", "),
                        hyperFixationsCsv = selectedHyperFixations.joinToString(", "),
                        activeThemeId = if (activeThemeId.isNotBlank()) activeThemeId else (recommendedThemes.firstOrNull()?.id ?: "ancient_egypt"),
                        themeRotationSchedule = themeRotationSchedule.id,
                        lastThemeRotationTimestamp = System.currentTimeMillis(),
                        country = configuredCountry,
                        stateOrProvince = configuredState,
                        schoolDistrict = configuredDistrict,
                        stateStandard = configuredStandard,
                        zipOrPostalCodeOverride = postalCodeOverride.trim(),
                        dyslexiaFontEnabled = dyslexiaFont,
                        highContrastMode = highContrastMode,
                        readAnswersAloud = readAloudTts,
                        ambientSound = ambientSound,
                        isInitialSetupComplete = true
                    )

                    viewModel.saveAndActivateChildProfile(finalProfile) {
                        if (editingProfileId != null) {
                            onFinished()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_child_profile_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (editingProfileId != null) "Save Profile Changes" else "Create Profile & Start Adventure",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // 100-Theme Catalog Browser Modal Dialog
    if (showAllThemesDialog) {
        val filteredThemes = remember(selectedCategoryFilter, themeSearchQuery) {
            NeuroThemeCatalog.getAllThemes().filter { theme ->
                val matchesCategory = selectedCategoryFilter == null || theme.category == selectedCategoryFilter
                val matchesSearch = themeSearchQuery.isBlank() ||
                        theme.title.contains(themeSearchQuery, ignoreCase = true) ||
                        theme.buddyName.contains(themeSearchQuery, ignoreCase = true) ||
                        theme.bestForDiagnoses.any { it.contains(themeSearchQuery, ignoreCase = true) } ||
                        theme.bestForStrengths.any { it.contains(themeSearchQuery, ignoreCase = true) } ||
                        theme.mathIntegration.contains(themeSearchQuery, ignoreCase = true) ||
                        theme.scienceIntegration.contains(themeSearchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }

        AlertDialog(
            onDismissRequest = { showAllThemesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎨 100 Adaptive Neuro-Themes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = themeSearchQuery,
                        onValueChange = { themeSearchQuery = it },
                        placeholder = { Text("Search 100 themes, topics, subjects...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

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
                            label = { Text("All (100)", fontSize = 11.sp) }
                        )
                        NeuroThemeCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                },
                                label = { Text("${cat.emoji} ${cat.title}", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Found ${filteredThemes.size} themes:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredThemes.size) { index ->
                            val theme = filteredThemes[index]
                            val isSelected = activeThemeId == theme.id

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(theme.primaryHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(theme.primaryHex)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeThemeId = theme.id
                                        showAllThemesDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(theme.emoji, fontSize = 28.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                theme.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(theme.primaryHex)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(theme.primaryHex).copy(alpha = 0.8f)
                                            ) {
                                                Text(
                                                    theme.category.title,
                                                    fontSize = 8.5.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Buddy: ${theme.buddyName} (${theme.buddyRole})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "📐 Math: ${theme.mathIntegration}",
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            "🔬 Science: ${theme.scienceIntegration}",
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(theme.primaryHex),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllThemesDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
