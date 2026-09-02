package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguageDictionary
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.ui.components.AppLogoIcon

@Composable
fun ParentPinSetupScreen(viewModel: NeuroPathViewModel, modifier: Modifier = Modifier) {
    val profile by viewModel.currentProfile.collectAsState()
    val langCode = profile.appLanguageCode

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            Spacer(Modifier.height(36.dp))

            AppLogoIcon(size = 90.dp, showText = false)

            Spacer(Modifier.height(16.dp))

            Text(
                text = AppLanguageDictionary.getString("set_parental_pin", langCode),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = AppLanguageDictionary.getString("pin_subtitle", langCode),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "🔒 ${AppLanguageDictionary.getString("pin_security_note", langCode)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        pin = it
                        errorMessage = null
                    }
                },
                label = { Text(AppLanguageDictionary.getString("enter_pin", langCode)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPin = !showPin }) {
                        Icon(
                            imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Passcode"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_setup_input")
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        confirmPin = it
                        errorMessage = null
                    }
                },
                label = { Text(AppLanguageDictionary.getString("confirm_pin", langCode)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_confirm_input")
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    when {
                        pin.length < 4 -> {
                            errorMessage = "Passcode must be at least 4 digits long."
                        }
                        pin != confirmPin -> {
                            errorMessage = "Passcodes do not match. Please re-enter."
                        }
                        else -> {
                            viewModel.updateParentPin(pin)
                            viewModel.navigateTo(AppScreen.CHILD_PROFILE_SETUP)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("pin_continue_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = AppLanguageDictionary.getString("continue_btn", langCode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
