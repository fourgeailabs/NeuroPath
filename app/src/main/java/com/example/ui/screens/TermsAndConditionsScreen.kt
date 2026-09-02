package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguageDictionary
import com.example.data.model.EducationalLocaleManager
import com.example.data.model.LocaleLegalComplianceManager
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.ui.components.AppLogoIcon

@Composable
fun TermsAndConditionsScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.currentProfile.collectAsState()
    val isVerifyingLocation by viewModel.isVerifyingLocation.collectAsState()
    val locationComplianceResult by viewModel.locationComplianceResult.collectAsState()
    val langCode = profile.appLanguageCode

    // Auto-detect location compliance on initial screen launch
    LaunchedEffect(Unit) {
        if (locationComplianceResult == null) {
            viewModel.detectLocationCompliance(context)
        }
    }

    val legalNotice = remember(profile.country) {
        LocaleLegalComplianceManager.getComplianceNotice(profile.country)
    }

    val scrollState = rememberScrollState()
    var hasScrolledToBottom by remember { mutableStateOf(false) }
    var userAcknowledgedCheck by remember { mutableStateOf(false) }

    // Check if user has scrolled near bottom
    val isNearBottom by remember {
        derivedStateOf {
            scrollState.maxValue == 0 || scrollState.value >= (scrollState.maxValue - 80)
        }
    }

    LaunchedEffect(isNearBottom) {
        if (isNearBottom) {
            hasScrolledToBottom = true
        }
    }

    val canAccept = hasScrolledToBottom && userAcknowledgedCheck

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            AppLogoIcon(size = 54.dp, showText = false)

            Spacer(Modifier.height(6.dp))

            Text(
                text = AppLanguageDictionary.getString("terms_title", langCode),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dynamic Locale Jurisdiction Tag
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Jurisdiction",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${AppLanguageDictionary.getString("compliance_verified", langCode)}: ${legalNotice.countryName} • ${legalNotice.governingLaw}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Scrollable Terms Container
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // AI Accuracy Warning
                    TermsDisclaimerBanner(
                        title = "⚠️ AI Accuracy & Fallibility Warning",
                        text = legalNotice.aiMistakesWarning,
                        bgColor = Color(0xFFFFF3CD),
                        textColor = Color(0xFF856404),
                        icon = Icons.Default.WarningAmber
                    )

                    // Internet & Cloud AI Connection Notice
                    TermsDisclaimerBanner(
                        title = "🌐 Internet & Cloud Connectivity Notice",
                        text = legalNotice.internetAccessNotice,
                        bgColor = Color(0xFFE3F2FD),
                        textColor = Color(0xFF0D47A1),
                        icon = Icons.Default.Wifi
                    )

                    // Strict Location & Curriculum Boundary Notice
                    TermsDisclaimerBanner(
                        title = "📍 Location & Localized Curriculum Assurance",
                        text = legalNotice.locationCurriculumNotice,
                        bgColor = Color(0xFFE8F5E9),
                        textColor = Color(0xFF1B5E20),
                        icon = Icons.Default.Security
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Localized Terms Sections
                    legalNotice.termsSections.forEach { section ->
                        TermsSection(
                            title = section.sectionTitle,
                            body = section.content
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (hasScrolledToBottom) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (hasScrolledToBottom) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (hasScrolledToBottom) "You have reviewed all terms and disclaimers for ${legalNotice.countryName}." else "Please scroll all the way down to unlock acceptance.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Checkbox for Acceptance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = userAcknowledgedCheck,
                    onCheckedChange = { userAcknowledgedCheck = it },
                    enabled = hasScrolledToBottom,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("tc_agree_checkbox")
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = AppLanguageDictionary.getString("terms_agree_checkbox", langCode),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasScrolledToBottom) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    if (canAccept) {
                        viewModel.acceptTermsAndConditions()
                        viewModel.navigateTo(AppScreen.PARENT_PIN_SETUP)
                    }
                },
                enabled = canAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("tc_continue_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (!hasScrolledToBottom) AppLanguageDictionary.getString("terms_scroll_prompt", langCode) else if (!userAcknowledgedCheck) AppLanguageDictionary.getString("terms_check_agree_prompt", langCode) else AppLanguageDictionary.getString("continue_btn", langCode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TermsDisclaimerBanner(
    title: String,
    text: String,
    bgColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(22.dp).padding(top = 2.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = textColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = text,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun TermsSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            fontSize = 11.5.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
