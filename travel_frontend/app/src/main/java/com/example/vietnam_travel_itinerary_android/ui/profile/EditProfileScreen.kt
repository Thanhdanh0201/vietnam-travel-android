package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        AvatarImageUtils.uriToAvatarBytes(context, uri)
                    }
                    viewModel.onAvatarPicked(bytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.onAvatarPickError(e.message ?: "Không đọc được ảnh")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentProfile()
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.consumeSaveSuccess()
            onSaved()
        }
    }

    Scaffold(
        topBar = { AppBackTopBar(onBackClick = onBack) },
        containerColor = Color(0xFFF8F6F6),
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = VNRed)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Chỉnh sửa trang cá nhân",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    EditableProfileAvatar(
                        avatarUrl = uiState.avatarUrl,
                        initials = uiState.avatarInitials,
                        color = Color(uiState.avatarColor),
                        isUploading = uiState.isUploadingAvatar,
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )

                    Text(
                        text = "Chạm để đổi ảnh đại diện",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                    )

                    ProfileField(
                        label = "Tên hiển thị",
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        placeholder = "Tên của bạn",
                    )

                    ProfileField(
                        label = "Username",
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        placeholder = "danhnguyenhoctap12",
                        prefix = "@",
                        supportingText = "Chỉ dùng chữ thường, số, dấu chấm hoặc gạch dưới (3–30 ký tự)",
                    )

                    ProfileField(
                        label = "Giới thiệu",
                        value = uiState.bio,
                        onValueChange = viewModel::onBioChange,
                        placeholder = "Viết vài dòng về bạn...",
                        singleLine = false,
                        minLines = 3,
                    )

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Button(
                        onClick = viewModel::saveProfile,
                        enabled = !uiState.isSaving && !uiState.isUploadingAvatar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Lưu thay đổi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    prefix: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF334155),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color(0xFF94A3B8)) },
            prefix = prefix?.let { { Text(it, color = Color(0xFF64748B)) } },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = VNRed,
                cursorColor = VNRed,
            ),
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
            )
        }
    }
}
