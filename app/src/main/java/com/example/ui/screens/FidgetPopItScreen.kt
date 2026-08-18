package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

enum class PopItColorStyle(val title: String, val colors: List<Color>) {
    RAINBOW("Rainbow", listOf(Color(0xFFFF9AA2), Color(0xFFFFB7B2), Color(0xFFFFDAC1), Color(0xFFE2F0CB), Color(0xFFB5EAD7), Color(0xFFC7CEEA))),
    PASTEL_LILAC("Lavender", listOf(Color(0xFFE8D7FF), Color(0xFFD0BCFF), Color(0xFFC2A3FF), Color(0xFFB488FF))),
    GALAXY("Cosmic", listOf(Color(0xFF3D348B), Color(0xFF7678ED), Color(0xFFF7B801), Color(0xFFF18701))),
    MINT("Ocean Mint", listOf(Color(0xFF52B788), Color(0xFF74C69D), Color(0xFF95D5B2), Color(0xFFD8F3DC)))
}

@Composable
fun FidgetPopItScreen(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val bubbles by viewModel.popItBubbles.collectAsState()
    val totalPops by viewModel.totalPoppedCount.collectAsState()
    var selectedStyle by remember { mutableStateOf(PopItColorStyle.RAINBOW) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.testTag("popit_back_btn")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🫧 Tactile Silicone Pop-It",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Decompression & Sensory Reset",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "$totalPops Pops",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Style Selector Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            PopItColorStyle.values().forEach { style ->
                val isSelected = selectedStyle == style
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { selectedStyle = style }
                ) {
                    Text(
                        style.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 16-Bubble Silicone Pad Grid
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(16) { index ->
                    val isPopped = bubbles.getOrElse(index) { false }
                    val scale by animateFloatAsState(
                        targetValue = if (isPopped) 0.86f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
                        label = "popScale"
                    )

                    val bubbleColor = selectedStyle.colors[index % selectedStyle.colors.size]

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                if (isPopped) bubbleColor.copy(alpha = 0.5f) else bubbleColor
                            )
                            .clickable {
                                viewModel.popBubble(index)
                            }
                            .testTag("bubble_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner concave ring highlight
                        Surface(
                            shape = CircleShape,
                            color = if (isPopped) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(if (isPopped) 28.dp else 40.dp)
                        ) {}
                    }
                }
            }
        }

        // Bottom Controls: Flip/Reset & 4-7-8 Breathing Shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.resetPopIt() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("reset_popit_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Pad")
                Spacer(Modifier.width(6.dp))
                Text("Flip Pad", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.navigateTo(AppScreen.BREATHING_GUIDE) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("4-7-8 Breathing 🧘", fontWeight = FontWeight.Bold)
            }
        }
    }
}
