package com.example.vietnam_travel_itinerary_android.ui.components.introduction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Festival
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.displayLocation
import com.example.vietnam_travel_itinerary_android.ui.theme.BackgroundLight
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray300
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray600
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray800
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SheetShape = RoundedCornerShape(12.dp)
private val ScrimColor = Color(0x990F172A)

@Composable
fun PlaceIntroductionOverlay(
    place: Place,
    onDismiss: () -> Unit,
    onExplore: (Place) -> Unit,
) {
    val province = place.provinces?.name ?: place.cities?.name.orEmpty()
    val locationLine = if (province.isNotBlank()) "$province, Việt Nam" else "Việt Nam"
    val badge =
        when {
            place.type?.contains("heritage", ignoreCase = true) == true -> "Di sản thế giới"
            place.type?.contains("Di sản", ignoreCase = true) == true -> "Di sản thế giới"
            !place.type.isNullOrBlank() -> place.type!!.uppercase(Locale.getDefault())
            else -> "Điểm đến nổi bật"
        }
    val description = place.description?.takeIf { it.isNotBlank() }
        ?: "Khám phá vẻ đẹp và văn hóa đặc trưng của điểm đến này — thông tin chi tiết sẽ được cập nhật từ hệ thống."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss,
            ),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxHeight(0.9f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { /* consume */ },
                ),
            shape = SheetShape,
            colors = CardDefaults.cardColors(
                containerColor = BackgroundLight,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(VNRed.copy(alpha = 0.05f), Color.Transparent),
                            radius = 900f,
                        ),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(288.dp),
                        ) {
                            AsyncImage(
                                model = place.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                BackgroundLight,
                                                Color.Transparent,
                                                Color.Transparent,
                                            ),
                                        ),
                                    ),
                            )
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(VNRed.copy(alpha = 0.2f)),
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(VNRed.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Landscape,
                                    contentDescription = null,
                                    tint = VNRed,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.6.sp,
                                    color = VNRed,
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = place.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray900,
                                lineHeight = 36.sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = null,
                                    tint = VNRed,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = locationLine,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = VNRed,
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SlateGray600,
                                lineHeight = 26.sp,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                HighlightTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.Explore,
                                    label = "Trải nghiệm",
                                    value = place.type?.takeIf { it.isNotBlank() } ?: "Du lịch",
                                )
                                HighlightTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.Map,
                                    label = "Khu vực",
                                    value = province.ifBlank { "Việt Nam" },
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { onExplore(place) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp,
                                ),
                            ) {
                                Text(
                                    text = "Khám phá ngay",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, VNRed.copy(alpha = 0.2f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VNRed),
                            ) {
                                Text(
                                    text = "Đóng",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    VNRed,
                                    VNRed.copy(alpha = 0.6f),
                                    VNRed,
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun HighlightTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .border(1.dp, VNRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(CircleShape)
                .background(VNRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = VNRed, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray500,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SlateGray800,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
fun FestivalIntroductionOverlay(
    event: Event,
    onDismiss: () -> Unit,
    onSchedule: (Event) -> Unit,
) {
    val locationName = event.displayLocation()
    val dateRangeText = remember(event.startDate, event.endDate) {
        formatEventDateRange(event.startDate, event.endDate)
    }
    val subtitle =
        event.description?.lines()?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: "Sự kiện văn hóa đặc sắc tại địa phương."
    val bodyPrimary = event.description?.takeIf { it.isNotBlank() }
        ?: "Lễ hội là dịp để tôn vinh nét văn hóa bản địa, quảng bá du lịch và kết nối cộng đồng."
    val bodySecondary =
        event.description?.lines()?.drop(1)?.joinToString("\n")?.takeIf { it.isNotBlank() }
            ?: "Du khách được trải nghiệm không gian nghệ thuật, ẩm thực và các hoạt động giao lưu đặc sắc."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss,
            ),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxHeight(0.92f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { },
                ),
            shape = SheetShape,
            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(1.dp, VNRed.copy(alpha = 0.1f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(VNRed.copy(alpha = 0.04f), Color.Transparent),
                            radius = 800f,
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(256.dp),
                    ) {
                        AsyncImage(
                            model = null,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SlateGray300),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(BackgroundLight, Color.Transparent, Color.Transparent),
                                    ),
                                ),
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                "Đóng",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp, bottom = 20.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(VNRed)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Festival,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                "Lễ hội văn hóa",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = Color.White,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray900,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VNRed,
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        InfoBlock(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Thời gian diễn ra",
                            value = dateRangeText,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoBlock(
                            icon = Icons.Outlined.Place,
                            label = "Địa điểm",
                            value = locationName.ifBlank { "Cập nhật sau" },
                            modifier = Modifier.fillMaxWidth(),
                            minHeightValue = 48.dp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.MenuBook, null, tint = VNRed, modifier = Modifier.size(22.dp))
                            Text(
                                "Ý nghĩa văn hóa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray900,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(VNRed.copy(alpha = 0.3f)),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = bodyPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SlateGray600,
                                lineHeight = 26.sp,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = bodySecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SlateGray600,
                            lineHeight = 26.sp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onSchedule(event) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                        ) {
                            Text(
                                "LÊN LỊCH",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, VNRed.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VNRed),
                        ) {
                            Text("Đóng", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(VNRed, VNRed.copy(alpha = 0.6f), VNRed),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun InfoBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    minHeightValue: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VNRed.copy(alpha = 0.05f))
            .border(1.dp, VNRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VNRed),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = SlateGray500,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = SlateGray900,
                lineHeight = 24.sp,
                modifier = Modifier.heightIn(min = minHeightValue),
            )
        }
    }
}

private fun formatEventDateRange(start: String, end: String): String {
    return try {
        val fmtIn = DateTimeFormatter.ISO_LOCAL_DATE
        val d1 = LocalDate.parse(start.take(10), fmtIn)
        val d2 = LocalDate.parse(end.take(10), fmtIn)
        val fmtOut = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi"))
        "${d1.format(fmtOut)} – ${d2.format(fmtOut)}"
    } catch (_: Exception) {
        "$start – $end"
    }
}
