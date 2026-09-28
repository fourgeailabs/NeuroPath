package com.fourgeailabs.neuropath.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.fourgeailabs.neuropath.data.curriculum.oer.OerMediaResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.neuropath.data.curriculum.oer.OerCommonsCurriculumItem
import com.fourgeailabs.neuropath.data.curriculum.oer.OerGradeBand
import com.fourgeailabs.neuropath.data.curriculum.oer.PreinstalledOerCurriculumCatalog
import com.fourgeailabs.neuropath.data.local.ChatSessionSummary
import com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity
import com.fourgeailabs.neuropath.data.model.EducationalExplanationMode
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.EducationalSubjectTag
import com.fourgeailabs.neuropath.network.ChatModelMode
import com.fourgeailabs.neuropath.ui.ChatMessage
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf
import com.fourgeailabs.neuropath.ui.theme.ColorTokens
import com.fourgeailabs.neuropath.ui.theme.ElevationTokens
import com.fourgeailabs.neuropath.ui.theme.ShapeTokens
import com.fourgeailabs.neuropath.ui.theme.SpacingTokens
import androidx.compose.foundation.layout.sizeIn
import com.fourgeailabs.neuropath.ui.components.BuddyAvatar
import com.fourgeailabs.neuropath.ui.components.BuddyPose

/**
 * Rebuilt Educational Chat Interface component.
 * Features:
 * - Direct integration with Llama 3.2 3B AI Engine (Hugging Face Inference & Local On-Device GGUF).
 * - Personalized explanations with customizable modes: Step-by-Step (Socratic), Simpler Analogy (ELI5), Visual Breakdown, Deep Concept, & Direct Answer.
 * - Comprehensive Message History with Room database persistence, topic session switching, keyword search, and bookmarks.
 * - Single-tap explanation transformations: "Explain Simpler", "Step-by-Step Breakdown", TTS read aloud, and copy to clipboard.
 * - Dynamic curriculum-aligned educational subject filters & prompt suggestions.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EducationalChatInterface(
    viewModel: NeuroPathViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme = viewModel.getActiveTheme()
    val profile by viewModel.currentProfile.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isChatGenerating.collectAsState()
    val chatSendError by viewModel.chatSendError.collectAsState()
    val activeChatMode by viewModel.chatModelMode.collectAsState()
    val chatEngineLabel by viewModel.chatEngineLabel.collectAsState()

    // Refresh the honest engine label whenever the chat opens (model may have
    // been downloaded or deleted since the label was last computed).
    LaunchedEffect(Unit) { viewModel.refreshChatEngineLabel() }
    val activeExplanationMode by viewModel.explanationMode.collectAsState()
    val activeSubjectTag by viewModel.selectedSubjectTag.collectAsState()
    val currentSessionTitle by viewModel.currentSessionTitle.collectAsState()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsState()
    val isTranscribing by viewModel.isTranscribingAudio.collectAsState()
    val isVoiceMode by viewModel.isVoiceConversationMode.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showExplanationMenu by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showOerCollectionsSheet by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }
    var newSessionTitleInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size, isGenerating) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ==========================================
        // 1. TOP HEADER BAR
        // ==========================================
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = ElevationTokens.Card,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button & Buddy Avatar Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = t("back"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.width(SpacingTokens.xxs))

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                BuddyAvatar(
                                    themeId = theme.id,
                                    pose = BuddyPose.IDLE,
                                    size = SpacingTokens.huge,
                                    contentDescription = theme.buddyName
                                )
                            }
                        }

                        Spacer(Modifier.width(SpacingTokens.s))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    theme.buddyName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(SpacingTokens.xxs))
                                // Free Model Badge Indicator
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (activeChatMode.isFreeTier) ColorTokens.Light.success.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (activeChatMode.isFreeTier) "FREE" else "PRO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeChatMode.isFreeTier) ColorTokens.Light.success else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = SpacingTokens.xxs, vertical = 1.dp),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Text(
                                text = currentSessionTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(SpacingTokens.xs))

                    // Action Controls: Model Selector, New Topic, and Message History
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Free Model / Model Mode Dropdown Pill
                        Box {
                            // Honest engine label: cloud Llama, on-device Llama, or the
                            // offline Socratic fallback — whichever will actually answer.
                            val modelLabel = chatEngineLabel

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                    .clickable { showModelMenu = true }
                                    .testTag("chat_model_selector_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activeChatMode.icon, fontSize = 12.sp)
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        modelLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false }
                            ) {
                                Text(
                                    t("select_llama_model"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = SpacingTokens.l, vertical = SpacingTokens.xs)
                                )
                                // Exactly three AI options are offered: Cloud AI, Local AI,
                                // and the Socratic Teacher. The Socratic Teacher is the
                                // honest no-AI tool and is never listed as a Llama model.
                                // It is hidden when the parent has turned it off.
                                val chatModeOptions = buildList {
                                    add(ChatModelMode.GENERAL)
                                    add(ChatModelMode.LLAMA_LOCAL)
                                    if (profile.socraticTeacherEnabled) add(ChatModelMode.OFFLINE)
                                }
                                chatModeOptions.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(tf("str_3", mode.icon, t(mode.labelKey)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    if (mode.isFreeTier) {
                                                        Spacer(Modifier.width(SpacingTokens.xs))
                                                        Surface(
                                                            shape = ShapeTokens.ExtraSmall,
                                                            color = ColorTokens.Light.success.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(t("free_model"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ColorTokens.Light.success, modifier = Modifier.padding(horizontal = SpacingTokens.xxs, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text(t(mode.descriptionKey), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setChatModelMode(mode)
                                            showModelMenu = false
                                        },
                                        trailingIcon = {
                                            if (mode == activeChatMode) {
                                                Icon(Icons.Default.Check, contentDescription = t("active_2"), tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(SpacingTokens.xxs))

                        // New Session Button
                        IconButton(
                            onClick = {
                                newSessionTitleInput = ""
                                showNewSessionDialog = true
                            },
                            modifier = Modifier
                                .size(SpacingTokens.giant)
                                .testTag("chat_new_topic_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = t("start_new_topic"),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Message History Sheet Opener
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("chat_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = t("message_history"),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(SpacingTokens.xxl)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(SpacingTokens.xs))

                // ==========================================
                // 2. EXPLANATION MODE & SUBJECT SELECTOR BAR
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                ) {
                    // Active Explanation Mode Pill
                    Box {
                        Surface(
                            shape = ShapeTokens.Medium,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                .clickable { showExplanationMenu = true }
                                .testTag("chat_explanation_mode_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(activeExplanationMode.icon, fontSize = 12.sp)
                                Spacer(Modifier.width(SpacingTokens.xxs))
                                Text(
                                    tf("mode", activeExplanationMode.title),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showExplanationMenu,
                            onDismissRequest = { showExplanationMenu = false }
                        ) {
                            Text(
                                t("personalized_explanation_style"),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = SpacingTokens.l, vertical = SpacingTokens.xs)
                            )
                            EducationalExplanationMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(tf("str_3", mode.icon, mode.title), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(Modifier.width(SpacingTokens.xs))
                                                Surface(
                                                    shape = ShapeTokens.ExtraSmall,
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(mode.shortBadge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = SpacingTokens.xxs, vertical = 1.dp))
                                                }
                                            }
                                            Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setExplanationMode(mode)
                                        showExplanationMenu = false
                                    },
                                    trailingIcon = {
                                        if (mode == activeExplanationMode) {
                                            Icon(Icons.Default.Check, contentDescription = t("active_2"), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Subject Selector Chips
                    EducationalSubjectTag.entries.forEach { subject ->
                        val isSelected = activeSubjectTag == subject
                        Surface(
                            shape = ShapeTokens.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                .clickable { viewModel.setSelectedSubjectTag(subject) }
                                .testTag("subject_chip_${subject.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(subject.icon, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    subject.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. FREE MODEL STATUS & STARTER PROMPTS
        // ==========================================
        AnimatedVisibility(
            visible = chatMessages.size <= 1 && !isGenerating,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.l, vertical = SpacingTokens.xs)
            ) {
                // Free Model Banner
                Surface(
                    shape = ShapeTokens.Medium,
                    color = if (activeChatMode.isFreeTier) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (activeChatMode.isFreeTier) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (activeChatMode.isFreeTier) "✨" else "🧠", fontSize = 14.sp)
                        Spacer(Modifier.width(SpacingTokens.xs))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeChatMode.isFreeTier) "Llama 3.2 3B Active (${activeChatMode.modelName})" else "Pro Model Active (${activeChatMode.modelName})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (activeChatMode.isFreeTier) ColorTokens.Light.success else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = tf("personalized_explanations_tailored_to", profile.gradeLevel, profile.schoolDistrict),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (activeChatMode.isFreeTier) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(SpacingTokens.xs))

                // Subject Starter Prompts
                Text(
                    t("suggested_educational_topics"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = SpacingTokens.xxxs, bottom = SpacingTokens.xxs)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    contentPadding = PaddingValues(horizontal = SpacingTokens.xxxs)
                ) {
                    items(activeSubjectTag.samplePrompts) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = ElevationTokens.Level1,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable(enabled = !isGenerating) {
                                viewModel.sendChatMessage(prompt)
                            }
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xs)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. MAIN MESSAGE STREAM
        // ==========================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SpacingTokens.l),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.m),
                contentPadding = PaddingValues(top = SpacingTokens.s, bottom = SpacingTokens.l)
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    EducationalMessageBubble(
                        message = message,
                        buddyThemeId = theme.id,
                        buddyName = theme.buddyName,
                        dyslexiaFont = profile.dyslexiaFontEnabled,
                        onSpeak = { text -> viewModel.speechManager.speak(text) },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(t("explanation"), text))
                            Toast.makeText(context, t("copied_to_clipboard"), Toast.LENGTH_SHORT).show()
                        },
                        onExplainSimpler = { baseText ->
                            viewModel.requestSimplerExplanation(baseText)
                        },
                        onStepByStep = { baseText ->
                            viewModel.requestStepByStepExplanation(baseText)
                        },
                        onToggleBookmark = { msg ->
                            viewModel.toggleMessageBookmark(msg)
                        },
                        onSelectFollowUp = { followUp ->
                            viewModel.sendChatMessage(followUp)
                        }
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SpacingTokens.s),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(SpacingTokens.huge)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = SpacingTokens.xxxs,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(SpacingTokens.m))
                            Surface(
                                shape = ShapeTokens.Large,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = SpacingTokens.l, vertical = SpacingTokens.s),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        tf("is_thinking_gently_with", theme.buddyName, t(activeChatMode.labelKey)),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4b. SEND-ERROR BANNER (honest failure, never a fake buddy message)
        // ==========================================
        chatSendError?.let { errorText ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = ShapeTokens.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xxs)
                    .testTag("chat_send_error_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SpacingTokens.l, vertical = SpacingTokens.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.retryFailedChatMessage() },
                        modifier = Modifier.testTag("chat_send_error_retry_btn")
                    ) {
                        Text(
                            text = t("retry"),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    TextButton(onClick = { viewModel.dismissChatSendError() }) {
                        Text(
                            text = t("dismiss"),
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. INPUT CONTROLS & VOICE TRANSCRIPTION
        // ==========================================
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = SpacingTokens.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                when {
                                    isRecordingAudio -> "Recording... tap stop to send"
                                    isTranscribing -> "Transcribing with AI..."
                                    else -> "Ask ${theme.buddyName} anything..."
                                },
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(22.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    viewModel.sendChatMessage(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        singleLine = false,
                        minLines = 1,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(Modifier.width(SpacingTokens.xs))

                    // Microphone Transcribe Button
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isRecordingAudio -> Color(0xFFE53935)
                            isTranscribing -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        modifier = Modifier
                            .size(SpacingTokens.giant)
                            .clickable {
                                if (isRecordingAudio) {
                                    viewModel.stopAudioRecordingAndTranscribe { transcribedText ->
                                        inputText = if (inputText.isBlank()) transcribedText else "$inputText $transcribedText"
                                    }
                                } else if (!isTranscribing) {
                                    viewModel.startAudioRecording()
                                }
                            }
                            .testTag("chat_transcribe_mic_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isTranscribing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = SpacingTokens.xxxs,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = t("transcribe_audio"),
                                    tint = if (isRecordingAudio) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(SpacingTokens.xxl)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(SpacingTokens.xs))

                    // Send Button
                    Surface(
                        shape = CircleShape,
                        color = if (inputText.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(SpacingTokens.giant)
                            .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                                viewModel.sendChatMessage(inputText)
                                inputText = ""
                            }
                            .testTag("chat_send_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = t("send"),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 6. MESSAGE HISTORY BOTTOM SHEET
    // ==========================================
    if (showHistorySheet) {
        EducationalChatHistorySheet(
            viewModel = viewModel,
            onDismiss = { showHistorySheet = false },
            onSelectSession = { sessionId, sessionTitle ->
                viewModel.loadChatSession(sessionId, sessionTitle)
                showHistorySheet = false
            }
        )
    }

    // ==========================================
    // 6b. OER COMMONS CURATED COLLECTIONS SHEET
    // ==========================================
    if (showOerCollectionsSheet) {
        OerCuratedCollectionsBrowserSheet(
            onDismiss = { showOerCollectionsSheet = false },
            onSelectUnit = { unitPrompt ->
                showOerCollectionsSheet = false
                viewModel.sendChatMessage(unitPrompt)
            }
        )
    }

    // ==========================================
    // 7. NEW TOPIC DIALOG
    // ==========================================
    if (showNewSessionDialog) {
        AlertDialog(
            onDismissRequest = { showNewSessionDialog = false },
            title = { Text(t("start_new_educational_topic"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        t("give_this_study_topic_a_title_e_g_fractions_prac"),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(SpacingTokens.m))
                    OutlinedTextField(
                        value = newSessionTitleInput,
                        onValueChange = { newSessionTitleInput = it },
                        placeholder = { Text(t("e_g_5th_grade_fractions")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = newSessionTitleInput.ifBlank { "Study Session" }
                        viewModel.startNewChatSession(title)
                        showNewSessionDialog = false
                    }
                ) {
                    Text(t("start_topic"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSessionDialog = false }) {
                    Text(t("cancel"))
                }
            }
        )
    }
}

fun buildMarkdownAnnotatedString(
    text: String,
    baseColor: Color,
    boldColor: Color = baseColor
): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) {
                append("\n")
            }

            var processedLine = line
            var isHeading = false

            if (processedLine.startsWith("### ")) {
                processedLine = processedLine.substring(4)
                isHeading = true
            } else if (processedLine.startsWith("## ")) {
                processedLine = processedLine.substring(3)
                isHeading = true
            } else if (processedLine.startsWith("# ")) {
                processedLine = processedLine.substring(2)
                isHeading = true
            }

            val lineStart = length

            var i = 0
            while (i < processedLine.length) {
                if (i + 1 < processedLine.length && processedLine[i] == '*' && processedLine[i + 1] == '*') {
                    val closingIndex = processedLine.indexOf("**", i + 2)
                    if (closingIndex != -1) {
                        val content = processedLine.substring(i + 2, closingIndex)
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor))
                        append(content)
                        pop()
                        i = closingIndex + 2
                        continue
                    }
                } else if (processedLine[i] == '*' || processedLine[i] == '_') {
                    val marker = processedLine[i]
                    val closingIndex = processedLine.indexOf(marker, i + 1)
                    if (closingIndex != -1 && closingIndex > i + 1) {
                        val content = processedLine.substring(i + 1, closingIndex)
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(content)
                        pop()
                        i = closingIndex + 1
                        continue
                    }
                } else if (processedLine[i] == '`') {
                    val closingIndex = processedLine.indexOf('`', i + 1)
                    if (closingIndex != -1) {
                        val content = processedLine.substring(i + 1, closingIndex)
                        pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = baseColor.copy(alpha = 0.1f)))
                        append(content)
                        pop()
                        i = closingIndex + 1
                        continue
                    }
                }
                append(processedLine[i])
                i++
            }

            if (isHeading) {
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = boldColor
                    ),
                    lineStart,
                    length
                )
            }
        }
    }
}

/**
 * Individual message bubble with rich educational actions, dyslexic spacing,
 * speaker TTS, copy, explanation transformer, and follow-up chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EducationalMessageBubble(
    message: ChatMessage,
    buddyThemeId: String,
    buddyName: String,
    dyslexiaFont: Boolean,
    onSpeak: (String) -> Unit,
    onCopy: (String) -> Unit,
    onExplainSimpler: (String) -> Unit,
    onStepByStep: (String) -> Unit,
    onToggleBookmark: (ChatMessage) -> Unit,
    onSelectFollowUp: (String) -> Unit
) {
    val isUser = message.sender == "USER"

    val baseTextColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val boldTextColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    val annotatedContent = remember(message.text, isUser, baseTextColor, boldTextColor) {
        buildMarkdownAnnotatedString(
            text = message.text,
            baseColor = baseTextColor,
            boldColor = boldTextColor
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(34.dp)
                        .padding(top = SpacingTokens.xxxs)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        BuddyAvatar(
                            themeId = buddyThemeId,
                            pose = BuddyPose.IDLE,
                            size = 30.dp,
                            contentDescription = buddyName
                        )
                    }
                }
                Spacer(Modifier.width(SpacingTokens.s))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = SpacingTokens.xl,
                    topEnd = SpacingTokens.xl,
                    bottomStart = if (isUser) SpacingTokens.xl else SpacingTokens.xxs,
                    bottomEnd = if (isUser) SpacingTokens.xxs else SpacingTokens.xl
                ),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shadowElevation = 0.5.dp,
                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) else null,
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.l)) {
                    // AI message header tags
                    if (!isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (message.isFreeModel) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (message.isFreeModel) "⚡ Free Model" else "🧠 Pro Model",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (message.isFreeModel) ColorTokens.Light.success else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxxs)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = message.explanationMode.shortBadge,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxxs)
                                    )
                                }

                                // Honest provenance: Socratic Teacher answers are
                                // the offline engine, never the Llama model.
                                if (message.modelMode == ChatModelMode.OFFLINE) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFF3E0)
                                    ) {
                                        Text(
                                            text = tf("str_3", "🧑‍🏫", t("ai_option_socratic")),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxxs)
                                        )
                                    }
                                }
                            }

                            // Bookmark icon
                            IconButton(
                                onClick = { onToggleBookmark(message) },
                                modifier = Modifier.size(SpacingTokens.xxxl)
                            ) {
                                Icon(
                                    imageVector = if (message.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = t("save_explanation"),
                                    tint = if (message.isBookmarked) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(SpacingTokens.xl)
                                )
                            }
                        }
                        Spacer(Modifier.height(SpacingTokens.s))
                    }

                    // Main Text Content
                    Text(
                        text = annotatedContent,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = if (dyslexiaFont) 1.0.sp else 0.2.sp,
                            lineHeight = if (dyslexiaFont) 22.sp else 20.sp,
                            fontSize = 14.sp
                        ),
                        color = baseTextColor
                    )

                    // AI Message Action Bar (TTS, Copy, Simplify, Step-by-Step)
                    if (!isUser) {
                        Spacer(Modifier.height(SpacingTokens.m))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(SpacingTokens.xs))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Transformation Quick Actions
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                            ) {
                                Surface(
                                    shape = ShapeTokens.Medium,
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable { onExplainSimpler(message.text) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(t("simpler"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                }

                                Surface(
                                    shape = ShapeTokens.Medium,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable { onStepByStep(message.text) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(t("steps"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }

                            // Audio & Copy Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onCopy(message.text) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = t("copy_text"),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onSpeak(message.text) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = t("read_aloud_3"),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(SpacingTokens.xl)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Suggested Follow-up Question Chips
        if (!isUser && message.suggestedFollowUps.isNotEmpty()) {
            Spacer(Modifier.height(SpacingTokens.xs))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)
            ) {
                message.suggestedFollowUps.forEach { followUp ->
                    Surface(
                        shape = ShapeTokens.Medium,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable { onSelectFollowUp(followUp) }
                    ) {
                        Text(
                            text = tf("str_13", followUp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Message History Bottom Sheet allowing learners and parents to view past educational sessions,
 * search messages by keyword, review saved bookmarks, and switch topics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EducationalChatHistorySheet(
    viewModel: NeuroPathViewModel,
    onDismiss: () -> Unit,
    onSelectSession: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(0) } // 0: Sessions, 1: Bookmarks, 2: Search
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val sessionSummaries by viewModel.chatSessionSummaries.collectAsState()
    val bookmarkedMessages by viewModel.bookmarkedChatMessages.collectAsState()
    val searchResults by viewModel.searchResultsChatMessages.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = SpacingTokens.xl)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = t("history"),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(SpacingTokens.s))
                    Text(
                        t("educational_chat_history"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = t("clear_all_history"),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(SpacingTokens.s))

            // Tab Navigation (Topics, Bookmarks, Search)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(tf("topics", sessionSummaries.size), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(tf("saved", bookmarkedMessages.size), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(t("search_2"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(Modifier.height(SpacingTokens.m))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Sessions List
                    if (sessionSummaries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                t("no_past_topic_sessions_found_start_chatting_to_b"),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(SpacingTokens.xxxl)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.s)
                        ) {
                            items(sessionSummaries) { session ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = ShapeTokens.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                        .clickable { onSelectSession(session.sessionId, session.sessionTitle) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(SpacingTokens.l),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                session.sessionTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(SpacingTokens.xxs))
                                            Text(
                                                "${session.messageCount} messages • ${formatTimestamp(session.lastTimestamp)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteChatSession(session.sessionId) },
                                            modifier = Modifier.size(SpacingTokens.giant)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = t("delete_session"),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(SpacingTokens.xl)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Bookmarked Explanations
                    if (bookmarkedMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                t("no_saved_explanations_yet_tap_the_bookmark_icon"),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(SpacingTokens.xxxl)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.s)
                        ) {
                            items(bookmarkedMessages) { msg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = ShapeTokens.Medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(SpacingTokens.l)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                tf("str_2", msg.sessionTitle),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                formatTimestamp(msg.timestamp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(SpacingTokens.xs))
                                        Text(
                                            msg.text,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Search in Chat History
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchChatMessages(it)
                            },
                            placeholder = { Text(t("search_past_explanations_e_g_fractions_solar")) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = t("search"), tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = ShapeTokens.Large
                        )

                        Spacer(Modifier.height(SpacingTokens.m))

                        if (searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (searchQuery.isBlank()) "Type a keyword above to search through all past chats" else "No matching explanations found for '$searchQuery'",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(SpacingTokens.s)
                            ) {
                                items(searchResults) { result ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = ShapeTokens.Medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                            .clickable { onSelectSession(result.sessionId, result.sessionTitle) }
                                    ) {
                                        Column(modifier = Modifier.padding(SpacingTokens.l)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    tf("in", result.sender, result.sessionTitle),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    formatTimestamp(result.timestamp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(Modifier.height(SpacingTokens.xxs))
                                            Text(
                                                result.text,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
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
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(t("clear_all_message_history")) },
            text = { Text(t("this_will_remove_all_saved_chat_messages_and_top")) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChatHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(t("clear_all"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(t("cancel"))
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Interactive OER Commons Curated Collections Browser Sheet.
 * Displays curated units from https://oercommons.org/curated-collections across all K-12 subjects,
 * allowing learners/parents to explore standards, launch Socratic practice problems, and ask Llama.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OerCuratedCollectionsBrowserSheet(
    onDismiss: () -> Unit,
    onSelectUnit: (String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSubjectFilter by remember { mutableStateOf<EducationalSubject?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeMediaResource by remember { mutableStateOf<OerMediaResource?>(null) }

    val allUnits = remember { PreinstalledOerCurriculumCatalog.getAllPreinstalledCurriculum() }
    val filteredUnits = remember(selectedSubjectFilter, searchQuery) {
        allUnits.filter { unit ->
            val matchesSubject = selectedSubjectFilter == null || unit.subject == selectedSubjectFilter
            val matchesSearch = searchQuery.isBlank() ||
                    unit.unitTitle.contains(searchQuery, ignoreCase = true) ||
                    unit.collectionTitle.contains(searchQuery, ignoreCase = true) ||
                    unit.standardCode.contains(searchQuery, ignoreCase = true) ||
                    unit.keyConcepts.any { it.contains(searchQuery, ignoreCase = true) }
            matchesSubject && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = SpacingTokens.xl)
        ) {
            // Header with OER link
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BuddyAvatar(themeId = "space", pose = BuddyPose.IDLE, size = 28.dp)
                        Spacer(Modifier.width(SpacingTokens.xs))
                        Text(
                            t("oer_commons_curated_collections"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        "https://oercommons.org/curated-collections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://oercommons.org/curated-collections"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, t("opening_browser"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                Surface(
                    shape = ShapeTokens.Small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://oercommons.org/curated-collections"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, tf("visit_url", "https://oercommons.org/curated-collections"), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        t("visit_site"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = SpacingTokens.s, vertical = SpacingTokens.xxs)
                    )
                }
            }

            Spacer(Modifier.height(SpacingTokens.m))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(t("search_curated_units_standards_e_g_ccss_ngss"), fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = t("search"), modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = t("clear"), modifier = Modifier.size(SpacingTokens.xl))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("oer_search_input"),
                shape = ShapeTokens.Medium
            )

            Spacer(Modifier.height(SpacingTokens.s))

            // Subject Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedSubjectFilter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable { selectedSubjectFilter = null }
                ) {
                    Text(
                        tf("all_subjects", allUnits.size),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (selectedSubjectFilter == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubjectFilter == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = 5.dp)
                    )
                }

                EducationalSubject.entries.forEach { subject ->
                    val isSelected = selectedSubjectFilter == subject
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant).clickable { selectedSubjectFilter = subject }
                    ) {
                        Text(
                            subject.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = SpacingTokens.m, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(SpacingTokens.m))

            // Curated Units List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.m)
            ) {
                items(filteredUnits) { unit ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(SpacingTokens.l)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        unit.unitTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        unit.collectionTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        unit.gradeBand.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxxs)
                                    )
                                }
                            }

                            Spacer(Modifier.height(SpacingTokens.xs))

                            Text(
                                unit.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(SpacingTokens.xs))

                            // Standard code & concepts
                            Text(
                                tf("standard_2", unit.standardCode),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Concepts: ${unit.keyConcepts.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(SpacingTokens.s))

                            // Action buttons: Video, Audio, Ask Llama & Try Practice Problem
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                            ) {
                                val videoItem = unit.mediaResources.find { it.mediaType == com.fourgeailabs.neuropath.data.curriculum.oer.OerMediaType.VIDEO_LESSON }
                                val audioItem = unit.mediaResources.find { it.mediaType == com.fourgeailabs.neuropath.data.curriculum.oer.OerMediaType.AUDIO_LECTURE }

                                if (videoItem != null) {
                                    Surface(
                                        shape = ShapeTokens.Small,
                                        color = Color(0xFFEFF6FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                            .clickable { activeMediaResource = videoItem }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = SpacingTokens.xs),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(t("video"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                        }
                                    }
                                }

                                if (audioItem != null) {
                                    Surface(
                                        shape = ShapeTokens.Small,
                                        color = Color(0xFFFDF2F8),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF472B6)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                            .clickable { activeMediaResource = audioItem }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = SpacingTokens.xs),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(t("audio"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                                        }
                                    }
                                }

                                Surface(
                                    shape = ShapeTokens.Small,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                        .clickable {
                                            onSelectUnit("Explain the OER Commons curriculum unit '${unit.unitTitle}' (${unit.standardCode}) with step-by-step concepts and real-world examples.")
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = SpacingTokens.xs),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(t("ask_llama"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }

                                if (unit.practiceProblems.isNotEmpty()) {
                                    val prob = unit.practiceProblems.first()
                                    Surface(
                                        shape = ShapeTokens.Small,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .sizeIn(minWidth = SpacingTokens.giant, minHeight = SpacingTokens.giant)
                                            .clickable {
                                                onSelectUnit("Let's solve this OER Commons practice problem from '${unit.unitTitle}': \"${prob.questionPrompt}\". Guide me step-by-step!")
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = SpacingTokens.xs),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(t("practice"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeMediaResource != null) {
            OerMultimediaPlayerBottomSheet(
                resource = activeMediaResource!!,
                onDismiss = { activeMediaResource = null }
            )
        }
    }
}
