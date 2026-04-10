package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@Composable
fun ForgotPasswordRequestScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.navigateToOtp) {
        if (uiState.navigateToOtp) {
            onNavigateToOtp(uiState.email)
            viewModel.resetNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F6))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            )
    ) {
        // Decorative Image Clip (Top Left)
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(top = 0.dp, start = 0.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = VNRed.copy(alpha = 0.03f),
                    radius = 30.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(30.dp.toPx(), 30.dp.toPx())
                )
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphism Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 448.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp)),
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Header
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(24.dp)
                                    .background(VNRed, CircleShape)
                            )
                            Text(
                                text = "Xác thực Email",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = "Nhập email của bạn để chúng tôi gửi mã xác thực khôi phục mật khẩu.",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    // Email Form
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EMAIL ADDRESS",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFF475569),
                                letterSpacing = 1.sp
                            )
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                color = Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (uiState.email.isEmpty()) {
                                            Text(
                                                text = "example@lotus.vn",
                                                fontFamily = BeVietnamPro,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color(0xFF475569).copy(alpha = 0.4f)
                                            )
                                        }
                                        BasicTextField(
                                            value = uiState.email,
                                            onValueChange = viewModel::onEmailChange,
                                            textStyle = TextStyle(
                                                fontFamily = BeVietnamPro,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color(0xFF475569)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            if (uiState.emailError != null) {
                                Text(
                                    text = uiState.emailError!!,
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontFamily = BeVietnamPro,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = viewModel::onRequestEmailClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    spotColor = VNRed.copy(alpha = 0.2f)
                                ),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "XÁC THỰC",
                                    fontFamily = BeVietnamPro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Divider & Back to Login
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Divider(color = VNRed.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Quay lại đăng nhập",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = VNRed,
                            modifier = Modifier.clickable { onNavigateBack() }
                        )
                    }
                }
            }
        }

        // Decorative Bottom Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF8F6F6))
                    )
                )
        )

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(32.dp)
                    .background(VNRed.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = VNRed,
                    modifier = Modifier.size(16.dp)
                )
            }
            // Title if needed
        }
    }
}
