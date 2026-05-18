package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItineraryScreen(
    itinerary: Itinerary? = null,
    viewModel: ItineraryViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val timelineItems = uiState.timelineMap[itinerary?.id] ?: emptyList()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F6F6),
        topBar = {
            Surface(
                color = Color(0xFFF8F6F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = VNRed)
                        }
                        Text(
                            text = itinerary?.title ?: "Kỳ nghỉ của tôi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = VNRed
                        )
                    }
                    AuthorAvatar(initials = "P", color = Color(0xFFF59E0B), size = 36)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Tháng & Lịch
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(VNRed)
                            )
                            Text(
                                text = "Tháng 12, 2024",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = SlateGray900
                            )
                        }
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Lịch", tint = VNRed)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val days = listOf(
                        Pair("TH 2", "12"), Pair("TH 3", "13"), Pair("TH 4", "14"),
                        Pair("TH 5", "15"), Pair("TH 6", "16"), Pair("TH 7", "17")
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(days) { (day, date) ->
                            val isSelected = day == "TH 2" && date == "12"
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) VNRed else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else SlateGray200,
                                        shape = CircleShape
                                    )
                                    .clickable { }
                            ) {
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SlateGray500
                                )
                                Text(
                                    text = date,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else SlateGray900
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                HorizontalDivider(color = SlateGray200, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            
            // Người tham gia
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(VNRed)
                            )
                            Text(
                                text = "Người tham gia",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = SlateGray900
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VNRed.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = VNRed)
                                Text("QUYỀN CHỈNH SỬA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VNRed)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatars Overlapping
                        Row {
                            val colors = listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFFF59E0B))
                            val initials = listOf("L", "M", "H")
                            for (i in 0..2) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-8 * i).dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFFF8F6F6), CircleShape)
                                ) {
                                    AuthorAvatar(initials = initials[i], color = colors[i], size = 40)
                                }
                            }
                            
                            // Nút Thêm
                            Box(
                                modifier = Modifier
                                    .offset(x = (-24).dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, VNRed.copy(alpha = 0.5f), CircleShape)
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Thêm người", tint = VNRed, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Column(modifier = Modifier.offset(x = (-16).dp)) {
                            Text("+3 người khác", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateGray900)
                            Text("CÓ THỂ CÙNG SỬA", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SlateGray500)
                        }
                    }
                }
            }
            
            // Timeline Lịch trình
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    timelineItems.forEachIndexed { index, item ->
                        TimelineItem(
                            data = item,
                            isLast = index == timelineItems.size - 1,
                            onDeleteClick = {
                                itinerary?.let { it ->
                                    viewModel.removePlaceFromItinerary(it.id, item)
                                }
                            }
                        )
                    }
                    
                    // Nút Thêm Địa Điểm ở cuối
                    Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                        Box(
                            modifier = Modifier.width(80.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SlateGray300)
                                    .align(Alignment.Center)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .align(Alignment.CenterVertically)
                                .drawBehind {
                                    val dashWidth = 10f
                                    val dashSpace = 10f
                                    drawRoundRect(
                                        color = VNRed.copy(alpha = 0.5f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashSpace), 0f)
                                        ),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                                    )
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(VNRed.copy(alpha = 0.05f))
                                .clickable { showAddDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = VNRed)
                                Text("THÊM ĐỊA ĐIỂM", color = VNRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal / Dialog Thêm địa điểm du lịch (Lọc theo Tỉnh và Quận Huyện)
    if (showAddDialog) {
        val parsed = itinerary?.location?.split(",") ?: emptyList()
        val district = parsed.getOrNull(0)?.trim() ?: ""
        val province = parsed.getOrNull(1)?.trim() ?: ""

        val filteredPlaces = uiState.allPlaces.filter { place ->
            place.provinces?.name?.trim()?.contains(province, ignoreCase = true) == true &&
            place.cities?.name?.trim()?.contains(district, ignoreCase = true) == true
        }.ifEmpty {
            viewModel.getFallbackPlacesFor(province, district)
        }

        var selectedPlace by remember { mutableStateOf<Place?>(filteredPlaces.firstOrNull()) }
        var timeInput by remember { mutableStateOf("08:00 AM") }
        var tagInput by remember { mutableStateOf("Tham quan") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Column {
                    Text(
                        text = "Thêm địa điểm du lịch",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateGray900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Khu vực: $district, $province",
                        fontSize = 12.sp,
                        color = SlateGray500
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Nhập thời gian
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Thời gian", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray600)
                        OutlinedTextField(
                            value = timeInput,
                            onValueChange = { timeInput = it },
                            placeholder = { Text("Ví dụ: 08:30 AM") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Nhập hoạt động
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Hoạt động / Tag", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray600)
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            placeholder = { Text("Ví dụ: Cáp treo, Ẩm thực, Di sản") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Danh sách địa điểm (Chỉ của Tỉnh / Huyện đó)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Chọn địa điểm thuộc vùng này", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray600)
                        if (filteredPlaces.isEmpty()) {
                            Text("Không có địa điểm nào khả dụng cho khu vực này.", color = VNRed, fontSize = 12.sp)
                        } else {
                            filteredPlaces.forEach { place ->
                                val isSelected = selectedPlace?.id == place.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) VNRed.copy(alpha = 0.08f) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) VNRed else SlateGray200,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedPlace = place }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPlace = place },
                                        colors = RadioButtonDefaults.colors(selectedColor = VNRed)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(place.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGray900)
                                        Text(place.type ?: "Địa điểm du lịch", fontSize = 11.sp, color = SlateGray500)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPlace?.let { place ->
                            itinerary?.let { it ->
                                viewModel.addPlaceToItinerary(
                                    itineraryId = it.id,
                                    time = timeInput,
                                    place = place,
                                    tag = tagInput
                                )
                            }
                        }
                        showAddDialog = false
                    },
                    enabled = selectedPlace != null,
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                ) {
                    Text("Thêm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Hủy", color = SlateGray500)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

data class TimelineItemData(
    val time: String,
    val title: String,
    val location: String,
    val tag: String,
    val imageUrl: String
)

@Composable
fun TimelineItem(data: TimelineItemData, isLast: Boolean, onDeleteClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Cột bên trái: Thời gian + Dấu chấm + Đường kẻ
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(80.dp)
        ) {
            Text(
                text = data.time,
                color = VNRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(VNRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(VNRed)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(SlateGray200)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(Color.Transparent)
                )
            }
        }
        
        // Cột bên phải: Card hiển thị thông tin
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, SlateGray200.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.height(100.dp)) {
                // Hình ảnh
                AsyncImage(
                    model = data.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                )
                
                // Chi tiết
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = data.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateGray900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = data.location,
                                fontSize = 11.sp,
                                color = SlateGray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray400)
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Xóa",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteClick() },
                                tint = SlateGray400
                            )
                            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = VNRed)
                        }
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VNRed.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = data.tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VNRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
