package com.screen.autolocker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.utils.formatTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PremiumTimerDial(
    color: AppPalette,
    isActive: Boolean,
    minutes: Float,
    remaining: Long,
    total: Long,
    progressColor: Color,
    onMinutesChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val min = 1f
    val max = 120f
    var lastAngle by remember { mutableStateOf(0f) }
    var totalRotation by remember { mutableStateOf(0f) }

    LaunchedEffect(minutes) {
        if (!isActive) {
            totalRotation = ((minutes - min) / (max - min)) * 360f
        }
    }

    val inactiveProgress = ((minutes - min) / (max - min)).coerceIn(0f, 1f)
    val activeProgress = if (total > 0) {
        (remaining.toFloat() / total).coerceIn(0f, 1f)
    } else {
        1f
    }

    val dialProgress by animateFloatAsState(
        targetValue = if (isActive) activeProgress else inactiveProgress,
        animationSpec = tween(if (isActive) 220 else 180),
        label = "dialProgress"
    )

    val knobAlpha by animateFloatAsState(
        targetValue = if (isActive) 0f else 1f,
        animationSpec = tween(220),
        label = "knobAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .pointerInput(isActive) {
                    if (isActive) return@pointerInput

                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val rawAngle = atan2(dy, dx)
                            var angle = Math.toDegrees(rawAngle.toDouble()).toFloat()
                            angle = (angle + 360f) % 360f
                            lastAngle = angle
                        },
                        onDragEnd = { lastAngle = 0f }
                    ) { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touch = change.position
                        val dx = touch.x - center.x
                        val dy = touch.y - center.y
                        val rawAngle = atan2(dy, dx)
                        var angle = Math.toDegrees(rawAngle.toDouble()).toFloat()
                        angle = (angle + 360f) % 360f

                        var delta = angle - lastAngle
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        val nextRotation = totalRotation + delta
                        val resistedDelta = when {
                            nextRotation < 0f && delta < 0f -> delta * 0.22f
                            nextRotation > 360f && delta > 0f -> delta * 0.22f
                            totalRotation < 18f && delta < 0f -> delta * 0.45f
                            totalRotation > 342f && delta > 0f -> delta * 0.45f
                            else -> delta
                        }

                        totalRotation = (totalRotation + resistedDelta).coerceIn(-24f, 384f)
                        lastAngle = angle

                        val normalized = (totalRotation / 360f).coerceIn(0f, 1f)
                        val rawValue = min + normalized * (max - min)
                        onMinutesChange(rawValue.coerceIn(min, max))
                    }
                }
        ) {
            val stroke = 12.dp.toPx()
            val knobRadius = 12.dp.toPx()
            val radius = (size.minDimension / 2f) - stroke - knobRadius
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)
            val sweep = dialProgress * 360f

            drawCircle(
                color = color.muted.copy(alpha = 0.22f),
                radius = radius,
                center = center,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )

            val angleRad = Math.toRadians((sweep - 90f).toDouble())
            val knobCenter = Offset(
                x = center.x + cos(angleRad).toFloat() * radius,
                y = center.y + sin(angleRad).toFloat() * radius
            )

            if (knobAlpha > 0f) {
                drawCircle(
                    color = color.surface.copy(alpha = knobAlpha),
                    radius = knobRadius,
                    center = knobCenter
                )

                drawCircle(
                    color = progressColor.copy(alpha = knobAlpha),
                    radius = 6.dp.toPx(),
                    center = knobCenter
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isActive) formatTime(remaining) else minutes.toInt().toString(),
                style = if (isActive) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge,
                fontSize = if (isActive) 42.sp else 64.sp,
                fontWeight = FontWeight.Bold,
                color = color.text
            )
            if (!isActive) {
                Text("minutes", color = color.muted)
            }
        }
    }
}
