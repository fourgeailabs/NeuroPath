package com.fourgeailabs.neuropath.util

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * True when the user asked the system to reduce motion (accessibility setting).
 * This app serves neurodivergent learners, so honor it: snap animations instead of
 * playing them, and skip infinite ambient motion.
 */
fun isReducedMotionEnabled(context: Context): Boolean {
    return try {
        val resolver = context.contentResolver
        val animatorScale = Settings.Global.getFloat(
            resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
        val transitionScale = Settings.Global.getFloat(
            resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f
        )
        animatorScale == 0f || transitionScale == 0f
    } catch (_: Exception) {
        false
    }
}

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember { isReducedMotionEnabled(context) }
}
