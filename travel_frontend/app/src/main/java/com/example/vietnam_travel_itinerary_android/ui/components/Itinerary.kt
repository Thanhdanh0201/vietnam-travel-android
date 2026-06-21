package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.itinerary.Participant
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

import androidx.compose.foundation.clickable

@Composable
fun ItineraryCard(
    itinerary: Itinerary,
    participants: List<Participant> = emptyList(),
    canDelete: Boolean = true,
    onClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(itinerary.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Ảnh bìa & Tag trạng thái
            Box(modifier = Modifier.height(160.dp)) {
                if (!itinerary.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = itinerary.coverUrl,
                        contentDescription = itinerary.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = itinerary.imageResId),
                        contentDescription = itinerary.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (canDelete) {
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }

                // Tag trạng thái (Sắp diễn ra, Đã kết thúc...)
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = if (itinerary.isUpcoming) Color(0xFFD32F2F) else Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = itinerary.statusText,
                        color = if (itinerary.isUpcoming) Color.White else Color.DarkGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nội dung Card
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = itinerary.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (itinerary.statusSubText != null) {
                        Text(
                            text = itinerary.statusSubText,
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Badges for isPublic and status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (itinerary.isPublic) "Công khai" else "Riêng tư",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (itinerary.isPublic) Color(0xFFD32F2F) else Color.Gray,
                        modifier = Modifier
                            .background(
                                color = (if (itinerary.isPublic) Color(0xFFD32F2F) else Color.Gray).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    val statusText = when (itinerary.status) {
                        "completed" -> "Hoàn thành"
                        else -> "Bản nháp"
                    }
                    val statusColor = when (itinerary.status) {
                        "completed" -> Color(0xFF10B981)
                        else -> Color.Gray
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier
                            .background(
                                color = statusColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = itinerary.location, fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = itinerary.dateRange, fontSize = 13.sp, color = Color.Gray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onShareClick(itinerary.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Chia sẻ",
                                modifier = Modifier.size(14.dp),
                                tint = VNRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (itinerary.shareCount > 0) itinerary.shareCount.toString() else "Chia sẻ",
                                fontSize = 13.sp,
                                color = VNRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Hiển thị Avatar những người tham gia thực tế
                    Row(
                        modifier = Modifier.offset(x = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        participants.filter { it.isAccepted }.take(3).forEachIndexed { index, participant ->
                            Box(
                                modifier = Modifier
                                    .offset(x = (-8 * index).dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                            ) {
                                AuthorAvatar(
                                    initials = participant.initials,
                                    color = Color(participant.avatarColor),
                                    size = 24
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}