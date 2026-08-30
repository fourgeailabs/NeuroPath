package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppLanguageDictionary
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

import com.example.ui.components.AppLogoIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(viewModel: NeuroPathViewModel, modifier: Modifier = Modifier) {
    val profile by viewModel.currentProfile.collectAsState()
    var selectedLanguageCode by remember { mutableStateOf(profile.appLanguageCode) }
    var expanded by remember { mutableStateOf(false) }

    val languages = AppLanguage.values().toList()
    val activeLang = AppLanguage.fromCode(selectedLanguageCode)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            AppLogoIcon(size = 110.dp, showText = false)

            Spacer(Modifier.height(16.dp))

            Text(
                text = AppLanguageDictionary.getString("welcome", selectedLanguageCode),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = AppLanguageDictionary.getString("select_language", selectedLanguageCode),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "${activeLang.flagEmoji} ${activeLang.displayName} (${activeLang.nativeName})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(AppLanguageDictionary.getString("language_label", selectedLanguageCode)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text("${lang.flagEmoji} ${lang.displayName} - ${lang.nativeName}") },
                            onClick = {
                                selectedLanguageCode = lang.code
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.updateProfileSettingsWithLocale(
                        name = profile.name,
                        gradeLevel = profile.gradeLevel,
                        stateStandard = profile.stateStandard,
                        country = profile.country,
                        stateOrProvince = profile.stateOrProvince,
                        city = profile.city,
                        schoolDistrict = profile.schoolDistrict,
                        appLanguageCode = selectedLanguageCode,
                        themeId = profile.activeThemeId,
                        neuroTypes = profile.neurodivergentTypesCsv,
                        dyslexiaFont = profile.dyslexiaFontEnabled,
                        contrastMode = profile.highContrastMode,
                        ttsSpeed = profile.ttsSpeed,
                        readAloud = profile.readAnswersAloud,
                        dailyMinutes = profile.dailyGoalMinutes
                    )
                    viewModel.navigateTo(AppScreen.TERMS_AND_CONDITIONS)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("continue_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = AppLanguageDictionary.getString("continue_btn", selectedLanguageCode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
