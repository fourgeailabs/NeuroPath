package com.fourgeailabs.neuropath.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Shared elevation values, from the shadow/tonal elevations already used in
 * ui/ (1dp, 2dp, 3dp, 4dp, 6dp, 8dp, with a single 16dp outlier).
 */
object ElevationTokens {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 2.dp
    val Level3 = 3.dp
    val Level4 = 6.dp
    val Level5 = 8.dp
    val Level6 = 16.dp

    /** Default card / list-row elevation. */
    val Card = Level2

    /** Floating action buttons and raised controls. */
    val Fab = Level4

    /** Dialogs and bottom sheets. */
    val Dialog = Level5

    /** Snackbars and toasts. */
    val Snackbar = Level3
}
