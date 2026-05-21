package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItineraryScreen(
    onBackClick: () -> Unit = {},
    onCreate: (Itinerary) -> Unit = {}
) {
    // Chọn thời gian (Mock Calendar Interactive)
    var startDay by remember { mutableStateOf<Int?>(16) }
    var endDay by remember { mutableStateOf<Int?>(20) }
    
    // Tên chuyến đi
    var tripName by remember { mutableStateOf("") }
    
    // Thành phố / Tỉnh
    val provinceToDistricts = mapOf(
        "Thừa Thiên Huế" to listOf("TP. Huế", "Hương Thủy", "Hương Trà", "Phú Vang", "Phú Lộc"),
        "Đà Nẵng" to listOf("Hải Châu", "Sơn Trà", "Ngũ Hành Sơn", "Liên Chiểu", "Cẩm Lệ"),
        "Quảng Nam" to listOf("Hội An", "Tam Kỳ", "Điện Bàn", "Đại Lộc", "Duy Xuyên"),
        "Hà Nội" to listOf("Hoàn Kiếm", "Ba Đình", "Tây Hồ", "Đống Đa", "Hai Bà Trưng"),
        "Hồ Chí Minh" to listOf("Quận 1", "Quận 3", "Quận 5", "Quận 10", "Phú Nhuận")
    )
    val provinces = provinceToDistricts.keys.toList()
    var selectedProvince by remember { mutableStateOf(provinces[0]) }
    
    // Quận Huyện
    var districts by remember { mutableStateOf(provinceToDistricts[selectedProvince] ?: listOf()) }
    var selectedDistrict by remember { mutableStateOf(districts.firstOrNull() ?: "") }

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
                            text = "Quay lại Lịch trình",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = VNRed
                        )
                    }
                    AuthorAvatar(initials = "P", color = Color(0xFFF59E0B), size = 36)
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFFF8F6F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val newItinerary = Itinerary(
                            id = System.currentTimeMillis().toString(),
                            title = tripName.ifBlank { "Lịch trình mới" },
                            location = "$selectedDistrict, $selectedProvince",
                            dateRange = if (startDay != null && endDay != null) "$startDay/12 - $endDay/12/2024" else "Sắp xếp sau",
                            statusText = "SẮP DIỄN RA",
                            statusSubText = "🕒 Mới tạo",
                            isUpcoming = true,
                            imageResId = android.R.drawable.ic_menu_gallery,
                            participantImages = listOf(android.R.drawable.ic_menu_report_image)
                        )
                        onCreate(newItinerary)
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .navigationBarsPadding(),
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("BẮT ĐẦU LÊN LỊCH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(VNRed)
                )
                Text(
                    text = "Tạo lịch trình mới",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = SlateGray900
                )
            }

            // Form Tên chuyến đi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TÊN CHUYẾN ĐI CỦA BẠN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SlateGray500
                )
                OutlinedTextField(
                    value = tripName,
                    onValueChange = { tripName = it },
                    placeholder = { Text("Ví dụ: Hành trình di sản miền Trung", color = SlateGray400, fontSize = 14.sp) },
                    trailingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = VNRed, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VNRed,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Chọn thời gian (Mock Calendar Interactive)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CHỌN THỜI GIAN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateGray500
                    )
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = SlateGray500, modifier = Modifier.size(16.dp))
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tháng 12, 2024",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = SlateGray900
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = null, tint = SlateGray400)
                                Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = SlateGray400)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Weekdays header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val daysOfWeek = listOf("T2" to SlateGray900, "T3" to SlateGray900, "T4" to SlateGray900, "T5" to SlateGray900, "T6" to SlateGray900, "T7" to VNRed, "CN" to VNRed)
                            daysOfWeek.forEach { (day, color) ->
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Calendar Grid Mock
                        val days = (25..30).map { it.toString() to SlateGray300 } + 
                                   (1..31).map { it.toString() to SlateGray900 } + 
                                   (1..5).map { it.toString() to SlateGray300 }
                        
                        var dayIndex = 0
                        for (row in 0..4) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (col in 0..6) {
                                    if (dayIndex < days.size) {
                                        val (dayStr, color) = days[dayIndex]
                                        val isCurrentMonth = color == SlateGray900
                                        val dayNum = dayStr.toIntOrNull() ?: 0
                                        
                                        val isSelectedRange = isCurrentMonth && startDay != null && endDay != null && dayNum in startDay!!..endDay!!
                                        val isStart = isCurrentMonth && dayNum == startDay
                                        val isEnd = isCurrentMonth && dayNum == endDay
                                        val isSingleSelection = isStart && isEnd
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .clickable {
                                                    if (isCurrentMonth) {
                                                        if (startDay == null || (startDay != null && endDay != null)) {
                                                            // Start new selection
                                                            startDay = dayNum
                                                            endDay = null
                                                        } else if (dayNum >= startDay!!) {
                                                            endDay = dayNum
                                                        } else {
                                                            startDay = dayNum
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Range background
                                            if (isSelectedRange && !isStart && !isEnd) {
                                                Box(modifier = Modifier.fillMaxSize().background(VNRed.copy(alpha = 0.1f)))
                                            } else if (isStart && endDay != null && !isSingleSelection) {
                                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5f).align(Alignment.CenterEnd).background(VNRed.copy(alpha = 0.1f)))
                                            } else if (isEnd && startDay != null && !isSingleSelection) {
                                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5f).align(Alignment.CenterStart).background(VNRed.copy(alpha = 0.1f)))
                                            }
                                            
                                            // Day circle
                                            val isHighlighted = isStart || isEnd || (isCurrentMonth && startDay != null && endDay == null && dayNum == startDay)
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isHighlighted) VNRed else Color.Transparent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = dayStr,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isHighlighted) Color.White else color
                                                )
                                            }
                                        }
                                        dayIndex++
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Nơi dự định đi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NƠI DỰ ĐỊNH ĐI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SlateGray500
                )
                
                var expandedProvince by remember { mutableStateOf(false) }

                Text(
                    text = "Tỉnh/Thành phố",
                    fontSize = 12.sp,
                    color = SlateGray600
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateGray50.copy(alpha = 0.5f),
                        border = borderStroke(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { expandedProvince = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(selectedProvince, color = SlateGray900, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = SlateGray500)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedProvince,
                        onDismissRequest = { expandedProvince = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                    ) {
                        provinces.forEach { province ->
                            DropdownMenuItem(
                                text = { Text(province) },
                                onClick = {
                                    selectedProvince = province
                                    expandedProvince = false
                                    districts = provinceToDistricts[province] ?: listOf()
                                    selectedDistrict = districts.firstOrNull() ?: ""
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                var expandedDistrict by remember { mutableStateOf(false) }

                Text(
                    text = "Quận/Huyện/Thành phố thuộc tỉnh",
                    fontSize = 12.sp,
                    color = SlateGray600
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateGray50.copy(alpha = 0.5f),
                        border = borderStroke(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { expandedDistrict = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(selectedDistrict, color = SlateGray900, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = SlateGray500)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedDistrict,
                        onDismissRequest = { expandedDistrict = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                    ) {
                        districts.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district) },
                                onClick = {
                                    selectedDistrict = district
                                    expandedDistrict = false
                                }
                            )
                        }
                    }
                }
            }

            // Mời bạn bè tham gia
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MỜI BẠN BÈ THAM GIA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateGray500
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VNRed.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = VNRed)
                            Text("THÊM BẠN BÈ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VNRed)
                        }
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Linh
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AuthorAvatar(initials = "L", color = VNRed, size = 48)
                            Text("Linh", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateGray900)
                        }
                        // User Minh
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AuthorAvatar(initials = "M", color = Color(0xFF3B82F6), size = 48)
                            Text("Minh", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateGray900)
                        }
                        // User Hà
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AuthorAvatar(initials = "H", color = Color(0xFFF59E0B), size = 48)
                            Text("Hà", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateGray900)
                        }
                        // +2 Khác
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(SlateGray100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+2", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGray600)
                            }
                            Text("Khác", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateGray900)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp)) // padding for bottom bar
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
