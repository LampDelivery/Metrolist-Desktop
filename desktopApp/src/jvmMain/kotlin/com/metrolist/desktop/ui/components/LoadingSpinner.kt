package com.metrolist.desktop.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val color = MaterialTheme.colorScheme.primary
    val strokeWidth = 2.dp
    val amplitude = 8.dp
    val wavelength = 48.dp
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val points = 32
        val step = width / points
        val ampPx = amplitude.toPx()
        val wavePx = wavelength.toPx()
        val strokePx = strokeWidth.toPx()
        var prev = Offset(0f, centerY)
        for (i in 1..points) {
            val x = i * step
            val y = centerY + ampPx * kotlin.math.sin((x / wavePx) * 2f * Math.PI.toFloat() + phase)
            drawLine(
                color = color,
                start = prev,
                end = Offset(x, y),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
            prev = Offset(x, y)
        }
    }
}