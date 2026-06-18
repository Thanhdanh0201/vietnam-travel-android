package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.theme.GoldStar
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray700
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRedContainer

@Composable
fun PlaceCard(
    place: Place,
    onPlaceClick: (Place) -> Unit = {},
    onFavoriteClick: (Place) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(180.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onPlaceClick(place) },
            ),
    ) {
        // Image container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = place.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Favorite button
            IconButton(
                onClick = { onFavoriteClick(place) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Yêu thích",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Place name
        Text(
            text = place.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Province/City
        Text(
            text = place.provinces?.name ?: place.cities?.name ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Card nằm ngang dùng riêng cho Search results ──────────────
@Composable
fun PlaceSearchCard(
    place: Place,
    onPlaceClick: (Place) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val typeLabel = when (place.type) {
        "historical"    -> "Lịch sử"
        "natural"       -> "Thiên nhiên"
        "cultural"      -> "Văn hóa"
        "entertainment" -> "Giải trí"
        "food_dining"   -> "Ẩm thực"
        "resort"        -> "Nghỉ dưỡng"
        "shopping"      -> "Mua sắm"
        else            -> null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onPlaceClick(place) }
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {

            // Ảnh bên trái
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin bên phải
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tên địa điểm
                Text(
                    text = place.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SlateGray700,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Vị trí
                val location = listOfNotNull(
                    place.cities?.name,
                    place.provinces?.name
                ).joinToString(", ")
                if (location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Place,
                            contentDescription = null,
                            tint = VNRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = SlateGray500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Loại địa điểm
                if (typeLabel != null) {
                    Box(
                        modifier = Modifier
                            .background(VNRedContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            color = VNRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Mô tả ngắn
                val description = place.description
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = SlateGray500,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                // Rating + Review count
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rating = place.rating
                    if (rating != null && rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = GoldStar,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "%.1f".format(rating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGray700
                            )
                        }
                    }

                    val reviewCount = place.reviewCount
                    if (reviewCount != null && reviewCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Outlined.RateReview,
                                contentDescription = null,
                                tint = SlateGray500,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (reviewCount >= 1000)
                                    "${"%.1f".format(reviewCount / 1000.0)}k đánh giá"
                                else
                                    "$reviewCount đánh giá",
                                fontSize = 12.sp,
                                color = SlateGray500
                            )
                        }
                    }
                }
            }
        }
    }
}
