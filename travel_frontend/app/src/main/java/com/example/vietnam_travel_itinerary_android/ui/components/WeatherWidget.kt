package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.WeatherData
import com.example.vietnam_travel_itinerary_android.ui.theme.OrangeAccent
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray400
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@Composable
fun WeatherWidget(
    weather: WeatherData?,
    locationName: String = "Đà Lạt",
    modifier: Modifier = Modifier
) {
    val temp = weather?.let { ((it.tempMax + it.tempMin) / 2).toInt() } ?: 22
    val feelsLike = weather?.let { it.tempMax.toInt() } ?: 24
    val condition = weather?.condition ?: "sunny"
    val conditionText = getConditionText(condition)
    val conditionIcon = getConditionIcon(condition)
    val conditionColor = getConditionColor(condition)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                VNRed.copy(alpha = 0.05f)
            )
        ),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Location + Temperature
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SlateGray500
                    )
                    Text(
                        text = locationName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGray500,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${temp}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // Center: Condition text
            Column {
                Text(
                    text = conditionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray500
                )
                Text(
                    text = "Cảm giác ${feelsLike}°C",
                    style = MaterialTheme.typography.titleSmall,
                    color = VNRed
                )
            }

            // Right: Weather icon
            Icon(
                imageVector = conditionIcon,
                contentDescription = conditionText,
                modifier = Modifier.size(40.dp),
                tint = conditionColor
            )
        }
    }
}

private fun getConditionText(condition: String): String = when (condition) {
    "sunny" -> "Trời nắng nhẹ"
    "partly_cloudy" -> "Có mây rải rác"
    "cloudy" -> "Trời nhiều mây"
    "rainy" -> "Có mưa"
    "stormy" -> "Có giông"
    else -> "Trời nắng nhẹ"
}

private fun getConditionIcon(condition: String): ImageVector = when (condition) {
    "sunny" -> Icons.Outlined.WbSunny
    "partly_cloudy" -> Icons.Outlined.Cloud
    "cloudy" -> Icons.Outlined.Cloud
    "rainy" -> Icons.Outlined.WaterDrop
    "stormy" -> Icons.Outlined.Thunderstorm
    else -> Icons.Outlined.WbSunny
}

private fun getConditionColor(condition: String): Color = when (condition) {
    "sunny" -> OrangeAccent
    "partly_cloudy" -> SlateGray400
    "cloudy" -> SlateGray400
    "rainy" -> Color(0xFF60A5FA)
    "stormy" -> SlateGray500
    else -> OrangeAccent
}
