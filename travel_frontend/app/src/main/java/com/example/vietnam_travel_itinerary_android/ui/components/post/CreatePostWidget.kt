package com.example.vietnam_travel_itinerary_android.ui.components.post

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.ui.theme.*
import com.example.vietnam_travel_itinerary_android.data.model.LinkedItinerary
import com.example.vietnam_travel_itinerary_android.data.model.PostPlace
import com.example.vietnam_travel_itinerary_android.ui.components.itinerary.ItineraryCompactCard
import androidx.compose.material.icons.outlined.Close
import coil3.compose.AsyncImage

// ============================================================
// CREATE POST WIDGET — dùng chung ở Community + Profile
// ============================================================

@Composable
fun CreatePostWidget(
    avatarInitials: String = "U",
    avatarColor: Long = 0xFFC6102E,
    avatarUrl: String = "",
    text: String = "",
    onTextChange: (String) -> Unit = {},
    linkedItinerary: LinkedItinerary? = null,
    onUnlinkClick: () -> Unit = {},
    selectedImages: List<Uri> = emptyList(),
    onImageClick: () -> Unit = {},
    onRemoveImage: (Int) -> Unit = {},
    selectedPlace: PostPlace? = null,
    onPlaceClick: () -> Unit = {},
    onRemovePlace: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onPost: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                AuthorAvatar(
                    initials = avatarInitials,
                    color = Color(avatarColor),
                    avatarUrl = avatarUrl,
                    size = 40,
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    // ── Text input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        if (text.isEmpty()) {
                            Text("Có gì mới?", color = Color(0xFF6B7280), fontSize = 14.sp)
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 14.sp, color = SlateGray900),
                            cursorBrush = SolidColor(VNRed),
                            maxLines = 5
                        )
                    }

                    // ── Selected images preview
                    if (selectedImages.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedImages.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Ảnh ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Remove button
                                    IconButton(
                                        onClick = { onRemoveImage(index) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Xóa ảnh",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            // Add more button (if under limit)
                            if (selectedImages.size < 4) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateGray50)
                                        .border(1.dp, SlateGray200, RoundedCornerShape(8.dp))
                                        .clickable(onClick = onImageClick),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Add,
                                        contentDescription = "Thêm ảnh",
                                        tint = SlateGray400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Selected place chip
                    if (selectedPlace != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = VNRed.copy(alpha = 0.06f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Place,
                                    contentDescription = null,
                                    tint = VNRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        selectedPlace.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = VNRed,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (selectedPlace.provinceName.isNotBlank()) {
                                        Text(
                                            selectedPlace.provinceName,
                                            fontSize = 10.sp,
                                            color = SlateGray500
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Bỏ địa điểm",
                                    tint = SlateGray400,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable(onClick = onRemovePlace)
                                )
                            }
                        }
                    }

                    if (linkedItinerary != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            ItineraryCompactCard(
                                itinerary = linkedItinerary,
                                onViewClick = {}
                            )
                            IconButton(
                                onClick = onUnlinkClick,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Hủy đính kèm",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SlateGray50)

                    // ── Bottom actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(
                                Icons.Outlined.Image, "Ảnh",
                                tint = if (selectedImages.isNotEmpty()) VNRed else SlateGray400,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(onClick = onImageClick)
                            )
                            Icon(
                                Icons.Outlined.Place, "Địa điểm",
                                tint = if (selectedPlace != null) VNRed else SlateGray400,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(onClick = onPlaceClick)
                            )
                            Icon(
                                Icons.Outlined.Map, "Bản đồ",
                                tint = if (selectedPlace != null) VNRed else SlateGray400,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(onClick = onMapClick)
                            )
                        }
                        Button(
                            onClick = onPost,
                            shape = CircleShape,
                            enabled = text.isNotBlank() || linkedItinerary != null || selectedImages.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VNRed,
                                disabledContainerColor = SlateGray200
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                "ĐĂNG", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
