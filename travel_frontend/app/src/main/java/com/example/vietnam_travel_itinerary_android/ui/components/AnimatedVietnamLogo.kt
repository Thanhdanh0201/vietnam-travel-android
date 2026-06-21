package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.ui.theme.BeVietnamPro
import kotlinx.coroutines.delay

private val VietnamHeaderRed = Color(0xFFBE1E2D)
private val MountainGray = Color(0xFFE5E7EB)

private const val VIEWBOX_WIDTH = 300f
private const val VIEWBOX_HEIGHT = 80f

@Composable
fun AnimatedVietnamLogo(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 148.dp,
) {
    val logoHeight = width * (VIEWBOX_HEIGHT / VIEWBOX_WIDTH)
    val infiniteTransition = rememberInfiniteTransition(label = "vietnam-logo")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave",
    )

    val planeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
        ),
        label = "plane",
    )

    val cyclistProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
        ),
        label = "cyclist",
    )

    val climberProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "climber",
    )

    Box(
        modifier = modifier
            .width(width)
            .height(logoHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val scaleX = size.width / VIEWBOX_WIDTH
            val scaleY = size.height / VIEWBOX_HEIGHT

            scale(scaleX, scaleY) {
                drawMountain(climberProgress)
                drawWave(waveOffset)
                drawPlane(planeProgress)
                drawCyclist(cyclistProgress)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = (-2).dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            "VIETNAM".forEachIndexed { index, letter ->
                AnimatedLogoLetter(
                    letter = letter.toString(),
                    delayMillis = (index + 1) * 100,
                )
            }
        }
    }
}

@Composable
private fun AnimatedLogoLetter(
    letter: String,
    delayMillis: Int,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(delayMillis) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "letter-alpha-$letter",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 10f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "letter-offset-$letter",
    )

    Text(
        text = letter,
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 17.sp,
        color = VietnamHeaderRed.copy(alpha = alpha),
        modifier = Modifier.offset(y = offsetY.dp),
        letterSpacing = 0.5.sp,
    )
}

private fun DrawScope.drawMountain(climberProgress: Float) {
    val mountain = Path().apply {
        moveTo(220f, 60f)
        lineTo(245f, 25f)
        lineTo(270f, 60f)
        close()
    }
    drawPath(mountain, MountainGray)

    val climberX = -5f * climberProgress
    val climberY = -8f * climberProgress
    val climberAlpha = 0.4f + (0.6f * climberProgress)

    translate(248f + climberX, 35f + climberY) {
        val climber = Path().apply {
            moveTo(0f, 0f)
            lineTo(4f, 0f)
            lineTo(2f, -5f)
            close()
        }
        drawPath(climber, VietnamHeaderRed.copy(alpha = climberAlpha))
    }
}

private fun DrawScope.drawWave(waveOffset: Float) {
    val wave = Path().apply {
        moveTo(10f, 65f + waveOffset)
        quadraticBezierTo(150f, 75f + waveOffset, 290f, 65f + waveOffset)
    }
    drawPath(
        path = wave,
        color = VietnamHeaderRed.copy(alpha = 0.3f),
        style = Stroke(width = 2f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawPlane(progress: Float) {
    val startX = -40f
    val endX = 340f
    val startY = 35f
    val endY = -30f
    val x = startX + (endX - startX) * progress
    val y = startY + (endY - startY) * progress
    val alpha = when {
        progress < 0.1f -> progress / 0.1f
        progress > 0.9f -> (1f - progress) / 0.1f
        else -> 1f
    }

    translate(x, y) {
        rotate(-10f) {
            val plane = Path().apply {
                moveTo(0f, 0f)
                lineTo(15f, 5f)
                lineTo(0f, 10f)
                lineTo(3f, 5f)
                close()
            }
            drawPath(plane, VietnamHeaderRed.copy(alpha = alpha))
        }
    }
}

private fun DrawScope.drawCyclist(progress: Float) {
    val x = -30f + (330f - (-30f)) * progress
    translate(x, 0f) {
        drawCircle(
            color = VietnamHeaderRed,
            radius = 3f,
            center = Offset(10f, 72f),
            style = Stroke(width = 1f),
        )
        drawCircle(
            color = VietnamHeaderRed,
            radius = 3f,
            center = Offset(20f, 72f),
            style = Stroke(width = 1f),
        )

        drawLine(
            color = VietnamHeaderRed,
            start = Offset(10f, 72f),
            end = Offset(15f, 67f),
            strokeWidth = 1f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = VietnamHeaderRed,
            start = Offset(15f, 67f),
            end = Offset(20f, 72f),
            strokeWidth = 1f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = VietnamHeaderRed,
            start = Offset(15f, 67f),
            end = Offset(15f, 63f),
            strokeWidth = 1f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = VietnamHeaderRed,
            start = Offset(15f, 63f),
            end = Offset(18f, 63f),
            strokeWidth = 1f,
            cap = StrokeCap.Round,
        )
    }
}
