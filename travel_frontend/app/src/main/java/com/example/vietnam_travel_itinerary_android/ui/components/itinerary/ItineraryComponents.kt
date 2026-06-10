package com.example.vietnam_travel_itinerary_android.ui.components.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.LinkedItinerary
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.components.post.ImagePlaceholderBox
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ============================================================
// SHARED ITINERARY COMPONENTS
//
// 1. ItineraryCompactCard  — Nhúng trong bài viết (PostCard)
//                            Đây là "LinkedItineraryCard" đã tách ra
// 2. ItineraryPublicCard   — Hiển thị đầy đủ trong:
//                            • Profile tab "Lịch trình"
//                            • Explore / Search kết quả
// ============================================================

// ── 1. COMPACT — nhúng trong bài viết
@Composable
fun ItineraryCompactCard(
    itinerary: LinkedItinerary,
    onViewClick: () -> Unit = {}
) {
    val red = VNRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VNRed.copy(alpha = 0.05f))
            .clickable(onClick = onViewClick)
            .drawBehind {
                drawLine(red, Offset(0f, 0f), Offset(0f, size.height), 8.dp.toPx())
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = VNRed,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = itinerary.title.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.6.sp,
                    color = VNRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = buildString {
                    append("Lịch trình chia sẻ • ${itinerary.stopCount} Điểm dừng")
                    if (itinerary.location.isNotBlank()) append(" • ${itinerary.location}")
                }
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateGray500
                )
            }
        }
        OutlinedButton(
            onClick = onViewClick,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.1f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = VNRed
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text("XEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// ── 2. PUBLIC CARD — hiển thị đầy đủ trong Profile tab + Explore
@Composable
fun ItineraryPublicCard(
    itinerary: LinkedItinerary,
    onViewClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.05f))
    ) {
        Column {
            // ── Cover image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                ImagePlaceholderBox(
                    label = itinerary.coverImageKey.ifBlank { itinerary.title },
                    modifier = Modifier.fillMaxSize(),
                    gradient = true
                )

                // Badge công khai / riêng tư
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(if (itinerary.isPublic) VNRed else SlateGray500)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (itinerary.isPublic) "CÔNG KHAI" else "RIÊNG TƯ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White
                    )
                }

                // Duration badge (bottom left)
                if (itinerary.durationDays > 0) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${itinerary.durationDays} ngày",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Card body
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    text = itinerary.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SlateGray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Meta row: Location + Stops
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (itinerary.location.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn, null,
                                tint = SlateGray400, modifier = Modifier.size(12.dp)
                            )
                            Text(
                                itinerary.location, fontSize = 12.sp,
                                color = SlateGray500, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Place, null,
                            tint = SlateGray400, modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "${itinerary.stopCount} điểm dừng",
                            fontSize = 12.sp, color = SlateGray500
                        )
                    }
                }

                HorizontalDivider(color = SlateGray50)

                // Bottom: author + like + view button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Author
                    if (itinerary.authorName.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuthorAvatar(
                                initials = itinerary.authorAvatarInitials,
                                color = Color(itinerary.authorAvatarColor),
                                size = 20
                            )
                            Text(
                                itinerary.authorName,
                                fontSize = 11.sp,
                                color = SlateGray500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like count
                        if (itinerary.likeCount > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Favorite, null,
                                    tint = VNRed, modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    itinerary.likeCount.toString(),
                                    fontSize = 11.sp, color = SlateGray500
                                )
                            }
                        }
                        // View button
                        TextButton(
                            onClick = onViewClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = VNRed),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Xem chi tiết",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
