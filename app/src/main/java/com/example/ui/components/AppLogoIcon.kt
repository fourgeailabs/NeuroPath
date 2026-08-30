package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLogoIcon(
    size: Dp = 100.dp,
    showText: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF338CA8),
                            Color(0xFF1E5B70),
                            Color(0xFF0F323F)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val radius = this.size.minDimension / 2f

                // Subtle outer ring
                drawCircle(
                    color = Color(0x7755C2E0),
                    radius = radius - 2.dp.toPx(),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Lightbulb outline parameters
                val bRadius = radius * 0.42f
                val topY = center.y - radius * 0.12f

                val path = Path().apply {
                    // Top circular dome of lightbulb
                    addArc(
                        oval = Rect(
                            left = center.x - bRadius,
                            top = topY - bRadius,
                            right = center.x + bRadius,
                            bottom = topY + bRadius
                        ),
                        startAngleDegrees = 35f,
                        sweepAngleDegrees = 110f
                    )
                }

                // Complete lightbulb body outline path
                val bulbPath = Path().apply {
                    moveTo(center.x - bRadius * 0.85f, topY + bRadius * 0.5f)
                    // Curve around dome
                    cubicTo(
                        center.x - bRadius * 1.25f, topY - bRadius * 0.4f,
                        center.x + bRadius * 1.25f, topY - bRadius * 0.4f,
                        center.x + bRadius * 0.85f, topY + bRadius * 0.5f
                    )
                    // Taper down to base
                    val baseTopY = center.y + radius * 0.38f
                    val baseWidth = radius * 0.22f
                    lineTo(center.x + baseWidth, baseTopY)
                    lineTo(center.x - baseWidth, baseTopY)
                    close()
                }

                drawPath(
                    path = bulbPath,
                    color = Color.White,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Yellow horizontal base ridges
                val baseTopY = center.y + radius * 0.43f
                val ridgeColor = Color(0xFFFFD54F)
                for (i in 0..2) {
                    val y = baseTopY + i * 5.dp.toPx()
                    val w = (radius * 0.38f) - (i * 3.dp.toPx())
                    drawLine(
                        color = ridgeColor,
                        start = Offset(center.x - w / 2, y),
                        end = Offset(center.x + w / 2, y),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Inner yellow filament wave ~
                val wavePath = Path().apply {
                    val sX = center.x - radius * 0.22f
                    val sY = center.y - radius * 0.05f
                    moveTo(sX, sY)
                    cubicTo(
                        sX + radius * 0.12f, sY - radius * 0.18f,
                        sX + radius * 0.28f, sY + radius * 0.12f,
                        sX + radius * 0.44f, sY - radius * 0.05f
                    )
                }
                drawPath(
                    path = wavePath,
                    color = Color(0xFFFFD54F),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Cyan sparkle (top-left)
                val spark1Center = Offset(center.x - radius * 0.56f, center.y - radius * 0.38f)
                drawSparkle(spark1Center, radius * 0.18f, Color(0xFF4DD0E1))

                // Golden yellow star sparkle (top-right)
                val spark2Center = Offset(center.x + radius * 0.54f, center.y - radius * 0.44f)
                drawSparkle(spark2Center, radius * 0.24f, Color(0xFFFFCA28))
            }
        }

        if (showText) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "NeuroPath",
                fontSize = (size.value * 0.24f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path = path, color = color)
}
