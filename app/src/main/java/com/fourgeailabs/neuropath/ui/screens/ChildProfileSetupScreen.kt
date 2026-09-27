package com.fourgeailabs.neuropath.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.GpsFixed
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
import androidx.compose.runtime.LaunchedEffect
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
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.model.AgeGroupTier
import com.fourgeailabs.neuropath.data.model.DIAGNOSIS_OPTIONS
import com.fourgeailabs.neuropath.data.model.GLOBAL_EDUCATIONAL_LOCALES
import com.fourgeailabs.neuropath.data.model.GradeLevel
import com.fourgeailabs.neuropath.data.model.HYPER_FIXATION_OPTIONS
import com.fourgeailabs.neuropath.data.model.NeuroThemeCatalog
import com.fourgeailabs.neuropath.data.model.NeuroThemeCategory
import com.fourgeailabs.neuropath.data.model.NeuroThemeData
import com.fourgeailabs.neuropath.data.model.STRENGTH_OPTIONS
import com.fourgeailabs.neuropath.data.model.STRUGGLE_OPTIONS
import com.fourgeailabs.neuropath.data.model.ThemeRotationSchedule
import com.fourgeailabs.neuropath.data.model.WorldTheme
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.components.ThemePreviewModal
import com.fourgeailabs.neuropath.util.LocationComplianceHelper
import androidx.compose.material.icons.filled.Visibility
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf

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
    val locationComplianceResult by viewModel.locationComplianceResult.collectAsState()

    // Runtime location permission: the scan button requests it on demand so the
    // GPS/network path in detectLocationCompliance can actually run.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Run detection regardless — the helper falls back gracefully if denied.
        viewModel.detectLocationCompliance(context)
    }

    val targetProfile = if (editingProfileId != null) {
        allProfiles.find { it.id == editingProfileId } ?: currentProfile
    } else {
        currentProfile
    }

    var childName by remember { mutableStateOf(if (editingProfileId != null) targetProfile.name else "") }
    var childAgeText by remember { mutableStateOf(if (editingProfileId != null) targetProfile.age.toString() else "7") }
    var selectedGrade by remember { mutableStateOf(GradeLevel.entries.find { it.name == targetProfile.gradeLevel } ?: GradeLevel.GRADE_1) }
    var selectedAgeTier by remember {
        mutableStateOf(
            AgeGroupTier.entries.find { it.id == targetProfile.ageGroupTier } ?: AgeGroupTier.ELEMENTARY
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
    var showThemePreviewModal by remember { mutableStateOf(false) }
    var previewModalThemeId by remember { mutableStateOf(activeThemeId) }
    var selectedCategoryFilter by remember { mutableStateOf<NeuroThemeCategory?>(null) }
    var themeSearchQuery by remember { mutableStateOf("") }
    var inspectingThemeData by remember { mutableStateOf<NeuroThemeData?>(null) }

    var postalCodeOverride by remember { mutableStateOf(targetProfile.zipOrPostalCodeOverride) }
    var configuredCountry by remember { mutableStateOf(targetProfile.country) }
    var configuredState by remember { mutableStateOf(targetProfile.stateOrProvince) }
    var configuredDistrict by remember { mutableStateOf(targetProfile.schoolDistrict) }
    var configuredStandard by remember { mutableStateOf(targetProfile.stateStandard) }
    var postalLookupMessage by remember { mutableStateOf<String?>(null) }

    // When a GPS scan (or ZIP lookup routed through detectLocationCompliance)
    // returns, mirror its mapping into the draft fields so the
    // "Current Educational Standards Mapping" card always matches the scan —
    // it must never show a stale district from a previous save.
    LaunchedEffect(locationComplianceResult) {
        val res = locationComplianceResult ?: return@LaunchedEffect
        configuredCountry = res.matchedEducationalLocale?.countryName ?: res.detectedCountry
        configuredState = if (res.detectedState.isNotBlank()) res.detectedState else res.matchedEducationalLocale?.defaultStateOrProvince ?: configuredState
        configuredDistrict = if (res.detectedDistrict.isNotBlank()) res.detectedDistrict else res.matchedEducationalLocale?.schoolDistricts?.firstOrNull() ?: configuredDistrict
        configuredStandard = res.matchedEducationalLocale?.stateCurriculumStandards?.firstOrNull() ?: configuredStandard
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("back"))
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
                        text = t("100_local_on_device_storage_private_safe"),
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
                        text = t("to_guarantee_absolute_safety_and_privacy_all_pro"),
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
                            t("1_learner_identity_school_level"),
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
                        label = { Text(t("learner_s_first_name_nickname")) },
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
                            label = { Text(t("age_years")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("child_age_input")
                        )

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(t("current_grade"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GradeLevel.entries.forEach { g ->
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
                        t("design_language_interface_scale"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        t("choose_the_layout_tailored_to_your_child_s_age_g"),
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgeGroupTier.entries.forEach { tier ->
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
                                                tf("str_5", tier.ageRange),
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
                                            contentDescription = t("selected"),
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
                                t("2_learning_differences_diagnoses"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("select_all_that_apply_to_customize_ai_scaffoldin"),
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
                                label = { Text(tf("str_3", opt.emoji, opt.title), fontSize = 12.sp) },
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
                                t("3_key_learning_challenges_focus_areas"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("ai_tutors_scaffold_these_exact_areas_with_target"),
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
                                label = { Text(tf("str_3", opt.emoji, opt.title), fontSize = 12.sp) }
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
                                t("4_learner_s_strengths_superpowers"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("lessons_will_leverage_these_natural_gifts_to_tea"),
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
                                label = { Text(tf("str_3", opt.emoji, opt.title), fontSize = 12.sp) }
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
                                t("5_hyper_fixations_favorite_topics"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("the_app_themes_problems_stories_directly_around"),
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
                                label = { Text(tf("str_3", opt.emoji, opt.title), fontSize = 12.sp) }
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
                                t("5b_theme_world_rotation_schedule"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("100_immersive_themes_tailored_to_their_personali"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Active Theme Spotlight Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(currentActiveThemeData.cardHex),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(currentActiveThemeData.primaryHex)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Category Tag & Theme Mode Indicator Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(currentActiveThemeData.primaryHex)
                                ) {
                                    Text(
                                        currentActiveThemeData.category.title,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        t("active_theme"),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E212B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Theme Header: Avatar + Title & Companion Buddy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.9f),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(currentActiveThemeData.emoji, fontSize = 24.sp)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        currentActiveThemeData.title,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E212B),
                                        lineHeight = 20.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        tf("companion_buddy", currentActiveThemeData.buddyName, currentActiveThemeData.buddyRole),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF374151)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Greeting Quote Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💬", fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        tf("str_4", currentActiveThemeData.greeting),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E212B),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Subject Integration Badges
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    t("cross_curricular_integration"),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B5563)
                                )
                                Text(
                                    tf("math_3", currentActiveThemeData.mathIntegration),
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF1E212B),
                                    lineHeight = 16.sp
                                )
                                Text(
                                    tf("reading_2", currentActiveThemeData.readingIntegration),
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF1E212B),
                                    lineHeight = 16.sp
                                )
                                Text(
                                    tf("science_2", currentActiveThemeData.scienceIntegration),
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF1E212B),
                                    lineHeight = 16.sp
                                )
                                Text(
                                    tf("social_studies_2", currentActiveThemeData.socialStudiesIntegration),
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF1E212B),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Theme Rotation Frequency Configuration
                    Text(
                        t("theme_rotation_preference"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        t("keep_permanent_or_periodically_rotate_to_fresh_p"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeRotationSchedule.entries.forEach { schedule ->
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
                        t("ai_recommended_themes_for_this_profile"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        t("synthesized_based_on_diagnoses_strengths_struggl"),
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
                                label = { Text(tf("str_3", theme.emoji, theme.title), fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Actions: Browse Catalog & Preview Palette
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAllThemesDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(t("100_themes"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                previewModalThemeId = activeThemeId
                                showThemePreviewModal = true
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(t("preview_palette_assets"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
                                t("6_standards_jurisdiction_location"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("detects_state_province_school_district_curriculu"),
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
                                        t("location_services_in_use"),
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
                                            t("locale_only"),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    t("used_only_to_identify_educational_jurisdiction_f"),
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
                                t("current_educational_standards_mapping"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                tf("jurisdiction", configuredDistrict, configuredState, configuredCountry),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                tf("standard_framework", configuredStandard),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Google Maps Location Auto-Scan Action
                    Button(
                        onClick = {
                            if (LocationComplianceHelper.hasLocationPermission(context)) {
                                viewModel.detectLocationCompliance(context)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("google_maps_setup_scan_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t("scan_location_with_google_maps"), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    // Scan result card: shows what the scan found (or why GPS
                    // gave no fix), mirroring the parent dashboard.
                    locationComplianceResult?.let { res ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (res.isGoogleMapsVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    if (res.isGoogleMapsVerified) "🗺️ Google Maps Verified: ${res.detectedCity}, ${res.detectedState}, ${res.detectedCountry}" else "📍 Location Detected: ${res.detectedCountry} (${res.detectedState})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (res.isGoogleMapsVerified) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(tf("district_alignment", res.detectedDistrict, res.educationalStandard), fontSize = 11.sp, color = if (res.isGoogleMapsVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(tf("source", res.verificationSource), fontSize = 10.sp, color = if (res.isGoogleMapsVerified) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Manual Postal / Zip Code Override Fallback
                    Text(
                        t("postal_code_zip_fallback"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        t("if_location_services_are_denied_or_unavailable_e"),
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
                            label = { Text(t("zip_postal_code")) },
                            placeholder = { Text(t("e_g_90210_sw1a_1aa_m5v_2t6")) },
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
                            Text(t("apply"))
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
                        t("7_accessibility_sensory_comfort"),
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
                            Text(t("text_to_speech_read_aloud"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(t("read_question_prompts_aloud_automatically_defaul"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text(t("opendyslexic_font_typography"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(t("weighted_bottom_heavy_letters_for_easier_letter"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text(t("100_adaptive_neuro_themes"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        placeholder = { Text(t("search_100_themes_topics_subjects"), fontSize = 12.sp) },
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
                            label = { Text(t("all_100"), fontSize = 11.sp) }
                        )
                        NeuroThemeCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                },
                                label = { Text(tf("str_3", cat.emoji, cat.title), fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        tf("found_themes", filteredThemes.size),
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
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(theme.primaryHex).copy(alpha = 0.85f)
                                        ) {
                                            Text(
                                                theme.category.title,
                                                fontSize = 8.5.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            theme.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            tf("buddy", theme.buddyName, theme.buddyRole),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            tf("math_2", theme.mathIntegration),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            tf("science_2", theme.scienceIntegration),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = t("selected"),
                                            tint = Color(theme.primaryHex),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        IconButton(
                                            onClick = {
                                                previewModalThemeId = theme.id
                                                showThemePreviewModal = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = t("preview_theme"),
                                                tint = Color(theme.primaryHex),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllThemesDialog = false }) {
                    Text(t("close"), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showThemePreviewModal) {
        ThemePreviewModal(
            initialThemeId = previewModalThemeId,
            currentActiveThemeId = activeThemeId,
            initialRotationSchedule = themeRotationSchedule,
            onDismiss = { showThemePreviewModal = false },
            onApplyTheme = { themeId, sched ->
                activeThemeId = themeId
                themeRotationSchedule = sched
                showThemePreviewModal = false
            }
        )
    }
}
