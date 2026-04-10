package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToOtp: (String) -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Navigation trigger
    LaunchedEffect(uiState.navigateToOtp) {
        if (uiState.navigateToOtp) {
            onNavigateToOtp(uiState.email)
            viewModel.resetNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            )
    ) {
        // #FDFDFD with very subtle red radial pattern
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDFDFD))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = 1.dp.toPx()
                val spacing = 24.dp.toPx()
                for (x in 0..(size.width / spacing).toInt()) {
                    for (y in 0..(size.height / spacing).toInt()) {
                        drawCircle(
                            color = VNRed.copy(alpha = 0.05f),
                            radius = dotRadius,
                            center = androidx.compose.ui.geometry.Offset(x * spacing, y * spacing)
                        )
                    }
                }
            }
        }

        RegisterForm(
            uiState = uiState,
            onFirstNameChange = viewModel::onFirstNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTermsToggle = viewModel::onTermsAgreeChange,
            onTogglePassword = viewModel::togglePasswordVisibility,
            onToggleConfirmPassword = viewModel::toggleConfirmPasswordVisibility,
            onRegisterClick = viewModel::onRegisterClick,
            onLoginClick = onNavigateToLogin
        )
    }
}

// ============================================================
// REGISTER FORM SCREEN
// ============================================================
@Composable
private fun RegisterForm(
    uiState: RegisterViewModel.RegisterUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsToggle: (Boolean) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable content takes up all available space except for the footer
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Container Card
            Surface(
                modifier = Modifier
                    .widthIn(max = 448.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.05f)
                    ),
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LogoIcon()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "VIETNAM TRAVEL",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF1F2937),
                            letterSpacing = (-0.6).sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cùng khám phá vẻ đẹp Việt Nam",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // Fields
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Name Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            RegisterTextField(
                                value = uiState.firstName,
                                onValueChange = onFirstNameChange,
                                label = "Họ",
                                placeholder = "Nguyễn",
                                modifier = Modifier.weight(1f),
                                error = uiState.firstNameError
                            )
                            RegisterTextField(
                                value = uiState.lastName,
                                onValueChange = onLastNameChange,
                                label = "Tên",
                                placeholder = "Văn A",
                                modifier = Modifier.weight(1f),
                                error = uiState.lastNameError
                            )
                        }

                        RegisterTextField(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            label = "Email",
                            placeholder = "email@example.com",
                            error = uiState.emailError
                        )

                        RegisterTextField(
                            value = uiState.password,
                            onValueChange = onPasswordChange,
                            label = "Mật khẩu",
                            placeholder = "••••••••",
                            isPassword = true,
                            isPasswordVisible = uiState.isPasswordVisible,
                            onToggleVisibility = onTogglePassword,
                            error = uiState.passwordError,
                            helperText = "Tối thiểu 8 ký tự"
                        )

                        RegisterTextField(
                            value = uiState.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = "Xác nhận mật khẩu",
                            placeholder = "••••••••",
                            isPassword = true,
                            isPasswordVisible = uiState.isConfirmPasswordVisible,
                            onToggleVisibility = onToggleConfirmPassword,
                            error = uiState.confirmPasswordError
                        )

                        // Terms
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTermsToggle(!uiState.agreesToTerms) }
                                ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = uiState.agreesToTerms,
                                onCheckedChange = onTermsToggle,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VNRed,
                                    uncheckedColor = Color(0xFFD1D5DB)
                                ),
                                modifier = Modifier.size(24.dp).padding(top = 4.dp)
                            )
                            Text(
                                text = "Tôi đồng ý với các Điều khoản dịch vụ và Chính sách bảo mật",
                                fontFamily = BeVietnamPro,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = if (uiState.termsError != null) Color.Red else Color(0xFF6B7280)
                            )
                        }
                    }

                    // Submit
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Color(0xFFFEE2E2)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "ĐĂNG KÝ",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Fixed Footer at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bạn đã có tài khoản? ",
                    fontFamily = BeVietnamPro,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "ĐĂNG NHẬP",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = VNRed,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

// ============================================================
// SHARED COMPONENTS
// ============================================================

@Composable
private fun LogoIcon() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFFFECACA)
            )
            .background(VNRed, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    error: String? = null,
    helperText: String? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (error != null) Color.Red else Color(0xFF374151)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp)),
            color = Color(0xFFF9FAFB),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp,
                            fontFamily = BeVietnamPro
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (isPassword) {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 10.sp, fontFamily = BeVietnamPro)
        } else if (helperText != null) {
            Text(text = helperText, color = Color(0xFF9CA3AF), fontSize = 10.sp, fontFamily = BeVietnamPro)
        }
    }
}
