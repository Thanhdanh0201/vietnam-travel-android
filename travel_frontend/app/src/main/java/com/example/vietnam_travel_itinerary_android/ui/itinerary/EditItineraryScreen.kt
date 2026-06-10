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
import androidx.compose.material.icons.filled.Settings
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
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext


fun parseItineraryDays(dateRange: String): List<Pair<String, String>> {
    val defaultDays = listOf(
        Pair("TH 2", "12"), Pair("TH 3", "13"), Pair("TH 4", "14"),
        Pair("TH 5", "15"), Pair("TH 6", "16"), Pair("TH 7", "17")
    )
    if (dateRange == "Sắp xếp sau" || dateRange.isBlank()) return defaultDays
    try {
        val parts = dateRange.split("-")
        if (parts.size != 2) return defaultDays
        val startPart = parts[0].trim()
        val endPart = parts[1].trim()
        
        val endSubParts = endPart.split("/")
        if (endSubParts.size != 3) return defaultDays
        val yearStr = endSubParts[2]
        val endMonthStr = endSubParts[1]
        val endDayStr = endSubParts[0]
        
        val startSubParts = startPart.split("/")
        val startDayStr = startSubParts[0]
        val startMonthStr = startSubParts[1]
        
        val year = yearStr.toIntOrNull() ?: return defaultDays
        val startMonth = startMonthStr.toIntOrNull() ?: return defaultDays
        val startDay = startDayStr.toIntOrNull() ?: return defaultDays
        val endMonth = endMonthStr.toIntOrNull() ?: return defaultDays
        val endDay = endDayStr.toIntOrNull() ?: return defaultDays
        
        val startDate = LocalDate.of(year, startMonth, startDay)
        val endDate = LocalDate.of(year, endMonth, endDay)
        
        if (startDate.isAfter(endDate)) return defaultDays
        
        val daysList = mutableListOf<Pair<String, String>>()
        var current = startDate
        val vnLocale = Locale("vi", "VN")
        
        while (!current.isAfter(endDate)) {
            val dayOfWeek = current.dayOfWeek.getDisplayName(TextStyle.SHORT, vnLocale)
            val label = when (dayOfWeek.lowercase(vnLocale)) {
                "thứ hai", "t2", "mon" -> "TH 2"
                "thứ ba", "t3", "tue" -> "TH 3"
                "thứ tư", "t4", "wed" -> "TH 4"
                "thứ năm", "t5", "thu" -> "TH 5"
                "thứ sáu", "t6", "fri" -> "TH 6"
                "thứ bảy", "t7", "sat" -> "TH 7"
                "chủ nhật", "cn", "sun" -> "CN"
                else -> dayOfWeek.uppercase(vnLocale)
            }
            val dateStr = current.dayOfMonth.toString().padStart(2, '0')
            daysList.add(Pair(label, dateStr))
            current = current.plusDays(1)
        }
        return daysList
    } catch (e: Exception) {
        e.printStackTrace()
        return defaultDays
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItineraryScreen(
    itinerary: Itinerary? = null,
    viewModel: ItineraryViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val days = remember(itinerary?.dateRange) {
        parseItineraryDays(itinerary?.dateRange ?: "")
    }
    
    var selectedDay by remember(days) {
        mutableStateOf(days.firstOrNull()?.second ?: "12")
    }

    val monthYearText = remember(itinerary?.dateRange) {
        if (itinerary?.dateRange == null || itinerary.dateRange == "Sắp xếp sau" || itinerary.dateRange.isBlank()) {
            "Chọn thời gian"
        } else {
            try {
                val parts = itinerary.dateRange.split("-")
                val endPart = parts.getOrNull(1)?.trim() ?: ""
                val endSubParts = endPart.split("/")
                if (endSubParts.size == 3) {
                    val year = endSubParts[2]
                    val month = endSubParts[1].toIntOrNull() ?: 12
                    "Tháng $month, $year"
                } else {
                    "Tháng 12, 2024"
                }
            } catch (e: Exception) {
                "Tháng 12, 2024"
            }
        }
    }

    LaunchedEffect(itinerary?.id) {
        itinerary?.let {
            val parsed = it.location.split(",")
            val province = if (parsed.size >= 2) parsed[1].trim() else it.location.trim()
            viewModel.fetchPlacesForProvince(province)
            viewModel.fetchCollaborators(it.id)
            viewModel.fetchItineraryItems(it.id)
        }
    }

    val timelineItems = uiState.timelineMap["${itinerary?.id}-$selectedDay"] ?: emptyList()
    // participantsMap là reactive từ collectAsState() — sẽ tự recompose khi fetchCollaborators xong
    val participants = uiState.participantsMap[itinerary?.id] ?: emptyList()
    
    // Kiểm tra quyền chỉnh sửa của user hiện tại với itinerary này
    val canModify = remember(itinerary?.id, uiState.itineraries) {
        itinerary?.id?.let { viewModel.canModifyItinerary(it) } ?: true
    }
    val isOwner = itinerary?.myRole == "OWNER"
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSettingsDeleteConfirm by remember { mutableStateOf(false) }

    // Refresh collaborators after dialog closes
    LaunchedEffect(showParticipantsDialog) {
        if (!showParticipantsDialog) {
            itinerary?.id?.let { viewModel.fetchCollaborators(it) }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F6F6),
        topBar = {
            AppBackTopBar(
                onBackClick = onBackClick,
                trailingContent = if (isOwner) {
                    {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = VNRed)
                        }
                    }
                } else {
                    null
                },
            )
        }
    ) { paddingValues ->
        if (itinerary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VNRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(VNRed),
                    )
                    Text(
                        text = itinerary?.title ?: "Kỳ nghỉ của tôi",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = SlateGray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Tháng & Lịch chọn ngày nằm ngang
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
                                text = monthYearText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = SlateGray900
                            )
                        }
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Lịch", tint = VNRed)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(days) { (day, date) ->
                            val isSelected = date == selectedDay
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
                                    .clickable { selectedDay = date }
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

                    if (!itinerary?.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = VNRed.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = itinerary!!.description!!,
                                fontSize = 13.sp,
                                color = SlateGray700,
                                modifier = Modifier.padding(12.dp)
                            )
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
                        
                        // Chỉ OWNER mới được quản lý thành viên
                        if (isOwner) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = VNRed.copy(alpha = 0.1f),
                                modifier = Modifier.clickable { showParticipantsDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = VNRed)
                                    Text("QUẢN LÝ THÀNH VIÊN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VNRed)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hiển thị danh sách Avatar lợp chồng
                        Row {
                            participants.take(4).forEachIndexed { index, participant ->
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-8 * index).dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFFF8F6F6), CircleShape)
                                ) {
                                    AuthorAvatar(
                                        initials = participant.initials,
                                        color = Color(participant.avatarColor),
                                        size = 40
                                    )
                                }
                            }
                            
                            // Nút Thêm người tham gia nhanh - chỉ hiện với OWNER
                            if (isOwner) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = if (participants.isNotEmpty()) (-8 * participants.take(4).size).dp else 0.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, VNRed.copy(alpha = 0.5f), CircleShape)
                                        .clickable { showParticipantsDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Thêm người", tint = VNRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        
                        val remainingCount = participants.size - 4
                        val offsetDp = if (participants.isNotEmpty()) (-8 * participants.take(4).size - 8).dp else 8.dp
                        
                        Column(modifier = Modifier.offset(x = offsetDp)) {
                            if (remainingCount > 0) {
                                Text("+$remainingCount người khác", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateGray900)
                            } else {
                                Text("${participants.size} thành viên", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateGray900)
                            }
                            Text("CÙNG THAM GIA", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SlateGray500)
                        }
                    }
                }
            }
            
            // Timeline Lịch trình cụ thể cho ngày đang chọn
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (timelineItems.isEmpty()) {
                        // Trạng thái trống cho ngày này
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Chưa có địa điểm nào cho ngày $selectedDay", fontSize = 14.sp, color = SlateGray500)
                            Text("Hãy bấm nút bên dưới để thêm địa điểm đầu tiên!", fontSize = 12.sp, color = SlateGray400)
                        }
                    } else {
                        timelineItems.forEachIndexed { index, item ->
                            TimelineItem(
                                data = item,
                                isLast = index == timelineItems.size - 1,
                                canModify = canModify,
                                onDeleteClick = {
                                    itinerary?.let { it ->
                                        viewModel.removePlaceFromItinerary(it.id, selectedDay, item)
                                    }
                                }
                            )
                        }
                    }
                    
                    // Nút Thêm Địa Điểm ở cuối - Chỉ hiện với OWNER/EDIT
                    if (canModify) {
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
                                    Text("THÊM ĐỊA ĐIỂM NGÀY $selectedDay", color = VNRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Modal / Dialog Thêm địa điểm du lịch
    if (showAddDialog) {
        val parsed = itinerary?.location?.split(",") ?: emptyList()
        val (district, province) = if (parsed.size >= 2) {
            Pair(parsed[0].trim(), parsed[1].trim())
        } else {
            Pair("", itinerary?.location?.trim() ?: "")
        }

        val filteredPlaces = uiState.allPlaces.filter { place ->
            val matchProvince = if (province.isNotBlank()) {
                place.provinces?.name?.trim()?.contains(province, ignoreCase = true) == true ||
                province.trim().contains(place.provinces?.name?.trim() ?: "___", ignoreCase = true)
            } else false
            
            val matchDistrict = if (district.isNotBlank()) {
                place.cities?.name?.trim()?.contains(district, ignoreCase = true) == true ||
                district.trim().contains(place.cities?.name?.trim() ?: "___", ignoreCase = true)
            } else false
            
            if (province.isNotBlank() && district.isNotBlank()) {
                matchProvince && matchDistrict
            } else if (province.isNotBlank()) {
                matchProvince
            } else if (district.isNotBlank()) {
                matchDistrict
            } else {
                true
            }
        }.ifEmpty {
            if (province.isNotBlank()) {
                uiState.allPlaces.filter { place ->
                    place.provinces?.name?.trim()?.contains(province, ignoreCase = true) == true ||
                    province.trim().contains(place.provinces?.name?.trim() ?: "___", ignoreCase = true)
                }
            } else emptyList()
        }.ifEmpty {
            uiState.allPlaces
        }.ifEmpty {
            viewModel.getFallbackPlacesFor(province, district)
        }

        var selectedPlace by remember { mutableStateOf<Place?>(filteredPlaces.firstOrNull()) }
        var timeInput by remember { mutableStateOf("08:00 AM") }
        var noteInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Column {
                    Text(
                        text = "Thêm địa điểm Ngày $selectedDay",
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



                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Ghi chú địa điểm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray600)
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("Ví dụ: Nhớ mua vé trước tại quầy...") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }

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
                                    day = selectedDay,
                                    time = timeInput,
                                    place = place,
                                    note = noteInput.ifBlank { null }
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

    // Modal / Dialog Quản lý Người Tham Gia & Phân Quyền
    if (showParticipantsDialog) {
        var newMemberName by remember { mutableStateOf("") }
        var newMemberEmail by remember { mutableStateOf("") }
        var newMemberRole by remember { mutableStateOf(ParticipantRole.VIEW_ONLY) }

        AlertDialog(
            onDismissRequest = { showParticipantsDialog = false },
            title = {
                Text(
                    text = "Quản lý thành viên",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateGray900
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Form thêm thành viên mới
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateGray50),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Thêm thành viên mới",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SlateGray700
                            )
                            
                            OutlinedTextField(
                                value = newMemberName,
                                onValueChange = { newMemberName = it },
                                placeholder = { Text("Tên người tham gia") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = newMemberEmail,
                                onValueChange = { newMemberEmail = it },
                                placeholder = { Text("Email thành viên") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Phân Quyền
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Quyền:", fontSize = 12.sp, color = SlateGray600)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = newMemberRole == ParticipantRole.EDIT,
                                            onClick = { newMemberRole = ParticipantRole.EDIT },
                                            colors = RadioButtonDefaults.colors(selectedColor = VNRed)
                                        )
                                        Text("Chỉnh sửa", fontSize = 12.sp, color = SlateGray700)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = newMemberRole == ParticipantRole.VIEW_ONLY,
                                            onClick = { newMemberRole = ParticipantRole.VIEW_ONLY },
                                            colors = RadioButtonDefaults.colors(selectedColor = VNRed)
                                        )
                                        Text("Chỉ xem", fontSize = 12.sp, color = SlateGray700)
                                    }
                                }
                            }
                            
                            Button(
                                onClick = {
                                    if (newMemberName.isNotBlank() && newMemberEmail.isNotBlank()) {
                                        itinerary?.let { it ->
                                            viewModel.addParticipant(
                                                itineraryId = it.id,
                                                name = newMemberName,
                                                email = newMemberEmail,
                                                role = newMemberRole
                                            )
                                        }
                                        newMemberName = ""
                                        newMemberEmail = ""
                                    }
                                },
                                enabled = newMemberName.isNotBlank() && newMemberEmail.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("THÊM THÀNH VIÊN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    HorizontalDivider(color = SlateGray200)

                    // Danh sách thành viên hiện tại
                    Text(
                        text = "Danh sách thành viên (${participants.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateGray700
                    )
                    
                    if (participants.isEmpty()) {
                        Text("Chưa có thành viên nào tham gia.", fontSize = 12.sp, color = SlateGray500)
                    } else {
                        participants.forEach { participant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SlateGray200.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AuthorAvatar(
                                        initials = participant.initials,
                                        color = Color(participant.avatarColor),
                                        size = 36
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = participant.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SlateGray900
                                        )
                                        Text(
                                            text = participant.email,
                                            fontSize = 10.sp,
                                            color = SlateGray500
                                        )
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isEdit = participant.role == ParticipantRole.EDIT
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isEdit) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
                                        modifier = Modifier.clickable {
                                            itinerary?.let { it ->
                                                viewModel.updateParticipantRole(
                                                    itineraryId = it.id,
                                                    email = participant.email,
                                                    newRole = if (isEdit) ParticipantRole.VIEW_ONLY else ParticipantRole.EDIT
                                                )
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = if (isEdit) "CHỈNH SỬA" else "CHỈ XEM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEdit) Color(0xFF2E7D32) else Color(0xFF455A64),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            itinerary?.let { it ->
                                                viewModel.removeParticipant(it.id, participant.email)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Xóa thành viên",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showParticipantsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                ) {
                    Text("Hoàn tất", color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Settings Configuration Dialog
    if (showSettingsDialog && itinerary != null) {
        var editTitle by remember { mutableStateOf(itinerary.title) }
        var editDesc by remember { mutableStateOf(itinerary.description ?: "") }
        var editIsPublic by remember { mutableStateOf(itinerary.isPublic) }
        var editStatus by remember { mutableStateOf(itinerary.status ?: "draft") }
        var editCoverUrl by remember { mutableStateOf(itinerary.coverUrl ?: "") }
        var isUploadingCover by remember { mutableStateOf(false) }
        var coverUploadError by remember { mutableStateOf<String?>(null) }
        
        val context = LocalContext.current
        val coverPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            uri?.let {
                isUploadingCover = true
                coverUploadError = null
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val fileName = "cover_${System.currentTimeMillis()}.jpg"
                        viewModel.uploadCover(
                            bytes,
                            fileName,
                            onSuccess = { url ->
                                editCoverUrl = url
                                isUploadingCover = false
                            },
                            onFailure = { errorMsg ->
                                coverUploadError = errorMsg
                                isUploadingCover = false
                            }
                        )
                    } else {
                        isUploadingCover = false
                        coverUploadError = "Không thể đọc dữ liệu ảnh"
                    }
                } catch (e: Exception) {
                    isUploadingCover = false
                    coverUploadError = "Lỗi: ${e.message}"
                }
            }
        }
        
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "Cài đặt lịch trình",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateGray900
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Title
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Tên chuyến đi") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VNRed,
                            unfocusedBorderColor = SlateGray300,
                            focusedLabelColor = VNRed,
                            unfocusedLabelColor = SlateGray500
                        )
                    )
                    
                    // Description
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Mô tả") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VNRed,
                            unfocusedBorderColor = SlateGray300,
                            focusedLabelColor = VNRed,
                            unfocusedLabelColor = SlateGray500
                        )
                    )
                    
                    // Public/Private Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Công khai", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGray900)
                            Text("Cho phép mọi người xem lịch trình của bạn", fontSize = 11.sp, color = SlateGray500)
                        }
                        Switch(
                            checked = editIsPublic,
                            onCheckedChange = { editIsPublic = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VNRed)
                        )
                    }

                    // Completed status switch or dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Trạng thái hoàn thành", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGray900)
                            Text("Đánh dấu chuyến đi đã kết thúc/hoàn thành", fontSize = 11.sp, color = SlateGray500)
                        }
                        Switch(
                            checked = editStatus == "completed",
                            onCheckedChange = { isCompleted ->
                                editStatus = if (isCompleted) "completed" else "draft"
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                        )
                    }
                    
                    // Cover Image section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Ảnh bìa lịch trình", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGray900)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateGray100)
                                .clickable { coverPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingCover) {
                                CircularProgressIndicator(color = VNRed, modifier = Modifier.size(24.dp))
                            } else if (editCoverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = editCoverUrl,
                                    contentDescription = "Cover preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("Bấm để chọn ảnh bìa mới", fontSize = 12.sp, color = SlateGray500)
                            }
                        }
                        coverUploadError?.let {
                            Text(it, color = VNRed, fontSize = 11.sp)
                        }
                    }
                    
                    // Delete Button
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showSettingsDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = VNRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("XÓA LỊCH TRÌNH", color = VNRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateItinerary(
                            itineraryId = itinerary.id,
                            title = editTitle,
                            description = editDesc,
                            isPublic = editIsPublic,
                            status = editStatus,
                            coverUrl = editCoverUrl,
                            onSuccess = {
                                showSettingsDialog = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                ) {
                    Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Hủy", color = SlateGray500)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Settings Delete Confirmation
    if (showSettingsDeleteConfirm && itinerary != null) {
        AlertDialog(
            onDismissRequest = { showSettingsDeleteConfirm = false },
            title = {
                Text(
                    text = "Xóa lịch trình",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateGray900
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa lịch trình này? Tất cả các điểm dừng và cộng tác viên sẽ bị xóa và không thể hoàn tác.",
                    fontSize = 14.sp,
                    color = SlateGray600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItinerary(itinerary.id)
                        showSettingsDeleteConfirm = false
                        showSettingsDialog = false
                        onBackClick() // Go back to the itinerary list
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDeleteConfirm = false }) {
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
    val imageUrl: String,
    val id: String = "",
    val day: String = "",
    val note: String? = null,
    val warningType: String? = null,
    val warningValue: Float? = null
)

@Composable
fun TimelineItem(data: TimelineItemData, isLast: Boolean, canModify: Boolean = true, onDeleteClick: () -> Unit = {}) {
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
                            if (!data.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📝 ${data.note}",
                                    fontSize = 11.sp,
                                    color = SlateGray600,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray400)
                            if (canModify) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Xóa",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onDeleteClick() },
                                    tint = SlateGray400
                                )
                            }
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
