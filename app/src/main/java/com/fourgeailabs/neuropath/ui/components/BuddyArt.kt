package com.fourgeailabs.neuropath.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fourgeailabs.neuropath.R

/**
 * Buddy artwork poses. Each of the 14 Learning Buddies ships these 6 poses as
 * `buddy_<themeId>_<pose>.webp` in drawable-nodpi
 * (see BUDDY_ART_MANIFEST.md in src/main/assets).
 */
enum class BuddyPose(val suffix: String) {
    IDLE("idle"),
    HAPPY("happy"),
    THINKING("thinking"),
    WAVING("waving"),
    COMFORTING("comforting"),
    CELEBRATING("celebrating")
}

/**
 * Resolves the drawable resource id for a buddy pose via the generated resource
 * name. Falls back to the dino idle art for unknown theme ids — never returns 0,
 * never crashes.
 */
@Composable
fun rememberBuddyArtResId(themeId: String, pose: BuddyPose): Int {
    val context = LocalContext.current
    return remember(themeId, pose) {
        val resName = "buddy_${themeId}_${pose.suffix}"
        val found = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (found != 0) found else R.drawable.buddy_dino_idle
    }
}

/**
 * Circular buddy portrait — the drop-in replacement for the old
 * `Text(theme.emoji)` touchpoints.
 */
@Composable
fun BuddyAvatar(
    themeId: String,
    pose: BuddyPose = BuddyPose.IDLE,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val resId = rememberBuddyArtResId(themeId, pose)
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}

/**
 * Maps avatar-shop item ids to buddy theme ids so profile avatars and shop
 * previews can show the matching buddy art. Returns null when an item has no
 * buddy equivalent (hats, badges, …) — callers keep the emoji in that case.
 */
fun avatarShopIdToThemeId(avatarId: String): String? = when (avatarId) {
    "av_robot" -> "robotics"
    "av_dino" -> "dino"
    "av_astronaut" -> "space"
    "av_wizard" -> "magic"
    "av_ocean" -> "ocean"
    "av_unicorn" -> "magic"
    "av_superhero" -> "superhero"
    "av_phoenix" -> "mythical"
    "av_dragon_knight" -> "knights"
    "av_fairy_queen" -> "magic"
    "pet_dragon" -> "mythical"
    "pet_kitsune" -> "space" // Commander Nova is a fox
    "pet_pegasus" -> "magic"
    else -> null
}
