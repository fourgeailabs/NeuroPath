package com.fourgeailabs.neuropath.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fourgeailabs.neuropath.data.model.tr
import java.util.Locale

/**
 * Global holder for the active app language code (e.g. "en-US", "es").
 *
 * Set synchronously from the current child profile at the composition root (MainActivity).
 * It is Compose state (not a plain var) so that changing it recomposes every composable
 * that rendered a [t]/[tf] string; non-composable callers (onClick handlers, Toasts,
 * speech synthesis, the ViewModel) simply read the current value, which is thread-safe.
 * The underlying dictionaries are immutable; reads never crash.
 */
object AppStrings {
    var language: String by mutableStateOf("en-US")
}

/** Localised UI string for [key]. Falls back to en-US, then to the key itself — never crashes. */
fun t(key: String): String = tr(key, AppStrings.language)

/**
 * Localised format string for [key]. Dictionary values use positional specifiers
 * (%1$s, %2$d) so translators can reorder arguments. Plain [t] strings never go through
 * the formatter, so literal % and $ characters are safe there.
 */
fun tf(key: String, vararg args: Any?): String =
    String.format(Locale.ROOT, tr(key, AppStrings.language), *args)
