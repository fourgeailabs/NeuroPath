package com.fourgeailabs.neuropath.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shared corner radii, ranked by how often each radius is already used across
 * ui/ (12dp ≈ 75 uses, 20dp ≈ 41, 16dp ≈ 39, 10dp ≈ 39, 14dp ≈ 34, 8dp ≈ 24,
 * 6dp ≈ 16, 4dp ≈ 7).
 */
object ShapeTokens {
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(20.dp)

    /** Chips, pills, avatar circles. */
    val Pill = RoundedCornerShape(50)

    /** Bottom-sheet / dialog top corners (24dp already used for these). */
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}
