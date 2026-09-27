package com.fourgeailabs.neuropath.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.network.LlamaClient
import com.fourgeailabs.neuropath.network.LlamaDownloadState
import com.fourgeailabs.neuropath.network.LlamaLocalManager
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf
import kotlinx.coroutines.launch

/**
 * Dedicated AI settings area, separated from general app settings.
 *
 * Hosts the Learning Buddy engine controls (mode, local accelerator, GGUF
 * model download) plus the buddy voice picker — everything the parent needs
 * to decide how AI behaves for their child, in one place.
 */
@Composable
fun AiSettingsScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var hfTokenInput by remember { mutableStateOf("") }
    var hfTokenEdited by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.PARENT_DASHBOARD) },
                    modifier = Modifier.testTag("ai_settings_back_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("back"))
                }
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        t("ai_settings"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        t("ai_settings_section_description"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Buddy Voice
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            t("buddy_voice_title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        t("buddy_voice_description"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val voices by viewModel.availableTtsVoices.collectAsState()
                    val currentVoiceName by viewModel.currentTtsVoiceName.collectAsState()
                    var voiceMenuExpanded by remember { mutableStateOf(false) }

                    if (voices.isEmpty()) {
                        Text(
                            t("buddy_voice_none_available"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Box {
                            OutlinedButton(
                                onClick = { voiceMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("tts_voice_picker_btn")
                            ) {
                                Text(
                                    currentVoiceName ?: t("buddy_voice_choose"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            DropdownMenu(
                                expanded = voiceMenuExpanded,
                                onDismissRequest = { voiceMenuExpanded = false }
                            ) {
                                voices.forEach { voice ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                voice.name,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        },
                                        onClick = {
                                            viewModel.setTtsVoice(voice.name)
                                            voiceMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.previewTtsVoice() },
                        modifier = Modifier.fillMaxWidth().testTag("tts_voice_preview_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(t("buddy_voice_preview"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // AI Engine Service Info (Meta Llama 3.2 AI Engine) — moved here from the
        // parent dashboard so AI controls live in their own section.
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = t("ai_settings"), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            t("ai_engine_service_llama_3_2_3b"),
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
                                text = t("hugging_face_llama_3_2_3b_integration_active"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        t("neuropath_is_powered_globally_by_meta_llama_3_2"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Disable Learning Buddy toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t("disable_learning_buddy_collectively"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(t("completely_hides_learning_buddy_from_the_child_s"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = profile.learningBuddyDisabled,
                            onCheckedChange = { disabled ->
                                viewModel.updateParentAiConfig(disabled, profile.aiVersionMode, profile.localLlamaInstalled)
                            },
                            modifier = Modifier.testTag("disable_learning_buddy_switch")
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(t("child_ai_version_mode"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "SOCRATIC_ONLY" to "Socratic Only",
                            "FULL_AI" to "Llama 3.2 (Cloud)",
                            "LOCAL_OFFLINE" to "Llama 3.2 (Local)"
                        ).forEach { (mode, label) ->
                            val isSelected = profile.aiVersionMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateParentAiConfig(profile.learningBuddyDisabled, mode, profile.localLlamaInstalled)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Local Offline Llama 3.2 3B Hardware & GPU Acceleration
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t("llama_3_2_3b_hardware_gpu_acceleration"), fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(
                                if (profile.localLlamaInstalled) "Status: Llama 3.2 3B loaded on-device with LiteRT-LM Vulkan/OpenCL hardware acceleration active." else "Status: Enable LiteRT-LM Llama 3.2 3B on-device hardware acceleration without cloud dependency.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    viewModel.updateParentAiConfig(profile.learningBuddyDisabled, "LOCAL_OFFLINE", !profile.localLlamaInstalled)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("install_local_llama_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(if (profile.localLlamaInstalled) "Disable Local Llama 3.2 Accelerator" else "Enable Llama 3.2 Hardware Accelerator", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Llama 3.2 3B Local GGUF Engine from Hugging Face
                    val llamaState by LlamaLocalManager.downloadState.collectAsState()
                    val llamaComp = remember(context) { LlamaLocalManager.checkDeviceCompatibility(context) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t("llama_3_2_3b_local_engine_hugging_face_meta_llam"), fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.tertiary)
                            }

                            Text(
                                tf("source", LlamaLocalManager.HUGGINGFACE_REPO_URL),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Text(
                                llamaComp.compatibilitySummary,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            when (val state = llamaState) {
                                is LlamaDownloadState.NotInstalled, is LlamaDownloadState.Error -> {
                                    if (state is LlamaDownloadState.Error) {
                                        Text(
                                            tf("str_11", state.message),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Text(t("official_repository_meta_llama_llama_3_2_3b_inst"), fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    OutlinedTextField(
                                        value = hfTokenInput,
                                        onValueChange = { hfTokenInput = it; hfTokenEdited = true },
                                        label = { Text(t("hugging_face_access_token_hf_optional")) },
                                        placeholder = { Text(t("stored_securely_on_this_device_never_displayed")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("hf_token_input")
                                    )

                                    Button(
                                        onClick = {
                                            if (hfTokenInput.isNotBlank()) {
                                                viewModel.updateAiSetup(profile.customAiPlatform, hfTokenInput.trim())
                                            }
                                            coroutineScope.launch {
                                                LlamaLocalManager.startLlamaDownload(context, hfTokenInput)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("download_llama_3b_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Text(t("download_llama_3_2_3b_weights_from_hugging_face"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                is LlamaDownloadState.Downloading -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(t("downloading_llama_3_2_3b_from_hugging_face"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${(state.progress * 100).toInt()}% (${state.downloadSpeedKbps} KB/s)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                        }
                                        LinearProgressIndicator(
                                            progress = { state.progress },
                                            modifier = Modifier.fillMaxWidth().height(8.dp),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                is LlamaDownloadState.Installed -> {
                                    Text(
                                        "Status: Hugging Face Llama 3.2 3B model package (${"%.2f".format(state.fileSizeBytes / (1024f * 1024f * 1024f))} GB) installed locally on device storage at ${state.localPath}. Active for on-device local inference!",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                viewModel.updateParentAiConfig(profile.learningBuddyDisabled, "LLAMA_LOCAL", true)
                                            },
                                            modifier = Modifier.weight(1f).testTag("activate_llama_local_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                        ) {
                                            Text(t("set_active_engine"), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                LlamaLocalManager.deleteLlamaModel(context)
                                            },
                                            modifier = Modifier.weight(1f).testTag("delete_llama_model_btn"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(t("delete_model"), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Manual warm-up: lets the parent pre-load the model so the
                    // child's first chat message is instant.
                    val isModelLoading by viewModel.isModelLoading.collectAsState()
                    val loadProgress by viewModel.modelLoadProgress.collectAsState()
                    val loadStage by viewModel.modelLoadStage.collectAsState()
                    OutlinedButton(
                        onClick = { viewModel.ensureLocalModelReady() },
                        enabled = !isModelLoading,
                        modifier = Modifier.fillMaxWidth().testTag("warm_up_model_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (isModelLoading) "${loadStage.ifBlank { "Warming up…" }} ${(loadProgress * 100).toInt()}%"
                            else t("warm_up_model_now"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
