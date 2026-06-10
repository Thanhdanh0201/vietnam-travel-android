package com.example.vietnam_travel_itinerary_android.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.theme.*
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// ============================================================
// PLACE PICKER BOTTOM SHEET — Tìm kiếm & chọn địa điểm
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerBottomSheet(
    searchResults: List<Place>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onSelect: (Place) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SlateGray200)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // ── Title
            Text(
                "Chọn địa điểm",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SlateGray900
            )
            Spacer(Modifier.height(16.dp))

            // ── Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Tìm kiếm",
                        tint = SlateGray400,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Tìm kiếm địa điểm...",
                                color = SlateGray400,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                if (query.length >= 2) {
                                    onSearch(query)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = SlateGray900
                            ),
                            cursorBrush = SolidColor(VNRed),
                            singleLine = true
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Xóa",
                            tint = SlateGray400,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    searchQuery = ""
                                    onSearch("")
                                }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Results
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = VNRed,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else if (searchResults.isEmpty() && searchQuery.length >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.SearchOff,
                            contentDescription = null,
                            tint = SlateGray300,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Không tìm thấy địa điểm",
                            fontSize = 14.sp,
                            color = SlateGray400
                        )
                    }
                }
            } else if (searchQuery.length < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Place,
                            contentDescription = null,
                            tint = SlateGray200,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Nhập ít nhất 2 ký tự để tìm kiếm",
                            fontSize = 14.sp,
                            color = SlateGray400
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(
                        items = searchResults,
                        key = { it.id }
                    ) { place ->
                        PlaceResultItem(
                            place = place,
                            onClick = { onSelect(place) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceResultItem(
    place: Place,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Place image or icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(VNRed.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            if (!place.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val icon = when (place.type?.lowercase()) {
                    "restaurant", "cafe" -> Icons.Outlined.Restaurant
                    "hotel", "resort" -> Icons.Outlined.Hotel
                    "beach" -> Icons.Outlined.BeachAccess
                    "museum" -> Icons.Outlined.Museum
                    "park" -> Icons.Outlined.Park
                    else -> Icons.Outlined.Place
                }
                Icon(
                    icon,
                    contentDescription = null,
                    tint = VNRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Place info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                place.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SlateGray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                place.type?.let { append(it.replaceFirstChar { c -> c.uppercase() }) }
                place.provinces?.name?.let {
                    if (isNotEmpty()) append(" • ")
                    append(it)
                }
            }
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = SlateGray400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Rating badge
        place.rating?.let { rating ->
            if (rating > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        String.format("%.1f", rating),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateGray500
                    )
                }
            }
        }
    }
    HorizontalDivider(color = SlateGray50)
}
