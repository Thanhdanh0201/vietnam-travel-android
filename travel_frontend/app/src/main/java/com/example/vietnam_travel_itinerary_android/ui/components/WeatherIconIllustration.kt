package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val SunCore = Color(0xFFFA9E42)
private val SunGlow = Color(0xFFF8E36F)
private val CloudLight = Color(0x99FFFFFF)
private val CloudHighlight = Color(0x98FFFFFF)
private val RainBlue = Color(0xFF42A0F0)
private val ThunderGold = Color(0xFFFFD100)
private val ThunderLight = Color(0xFFFFF0AA)
private val MoonPurple = Color(0xFF713FFD)
private val MoonGlow = Color(0xFFA586FC)
private val StarPurple = Color(0xFF6B38FC)
private val StarLight = Color(0xFF9F7EFD)
@Composable
fun WeatherConditionIllustration(
    condition: String,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(48.dp)) {
        when (condition) {
            "sunny" -> drawSunnyIcon()
            "partly_cloudy" -> drawPartlyCloudyIcon()
            "cloudy" -> drawCloudyIcon()
            "rainy" -> drawRainyIcon()
            "stormy" -> drawStormyIcon()
            else -> drawSunnyIcon()
        }
    }
}

private fun DrawScope.drawSunnyIcon() {
    val cx = size.width * 0.58f
    val cy = size.height * 0.42f
    val coreRadius = size.minDimension * 0.16f

    drawCircle(
        color = SunGlow.copy(alpha = 0.35f),
        radius = coreRadius * 2.2f,
        center = Offset(cx, cy),
    )
    drawCircle(color = SunCore, radius = coreRadius, center = Offset(cx, cy))

    val rayCount = 8
    val innerR = coreRadius * 1.35f
    val outerR = coreRadius * 1.85f
    repeat(rayCount) { index ->
        val angle = Math.toRadians((index * 360.0 / rayCount) - 90.0)
        val start = Offset(
            cx + innerR * cos(angle).toFloat(),
            cy + innerR * sin(angle).toFloat(),
        )
        val end = Offset(
            cx + outerR * cos(angle).toFloat(),
            cy + outerR * sin(angle).toFloat(),
        )
        drawLine(
            color = SunCore.copy(alpha = 0.85f),
            start = start,
            end = end,
            strokeWidth = size.minDimension * 0.025f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawCloud(
    centerX: Float,
    baseY: Float,
    scale: Float,
    alpha: Float = 1f,
) {
    val unit = size.minDimension * scale
    val white = CloudLight.copy(alpha = CloudLight.alpha * alpha)
    val highlight = CloudHighlight.copy(alpha = CloudHighlight.alpha * alpha)

    drawCircle(color = white, radius = unit * 0.28f, center = Offset(centerX - unit * 0.22f, baseY))
    drawCircle(color = highlight, radius = unit * 0.34f, center = Offset(centerX, baseY - unit * 0.12f))
    drawCircle(color = white, radius = unit * 0.26f, center = Offset(centerX + unit * 0.24f, baseY))
    drawRoundRect(
        color = white,
        topLeft = Offset(centerX - unit * 0.42f, baseY - unit * 0.04f),
        size = Size(unit * 0.84f, unit * 0.22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(unit * 0.11f),
    )
}

private fun DrawScope.drawPartlyCloudyIcon() {
    drawCircle(
        color = SunGlow.copy(alpha = 0.25f),
        radius = size.minDimension * 0.14f,
        center = Offset(size.width * 0.72f, size.height * 0.28f),
    )
    drawCircle(
        color = SunCore,
        radius = size.minDimension * 0.1f,
        center = Offset(size.width * 0.72f, size.height * 0.28f),
    )
    drawCloud(centerX = size.width * 0.42f, baseY = size.height * 0.58f, scale = 0.95f)
}

private fun DrawScope.drawCloudyIcon() {
    drawCloud(centerX = size.width * 0.5f, baseY = size.height * 0.52f, scale = 1.1f)
}

private fun DrawScope.drawRainDrop(
    center: Offset,
    height: Float,
    width: Float,
) {
    val path = Path().apply {
        moveTo(center.x, center.y - height * 0.5f)
        quadraticTo(
            center.x + width * 0.55f,
            center.y - height * 0.05f,
            center.x,
            center.y + height * 0.45f,
        )
        quadraticTo(
            center.x - width * 0.55f,
            center.y - height * 0.05f,
            center.x,
            center.y - height * 0.5f,
        )
        close()
    }
    drawPath(path, RainBlue)
    drawCircle(
        color = Color.White,
        radius = width * 0.22f,
        center = Offset(center.x, center.y + height * 0.38f),
    )
}

private fun DrawScope.drawRainyIcon() {
    drawCloud(centerX = size.width * 0.5f, baseY = size.height * 0.4f, scale = 0.88f)
    val dropHeight = size.minDimension * 0.16f
    val dropWidth = size.minDimension * 0.07f
    val baseY = size.height * 0.72f
    listOf(0.28f, 0.42f, 0.56f, 0.7f).forEachIndexed { index, xFactor ->
        rotate(degrees = 18f, pivot = Offset(size.width * xFactor, baseY)) {
            drawRainDrop(
                center = Offset(size.width * xFactor, baseY + index * 2f),
                height = dropHeight,
                width = dropWidth,
            )
        }
    }
}

private fun DrawScope.drawLightningBolt(
    top: Offset,
    scale: Float,
) {
    val unit = size.minDimension * scale
    val path = Path().apply {
        moveTo(top.x, top.y)
        lineTo(top.x + unit * 0.08f, top.y + unit * 0.22f)
        lineTo(top.x - unit * 0.04f, top.y + unit * 0.22f)
        lineTo(top.x + unit * 0.12f, top.y + unit * 0.48f)
        lineTo(top.x + unit * 0.02f, top.y + unit * 0.28f)
        lineTo(top.x + unit * 0.1f, top.y + unit * 0.28f)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(ThunderGold, ThunderLight),
            start = top,
            end = Offset(top.x, top.y + unit * 0.48f),
        ),
    )
}

private fun DrawScope.drawStormyIcon() {
    drawNightAccent()
    drawCloud(centerX = size.width * 0.58f, baseY = size.height * 0.38f, scale = 0.82f, alpha = 0.75f)
    drawLightningBolt(
        top = Offset(size.width * 0.54f, size.height * 0.5f),
        scale = 0.5f,
    )
    val dropHeight = size.minDimension * 0.11f
    val dropWidth = size.minDimension * 0.05f
    val baseY = size.height * 0.8f
    listOf(0.42f, 0.58f, 0.74f).forEach { xFactor ->
        rotate(degrees = 18f, pivot = Offset(size.width * xFactor, baseY)) {
            drawRainDrop(
                center = Offset(size.width * xFactor, baseY),
                height = dropHeight,
                width = dropWidth,
            )
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, rotation: Float) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path()
        val points = 4
        val inner = radius * 0.42f
        for (i in 0 until points * 2) {
            val angle = Math.PI / 2 + i * Math.PI / points
            val r = if (i % 2 == 0) radius else inner
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(StarPurple, StarLight),
                startY = center.y - radius,
                endY = center.y + radius,
            ),
        )
    }
}

private fun DrawScope.drawNightAccent() {
    drawCircle(
        color = MoonGlow.copy(alpha = 0.35f),
        radius = size.minDimension * 0.16f,
        center = Offset(size.width * 0.22f, size.height * 0.24f),
    )
    drawCircle(
        color = MoonPurple,
        radius = size.minDimension * 0.11f,
        center = Offset(size.width * 0.24f, size.height * 0.24f),
        style = Fill,
    )
    drawCircle(
        color = Color(0xFF030F34),
        radius = size.minDimension * 0.09f,
        center = Offset(size.width * 0.28f, size.height * 0.22f),
    )
    drawStar(Offset(size.width * 0.12f, size.height * 0.14f), size.minDimension * 0.035f, -23f)
    drawStar(Offset(size.width * 0.34f, size.height * 0.12f), size.minDimension * 0.028f, 15f)
    drawStar(Offset(size.width * 0.18f, size.height * 0.34f), size.minDimension * 0.024f, 103f)
}
