package com.example.vietnam_travel_itinerary_android.ui.suggestion

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray400
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

private data class TypeOption(val value: String, val label: String)

private val placeTypeOptions = listOf(
    TypeOption("historical", "Di tích lịch sử"),
    TypeOption("natural", "Thiên nhiên"),
    TypeOption("cultural", "Văn hóa"),
    TypeOption("entertainment", "Giải trí"),
    TypeOption("shopping", "Mua sắm"),
    TypeOption("food", "Ẩm thực"),
    TypeOption("resort", "Nghỉ dưỡng"),
    TypeOption("heritage", "Di sản thế giới"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitPlaceSuggestionScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: PlaceSuggestionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProvinceId by remember { mutableStateOf<String?>(null) }
    var selectedProvinceName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<TypeOption?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imagePreviewUri by remember { mutableStateOf<android.net.Uri?>(null) }

    LaunchedEffect(Unit) { viewModel.loadProvinces() }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            Toast.makeText(context, "Đã gửi đề xuất. Cảm ơn bạn!", Toast.LENGTH_SHORT).show()
            viewModel.consumeSubmitSuccess()
            onSubmitted()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            imagePreviewUri = it
            val stream = context.contentResolver.openInputStream(it)
            imageBytes = stream?.readBytes()
            stream?.close()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Đề xuất địa điểm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = VNRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ImagePickerBox(
                previewUri = imagePreviewUri,
                onClick = { imagePicker.launch("image/*") },
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên địa điểm *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            ProvinceDropdown(
                provinces = uiState.provinces,
                selectedName = selectedProvinceName,
                onSelect = { province ->
                    selectedProvinceId = province.id
                    selectedProvinceName = province.name
                },
            )

            TypeDropdown(
                selected = selectedType,
                onSelect = { selectedType = it },
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                "Để được duyệt thành điểm đến chính thức, đề xuất cần có tỉnh và loại địa điểm.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateGray500,
            )

            Button(
                onClick = {
                    viewModel.submit(
                        name = name,
                        provinceId = selectedProvinceId,
                        type = selectedType?.value,
                        description = description,
                        imageBytes = imageBytes,
                    )
                },
                enabled = !uiState.isSubmitting && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Gửi đề xuất", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ImagePickerBox(previewUri: android.net.Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (previewUri != null) {
            AsyncImage(
                model = previewUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = SlateGray400, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(4.dp))
                Text("Thêm ảnh", color = SlateGray500, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinceDropdown(
    provinces: List<com.example.vietnam_travel_itinerary_android.data.model.Province>,
    selectedName: String,
    onSelect: (com.example.vietnam_travel_itinerary_android.data.model.Province) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tỉnh / Thành phố") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            provinces.forEach { province ->
                DropdownMenuItem(
                    text = { Text(province.name) },
                    onClick = {
                        onSelect(province)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeDropdown(
    selected: TypeOption?,
    onSelect: (TypeOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Loại địa điểm") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            placeTypeOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
