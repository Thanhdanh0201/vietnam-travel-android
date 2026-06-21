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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.WeatherData
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray600
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import kotlin.math.roundToInt

val WeatherWidgetHeight = 132.dp
private val WidgetHeight = WeatherWidgetHeight
private val WidgetRadius = 20.dp
private val LocationHeaderHeight = 34.dp
private val WeatherBodyMainHeight = 64.dp

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
        shadowElevation = 6.dp,
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
    theme: WeatherTheme,
    onFavoriteClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (onFavoriteClick == null) return
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                if (theme.isDark) {
                    Color.White.copy(alpha = if (isFavorite) 0.28f else 0.14f)
                } else {
                    VNRed.copy(alpha = if (isFavorite) 0.18f else 0.1f)
                },
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onFavoriteClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Đã đặt làm mặc định" else "Đặt làm thành phố mặc định",
            tint = if (theme.isDark) {
                if (isFavorite) Color.White else Color.White.copy(alpha = 0.92f)
            } else {
                if (isFavorite) VNRed else VNRed.copy(alpha = 0.75f)
            },
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
                .background(
                    Brush.linearGradient(
                        colors = theme.gradient,
                        start = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 24.dp, y = (-12).dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(theme.glowColor.copy(alpha = 0.22f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 20.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(theme.glowColor.copy(alpha = 0.12f)),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (theme.isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.06f),
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.03f),
                            )
                        },
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
    theme: WeatherTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LocationHeaderHeight)
            .padding(end = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(theme.iconSurface),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = theme.iconTint,
                modifier = Modifier.size(13.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = locationName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = theme.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = locationSubtitle?.takeIf { it.isNotBlank() } ?: "\u00A0",
                style = MaterialTheme.typography.labelSmall,
                color = theme.secondaryText,
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
                theme = theme,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                WeatherWidgetLocationHeader(
                    locationName = locationName,
                    locationSubtitle = locationSubtitle,
                    theme = theme,
                )
                Spacer(modifier = Modifier.height(6.dp))
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
        theme = weatherTheme(isDark = false),
        locationName = locationName,
        locationSubtitle = locationSubtitle,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        bodyMain = {
            val theme = weatherTheme(isDark = false)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WeatherConditionIllustration(condition = "partly_cloudy")
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = theme.accent,
                    strokeWidth = 2.5.dp,
                )
                Text(
                    text = "Đang tải thời tiết…",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.secondaryText,
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
        theme = weatherTheme(isDark = false),
        locationName = locationName,
        locationSubtitle = locationSubtitle,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        bodyMain = {
            val theme = weatherTheme(isDark = false)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WeatherConditionIllustration(condition = "cloudy")
                Text(
                    text = "Chưa có dữ liệu — bật backend và thử lại",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
    val theme = weatherTheme(isDark = weather.condition == "stormy")
    val tempNow = ((weather.tempMax + weather.tempMin) / 2.0).roundToInt()
    val conditionText = getConditionText(weather.condition)
    val rainMm = weather.rainMm.coerceAtLeast(0.0)
    val detailText = when {
        rainMm > 0.1 -> "Mưa ~${rainMm.roundToInt()} mm"
        weather.humidity != null -> "Độ ẩm ${weather.humidity}%"
        else -> "\u00A0"
    }

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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WeatherConditionIllustration(
                    condition = weather.condition,
                    modifier = Modifier.size(52.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "$tempNow",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryText,
                            letterSpacing = (-1).sp,
                            lineHeight = 36.sp,
                        )
                        Text(
                            text = "°C",
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.mutedText,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                        )
                    }
                    Text(
                        text = conditionText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryText.copy(alpha = 0.92f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(92.dp),
                ) {
                    Text(
                        text = "H:${weather.tempMax.roundToInt()}°",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = theme.primaryText.copy(alpha = 0.88f),
                    )
                    Text(
                        text = "L:${weather.tempMin.roundToInt()}°",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = theme.mutedText,
                    )
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.secondaryText,
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier.height(14.dp),
                    )
                }
            }
        },
    )
}

private data class WeatherTheme(
    val gradient: List<Color>,
    val glowColor: Color,
    val isDark: Boolean,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val iconSurface: Color,
    val iconTint: Color,
    val accent: Color,
)

private fun weatherTheme(isDark: Boolean): WeatherTheme = if (isDark) {
    WeatherTheme(
        gradient = listOf(Color(0xFF082276), Color(0xFF030F34)),
        glowColor = Color(0xFF713FFD),
        isDark = true,
        primaryText = Color.White,
        secondaryText = Color.White.copy(alpha = 0.78f),
        mutedText = Color.White.copy(alpha = 0.72f),
        iconSurface = Color.White.copy(alpha = 0.16f),
        iconTint = Color.White.copy(alpha = 0.95f),
        accent = Color.White,
    )
} else {
    WeatherTheme(
        gradient = listOf(Color(0xFFEBEBEB), Color(0xFFEEEEEE)),
        glowColor = Color(0xFFCBD5E1),
        isDark = false,
        primaryText = SlateGray900,
        secondaryText = SlateGray600,
        mutedText = SlateGray500,
        iconSurface = VNRed.copy(alpha = 0.1f),
        iconTint = VNRed,
        accent = VNRed,
    )
}

private fun getConditionText(condition: String): String = when (condition) {
    "sunny" -> "Trời nắng"
    "partly_cloudy" -> "Ít mây"
    "cloudy" -> "Nhiều mây"
    "rainy" -> "Có mưa"
    "stormy" -> "Có giông"
    else -> "Trời đẹp"
}
