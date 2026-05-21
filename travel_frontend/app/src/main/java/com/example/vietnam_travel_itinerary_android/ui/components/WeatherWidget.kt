package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.WeatherData
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRedLight
import kotlin.math.roundToInt

val WeatherWidgetHeight = 124.dp
private val WidgetHeight = WeatherWidgetHeight
private val WidgetRadius = 18.dp
private val LocationHeaderHeight = 36.dp
private val WeatherBodyMainHeight = 56.dp

@Composable
fun WeatherWidget(
    weather: WeatherData?,
    locationName: String = "Đà Lạt",
    locationSubtitle: String? = null,
    isLoading: Boolean = false,
    loadFailed: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(WidgetHeight),
        shape = RoundedCornerShape(WidgetRadius),
        shadowElevation = 4.dp,
        color = Color.Transparent,
    ) {
        when {
            loadFailed -> WeatherWidgetError(
                locationName = locationName,
                locationSubtitle = locationSubtitle,
                isFavorite = isFavorite,
                onFavoriteClick = onFavoriteClick,
            )
            isLoading || weather == null -> WeatherWidgetLoading(
                locationName = locationName,
                locationSubtitle = locationSubtitle,
                isFavorite = isFavorite,
                onFavoriteClick = onFavoriteClick,
            )
            else -> {
                WeatherWidgetContent(
                    weather = weather,
                    locationName = locationName,
                    locationSubtitle = locationSubtitle,
                    isFavorite = isFavorite,
                    onFavoriteClick = onFavoriteClick,
                )
            }
        }
    }
}

@Composable
private fun WeatherFavoriteButton(
    isFavorite: Boolean,
    onFavoriteClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (onFavoriteClick == null) return
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (isFavorite) 0.35f else 0.18f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onFavoriteClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Đã đặt làm mặc định" else "Đặt làm thành phố mặc định",
            tint = if (isFavorite) Color.White else Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun WeatherWidgetGradientBox(
    theme: WeatherTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WidgetHeight)
            .clip(RoundedCornerShape(WidgetRadius)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(theme.gradient)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 36.dp)
                .size(width = 320.dp, height = 110.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = theme.gradient.map { it.copy(alpha = 0.85f) },
                    ),
                ),
        )
        content()
    }
}

@Composable
private fun WeatherWidgetLocationHeader(
    locationName: String,
    locationSubtitle: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LocationHeaderHeight)
            .padding(end = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(13.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = locationName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = locationSubtitle?.takeIf { it.isNotBlank() } ?: "\u00A0",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
                modifier = Modifier.height(14.dp),
            )
        }
    }
}

@Composable
private fun WeatherWidgetFrame(
    theme: WeatherTheme,
    locationName: String,
    locationSubtitle: String?,
    isFavorite: Boolean,
    onFavoriteClick: (() -> Unit)?,
    bodyMain: @Composable () -> Unit,
) {
    WeatherWidgetGradientBox(theme = theme) {
        Box(modifier = Modifier.fillMaxSize()) {
            WeatherFavoriteButton(
                isFavorite = isFavorite,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                WeatherWidgetLocationHeader(
                    locationName = locationName,
                    locationSubtitle = locationSubtitle,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeatherBodyMainHeight),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    bodyMain()
                }
            }
        }
    }
}

@Composable
private fun WeatherWidgetLoading(
    locationName: String,
    locationSubtitle: String?,
    isFavorite: Boolean,
    onFavoriteClick: (() -> Unit)?,
) {
    WeatherWidgetFrame(
        theme = weatherThemes("partly_cloudy"),
        locationName = locationName,
        locationSubtitle = locationSubtitle,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        bodyMain = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                )
                Text(
                    text = "Đang tải thời tiết…",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        },
    )
}

@Composable
private fun WeatherWidgetError(
    locationName: String,
    locationSubtitle: String?,
    isFavorite: Boolean,
    onFavoriteClick: (() -> Unit)?,
) {
    WeatherWidgetFrame(
        theme = weatherThemes("partly_cloudy"),
        locationName = locationName,
        locationSubtitle = locationSubtitle,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        bodyMain = {
            Text(
                text = "Chưa có dữ liệu — bật backend và thử lại",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun WeatherWidgetContent(
    weather: WeatherData,
    locationName: String,
    locationSubtitle: String?,
    isFavorite: Boolean,
    onFavoriteClick: (() -> Unit)?,
) {
    val theme = weatherThemes(weather.condition)
    val tempNow = ((weather.tempMax + weather.tempMin) / 2.0).roundToInt()
    val conditionText = getConditionText(weather.condition)
    val conditionIcon = getConditionIcon(weather.condition)

    WeatherWidgetFrame(
        theme = theme,
        locationName = locationName,
        locationSubtitle = locationSubtitle,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        bodyMain = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = conditionIcon,
                    contentDescription = conditionText,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White,
                )
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "$tempNow",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = "°C",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, start = 1.dp),
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(88.dp),
                ) {
                    Text(
                        text = conditionText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val rainMm = weather.rainMm.coerceAtLeast(0.0)
                    Text(
                        text = if (rainMm > 0.1) "Mưa ~${rainMm.roundToInt()} mm" else "\u00A0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier.height(14.dp),
                    )
                }
            }
        },
    )
}

private data class WeatherTheme(val gradient: List<Color>)

private fun weatherThemes(condition: String): WeatherTheme = when (condition) {
    "sunny" -> WeatherTheme(listOf(Color(0xFFFFB347), Color(0xFFE8475E), VNRed))
    "partly_cloudy" -> WeatherTheme(listOf(Color(0xFF9BF8F4), Color(0xFF6F7BF7)))
    "cloudy" -> WeatherTheme(listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF475569)))
    "rainy" -> WeatherTheme(listOf(Color(0xFF9BF8F4), Color(0xFF6F7BF7)))
    "stormy" -> WeatherTheme(listOf(Color(0xFF0E0E11), Color(0xFF383C47)))
    else -> WeatherTheme(listOf(Color(0xFF9BF8F4), VNRedLight))
}

private fun getConditionText(condition: String): String = when (condition) {
    "sunny" -> "Trời nắng"
    "partly_cloudy" -> "Ít mây"
    "cloudy" -> "Nhiều mây"
    "rainy" -> "Có mưa"
    "stormy" -> "Có giông"
    else -> "Trời đẹp"
}

private fun getConditionIcon(condition: String): ImageVector = when (condition) {
    "sunny" -> Icons.Outlined.WbSunny
    "partly_cloudy" -> Icons.Outlined.Cloud
    "cloudy" -> Icons.Outlined.Cloud
    "rainy" -> Icons.Outlined.WaterDrop
    "stormy" -> Icons.Outlined.Thunderstorm
    else -> Icons.Outlined.WbSunny
}
