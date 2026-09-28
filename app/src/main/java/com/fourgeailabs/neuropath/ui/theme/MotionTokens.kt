package com.fourgeailabs.neuropath.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

/**
 * Standard animation durations and easings. FastOutSlowInEasing and
 * LinearEasing are the two easings already in use across ui/; the longer
 * 700/1000ms durations belong to the typewriter loading effect and stay
 * where they are.
 */
object MotionTokens {
    /** Hover/ripple-scale feedback, icon state changes. */
    const val Fast = 200

    /** Default screen and panel transitions. */
    const val Medium = 300

    /** Emphasis transitions, large layout shifts. */
    const val Slow = 500

    val StandardEasing: Easing = FastOutSlowInEasing
    val EmphasizedEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val Linear: Easing = LinearEasing

    /**
     * The standard tween for transitions: 300ms with FastOutSlowIn easing,
     * matching the conventions already used in the codebase.
     */
    fun <T> standard(durationMs: Int = Medium, easing: Easing = StandardEasing) =
        tween<T>(durationMillis = durationMs, easing = easing)
}
