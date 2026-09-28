package com.fourgeailabs.neuropath.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialPackEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflinePackStatus
import com.fourgeailabs.neuropath.data.repository.OfflinePackManager
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.t
import com.fourgeailabs.neuropath.ui.tf

/**
 * Offer shown to the parent right after a location/framework is selected
 * (new profile, or a framework change in settings). Honest about what the
 * pack is: the complete NeuroPath lesson library for the child's framework,
 * prepared on the device — no internet needed.
 */
@Composable
fun OfflinePackOfferDialog(
    viewModel: NeuroPathViewModel,
    offer: NeuroPathViewModel.PackOffer,
    onDismiss: () -> Unit
) {
    var sizeLine by remember { mutableStateOf<String?>(null) }
    // Resolve the real size estimate once; a loading state shows in the meantime.
    androidx.compose.runtime.LaunchedEffect(offer.profileId) {
        val bytes = runCatching { viewModel.estimateOfflinePackSizeBytes() }.getOrNull()
        sizeLine = bytes?.let {
            "${t("offline_pack_offer_size")}: ${OfflinePackManager.formatBytes(it)}"
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                tf("offline_pack_offer_title", offer.childName),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("offline_pack_offer_body"))
                Text(
                    "${t("offline_pack_offer_framework")}: ${offer.frameworkLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sizeLine != null) {
                    Text(
                        sizeLine!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    t("offline_pack_offer_no_internet_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    t("offline_pack_offer_later_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.startOfflinePackDownload()
                onDismiss()
            }) {
                Text(t("offline_pack_download_now"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(t("offline_pack_skip"))
            }
        }
    )
}

/**
 * Parent Dashboard card for one profile's offline material pack: status,
 * lesson count, actual size, and download / re-download / cancel / delete
 * controls. Lives in the parent-gated Standards tab.
 */
@Composable
fun OfflinePackSection(
    profileId: Long,
    childName: String,
    currentFrameworkKey: String,
    currentFrameworkLabel: String,
    pack: OfflineMaterialPackEntity?,
    progress: Float?,
    error: String?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onClearError: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isStale = pack != null &&
        pack.status == OfflinePackStatus.READY.name &&
        pack.frameworkKey != currentFrameworkKey
    val downloading = progress != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📦", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        t("offline_pack_section_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$childName — $currentFrameworkLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status line
            val statusText = when {
                downloading -> t("offline_pack_status_downloading")
                pack == null || pack.status == OfflinePackStatus.NOT_DOWNLOADED.name ->
                    t("offline_pack_status_not_downloaded")
                pack.status == OfflinePackStatus.READY.name && isStale ->
                    t("offline_pack_status_stale")
                pack.status == OfflinePackStatus.READY.name ->
                    t("offline_pack_status_ready")
                else -> t("offline_pack_status_not_downloaded")
            }
            Text(statusText, fontWeight = FontWeight.SemiBold)

            // Size / lesson count
            if (pack != null && (pack.status == OfflinePackStatus.READY.name)) {
                Text(
                    tf(
                        "offline_pack_size_line",
                        pack.totalLessons.toString(),
                        OfflinePackManager.formatBytes(pack.totalBytes)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isStale) {
                Text(
                    t("offline_pack_stale_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9C6D00)
                )
            }

            // Progress
            if (downloading) {
                LinearProgressIndicator(
                    progress = { progress ?: 0f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    t("offline_pack_progress_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Error (retryable, visible)
            if (error != null) {
                Text(
                    error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = onClearError) {
                    Text(t("offline_pack_dismiss_error"))
                }
            }

            // Honesty note: what the pack is and isn't.
            Text(
                t("offline_pack_honesty_note"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    downloading -> {
                        Button(onClick = onCancel) {
                            Text(t("offline_pack_cancel"))
                        }
                    }
                    pack != null && pack.status == OfflinePackStatus.READY.name && !isStale -> {
                        OutlinedButton(onClick = onDownload) {
                            Text(t("offline_pack_redownload"))
                        }
                        OutlinedButton(onClick = { showDeleteConfirm = true }) {
                            Text(
                                t("offline_pack_delete"),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        Button(onClick = onDownload) {
                            Text(
                                if (isStale) t("offline_pack_redownload")
                                else t("offline_pack_download")
                            )
                        }
                        if (pack != null && pack.status == OfflinePackStatus.READY.name) {
                            OutlinedButton(onClick = { showDeleteConfirm = true }) {
                                Text(
                                    t("offline_pack_delete"),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(t("offline_pack_delete_title"), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    tf("offline_pack_delete_body", childName)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text(t("offline_pack_delete_confirm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(t("offline_pack_delete_cancel"))
                }
            }
        )
    }
}
