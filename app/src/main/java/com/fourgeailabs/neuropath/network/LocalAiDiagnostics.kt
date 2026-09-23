package com.fourgeailabs.neuropath.network

import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocalAiDiagnosticsSnapshot(
    val device: String,
    val soc: String,
    val runtime: String,
    val model: String,
    val requestedBackend: LocalAiBackend?,
    val activeBackend: LocalAiBackend?,
    val status: String,
    val failureReason: String?,
    val fallbackReason: String?
)

object LocalAiDiagnostics {
    private val _snapshot = MutableStateFlow<LocalAiDiagnosticsSnapshot?>(null)
    val snapshot: StateFlow<LocalAiDiagnosticsSnapshot?> = _snapshot.asStateFlow()

    fun recordAttempt(
        context: Context,
        model: String,
        requestedBackend: LocalAiBackend,
        failureReason: String? = null
    ) {
        _snapshot.value = snapshot(context, model, requestedBackend, null, failureReason, null)
    }

    fun recordSuccess(
        context: Context,
        model: String,
        requestedBackend: LocalAiBackend,
        activeBackend: LocalAiBackend
    ) {
        _snapshot.value = snapshot(context, model, requestedBackend, activeBackend, null, null)
    }

    fun recordFallback(
        context: Context,
        model: String,
        requestedBackend: LocalAiBackend?,
        fallbackReason: String
    ) {
        _snapshot.value = snapshot(context, model, requestedBackend, null, null, fallbackReason)
    }

    private fun snapshot(
        context: Context,
        model: String,
        requestedBackend: LocalAiBackend?,
        activeBackend: LocalAiBackend?,
        failureReason: String?,
        fallbackReason: String?
    ): LocalAiDiagnosticsSnapshot {
        val manufacturer = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else Build.MANUFACTURER
        val soc = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else Build.HARDWARE
        return LocalAiDiagnosticsSnapshot(
            device = Build.MODEL,
            soc = "$manufacturer $soc".trim(),
            runtime = "LiteRT-LM",
            model = model,
            requestedBackend = requestedBackend,
            activeBackend = activeBackend,
            status = when {
                activeBackend != null -> "VERIFIED"
                fallbackReason != null -> "FALLBACK"
                else -> "FAILED"
            },
            failureReason = failureReason,
            fallbackReason = fallbackReason
        )
    }
}
