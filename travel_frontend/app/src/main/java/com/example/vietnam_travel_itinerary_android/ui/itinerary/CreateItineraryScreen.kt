package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Province
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItineraryScreen(
    viewModel: ItineraryViewModel,
    onBackClick: () -> Unit = {},
    onCreate: (String) -> Unit = {}
) {
    // Tên chuyến đi
    var tripName by remember { mutableStateOf("") }

    // Khoảng ngày được chọn thực tế
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Upload ảnh bìa lên Supabase
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
        uri?.let {
            isUploadingImage = true
            uploadError = null
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
                            uploadedImageUrl = url
                            isUploadingImage = false
                        },
                        onFailure = { errorMsg ->
                            uploadError = errorMsg
                            isUploadingImage = false
                        }
                    )
                } else {
                    isUploadingImage = false
                    uploadError = "Không thể đọc dữ liệu ảnh"
                }
            } catch (e: Exception) {
                isUploadingImage = false
                uploadError = "Lỗi: ${e.message}"
            }
        }
    }

    // Định dạng ngày hiển thị
    val dateRangeStr = remember(startDateMillis, endDateMillis) {
        if (startDateMillis != null && endDateMillis != null) {
            val start = Instant.ofEpochMilli(startDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(endDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
            val dmyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dmFormatter = DateTimeFormatter.ofPattern("dd/MM")
            if (start.year == end.year) {
                "${start.format(dmFormatter)} - ${end.format(dmyFormatter)}"
            } else {
                "${start.format(dmyFormatter)} - ${end.format(dmyFormatter)}"
            }
        } else {
            "Sắp xếp sau"
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val provinces = uiState.provinces
    var selectedProvinceName by remember { mutableStateOf("") }
    var selectedProvinceCode by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }


    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                startDateMillis = start
                endDateMillis = end
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F6F6),
        topBar = { AppBackTopBar(onBackClick = onBackClick) },
        bottomBar = {
            Surface(
                color = Color(0xFFF8F6F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        isSaving = true
                        viewModel.addItinerary(
                            itinerary = Itinerary(
                                id = "",
                                title = tripName.ifBlank { "Lịch trình mới" },
                                location = selectedProvinceName,
                                dateRange = dateRangeStr,
                                statusText = "SẮP DIỄN RA",
                                statusSubText = "🕒 Mới tạo",
                                isUpcoming = true,
                                imageResId = android.R.drawable.ic_menu_gallery,
                                participantImages = emptyList()
                            ),
                            coverUrl = uploadedImageUrl,
                            onSuccess = { newId ->
                                isSaving = false
                                onCreate(newId)
                            },
                            onFailure = {
                                isSaving = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .navigationBarsPadding(),
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("BẮT ĐẦU LÊN LỊCH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
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

            // Nơi dự định đi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NƠI DỰ ĐỊNH ĐI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SlateGray500
                )
                ProvinceSelectorDropdown(
                    provinces = provinces,
                    selectedName = selectedProvinceName,
                    onSelect = { province ->
                        selectedProvinceName = province.name
                        selectedProvinceCode = province.code
                    }
                )
            }

            // Chọn thời gian chuyến đi thực tế
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = dateRangeStr,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (startDateMillis != null) SlateGray900 else SlateGray400
                            )
                            if (startDateMillis != null && endDateMillis != null) {
                                val start = Instant.ofEpochMilli(startDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
                                val end = Instant.ofEpochMilli(endDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
                                val daysCount = ChronoUnit.DAYS.between(start, end) + 1
                                Text(
                                    text = "Chuyến đi kéo dài $daysCount ngày",
                                    fontSize = 12.sp,
                                    color = VNRed,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Bấm để chọn khoảng ngày đi",
                                    fontSize = 12.sp,
                                    color = SlateGray400
                                )
                            }
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Date", tint = VNRed)
                        }
                    }
                }
            }

            // Chọn & Tải ảnh bìa (Supabase storage)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ẢNH BÌA LỊCH TRÌNH (Tải lên Supabase)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SlateGray500
                )
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable(enabled = !isUploadingImage) { imagePickerLauncher.launch("image/*") }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingImage) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = VNRed, modifier = Modifier.size(32.dp))
                                Text("Đang tải ảnh lên...", fontSize = 12.sp, color = SlateGray500)
                            }
                        } else if (uploadedImageUrl != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = uploadedImageUrl,
                                    contentDescription = "Cover Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clickable { imagePickerLauncher.launch("image/*") }
                                ) {
                                    Text(
                                        text = "Thay đổi ảnh",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Add, contentDescription = "Chọn ảnh", tint = SlateGray400, modifier = Modifier.size(28.dp))
                                Text("Chọn ảnh bìa từ điện thoại của bạn", fontSize = 12.sp, color = SlateGray400)
                                uploadError?.let {
                                    Text(it, color = VNRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // padding for bottom bar
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long?, Long?) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis)
                    onDismiss()
                }
            ) {
                Text("Chọn", color = VNRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = SlateGray500)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = Color.White)
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.weight(1f),
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = VNRed,
                todayContentColor = VNRed,
                selectedDayContentColor = Color.White,
                containerColor = Color.White
            ),
            title = {
                Text(
                    text = "Chọn thời gian chuyến đi",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateGray500
                )
            },
            headline = {
                val startText = dateRangePickerState.selectedStartDateMillis?.let {
                    java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US))
                } ?: "Bắt đầu"
                val endText = dateRangePickerState.selectedEndDateMillis?.let {
                    java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US))
                } ?: "Kết thúc"
                Text(
                    text = "$startText - $endText",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateGray900,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinceSelectorDropdown(
    provinces: List<Province>,
    selectedName: String,
    onSelect: (Province) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedName, expanded) {
        if (!expanded) {
            searchQuery = selectedName
        }
    }

    val filteredProvinces = remember(searchQuery, provinces) {
        if (searchQuery.isBlank() || searchQuery == selectedName) {
            provinces
        } else {
            val queryNormalized = searchQuery.removeDiacritics()
            provinces.filter { it.name.removeDiacritics().contains(queryNormalized, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true
            },
            placeholder = { Text("Nhập để tìm kiếm tỉnh / thành", color = SlateGray400, fontSize = 14.sp) },
            label = { Text("Tỉnh/Thành phố", fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VNRed,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp),
        ) {
            if (filteredProvinces.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Không tìm thấy kết quả", color = SlateGray400) },
                    onClick = {},
                    enabled = false
                )
            } else {
                filteredProvinces.forEach { province ->
                    DropdownMenuItem(
                        text = { Text(province.name) },
                        onClick = {
                            onSelect(province)
                            searchQuery = province.name
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

private fun String.removeDiacritics(): String {
    val temp = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(temp).replaceAll("")
        .replace('đ', 'd')
        .replace('Đ', 'D')
}

