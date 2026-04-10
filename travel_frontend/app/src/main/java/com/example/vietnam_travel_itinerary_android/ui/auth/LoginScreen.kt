package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import com.example.vietnam_travel_itinerary_android.R
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ============================================================
// Login Screen - Figma Design Implementation
// Matches: "Đăng nhập - Mặc định (Google only)"
// and:     "Đăng nhập - Lỗi Mật khẩu (Google only)"
// ============================================================

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Navigate on success
    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            viewModel.resetLoginSuccess()
            onNavigateToHome()
        }
    }

    // Background: #F3F4F6
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Main Container: small padding for rounded card edges
        Box(
            modifier = Modifier
                .widthIn(max = 390.dp)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Section - Login Screen Card
            // border-radius: 40px, shadow: 0px 25px 50px -12px rgba(0,0,0,0.25)
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(40.dp),
                color = Color.White,
                shadowElevation = 25.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color(0xFFF3F4F6)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // ===== TOP ILLUSTRATION AREA (height: 224dp) =====
                    TopIllustrationArea()

                    // ===== APP LOGO AND BRAND =====
                    // Position: top 225dp from card, logo has -48dp offset
                    AppLogoAndBrand(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 177.dp)
                    )

                    // ===== FORM SECTION (top: 297dp) =====
                    FormSection(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                        onLoginClick = {
                            focusManager.clearFocus()
                            viewModel.onLoginClick()
                        },
                        onGoogleSignInClick = viewModel::onGoogleSignInClick,
                        onForgotPasswordClick = onNavigateToForgotPassword,
                        focusManager = focusManager,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 340.dp)
                    )

                    // ===== FOOTER MESSAGE =====
                    // Position: bottom of card, bg: rgba(249,250,251,0.5)
                    FooterMessage(
                        onRegisterClick = onNavigateToRegister,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    // ===== LOADING OVERLAY =====
                    AnimatedVisibility(
                        visible = uiState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.7f))
                                .clip(RoundedCornerShape(40.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = VNRed,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Top Illustration Area (height: 224dp)
// Clipped to top corners of the card
// ============================================================

@Composable
private fun TopIllustrationArea(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(224.dp)
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
    ) {
        // Vietnam Illustration - remote URL
        AsyncImage(
            model = "https://raw.githubusercontent.com/Thanhdanh0201/KhaoSat/main/assets/vietnam_illustration.png",
            contentDescription = "Vietnam Landscape",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay - Figma: linear-gradient(0deg, #FFFFFF 0%, rgba(255,255,255,0) 50%, rgba(255,255,255,0) 100%)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White,
                            0.5f to Color.White.copy(alpha = 0f),
                            1.0f to Color.White.copy(alpha = 0f)
                        ),
                        startY = Float.POSITIVE_INFINITY,
                        endY = 0f
                    )
                )
        )
    }
}

// ============================================================
// App Logo and Brand
// Logo: 64x64, border-radius 16dp, bg #C6102E
// Shadow: 0px 10px 15px -3px #FECACA, 0px 4px 6px -4px #FECACA
// Brand text: "VIETNAM TRAVEL", 24sp Bold, #C6102E, letter-spacing -0.6sp
// ============================================================

@Composable
private fun AppLogoAndBrand(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Red Star Logo - 64x64, border-radius 16dp
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 15.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0xFFFECACA),
                    spotColor = Color(0xFFFECACA)
                )
                .background(VNRed, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // White star icon - 40x40
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Vietnam Travel Logo",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // 32dp gap between logo and brand text

        // Brand text: "VIETNAM TRAVEL"
        Text(
            text = "VIETNAM TRAVEL",
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.6).sp,
            color = VNRed,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================
// Form Section
// padding: 24px 32px, gap: 24px
// background: radial-gradient (barely visible, effectively transparent)
// ============================================================

@Composable
private fun FormSection(
    uiState: LoginViewModel.LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Heading: "Đăng Nhập" — 20sp Bold, #1F2937
        Text(
            text = "Đăng Nhập",
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            color = Color(0xFF1F2937)
        )

        // Input Fields Container — gap: 16dp
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email Field
            LoginTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "EMAIL",
                placeholder = "Nhập email của bạn",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError
            )

            // Password Field
            LoginTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "MẬT KHẨU",
                placeholder = "Nhập mật khẩu",
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLoginClick()
                    }
                ),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError
            )

            // "Quên mật khẩu?" link — right aligned, 12sp Medium, #C6102E
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = VNRed,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onForgotPasswordClick
                    )
                )
            }
        }

        // Login Button
        LoginButton(
            onClick = onLoginClick,
            isLoading = uiState.isLoading
        )

        // Social Login Section
        SocialLoginSection(
            onGoogleClick = onGoogleSignInClick
        )
    }
}

// ============================================================
// Login Text Field — Custom field matching Figma specs exactly
//
// Default state:
//   border: 1px solid #E5E7EB, border-radius: 12px
//   padding: 13px 16px, height: 46dp
//   bg: #FFFFFF
//
// Error state:
//   bg: #FEF2F2, border: 2px solid #EF4444
//   height: 48dp (due to 2px border)
//   label color: #DC2626
//   toggle icon tint: #F87171
// ============================================================

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label — 12sp SemiBold, uppercase
        // Default color: #6B7280, Error color: #DC2626
        Text(
            text = label,
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = if (isError) Color(0xFFDC2626) else Color(0xFF6B7280)
        )

        // Input container
        val borderColor = if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB)
        val borderWidth = if (isError) 2.dp else 1.dp
        val containerColor = if (isError) Color(0xFFFEF2F2) else Color.White
        val inputHeight = if (isError) 48.dp else 46.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(inputHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor, RoundedCornerShape(12.dp))
                .then(
                    Modifier.shadow(0.dp) // reset
                )
        ) {
            // Border
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                color = containerColor,
                border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text input area
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Placeholder
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = Color.Black
                            ),
                            visualTransformation = if (isPassword && !isPasswordVisible) {
                                PasswordVisualTransformation()
                            } else {
                                VisualTransformation.None
                            },
                            keyboardOptions = keyboardOptions,
                            keyboardActions = keyboardActions,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Password toggle icon — 20x20
                    if (isPassword) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onTogglePasswordVisibility,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) {
                                    Icons.Outlined.Visibility
                                } else {
                                    Icons.Outlined.VisibilityOff
                                },
                                contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                modifier = Modifier.size(20.dp),
                                // Default: #9CA3AF, Error: #F87171
                                tint = if (isError) Color(0xFFF87171) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }

        // Error message row — icon 14dp + text 11sp Medium #DC2626
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(top = 1.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFDC2626)
                )
                Text(
                    text = errorMessage ?: "",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}

// ============================================================
// Login Button
// height: 52dp, bg: #C6102E, border-radius: 12dp
// shadow: 0px 10px 15px -3px #FECACA, 0px 4px 6px -4px #FECACA
// text: 16sp Bold, #FFFFFF, "Đăng Nhập"
// ============================================================

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFFFECACA),
                spotColor = Color(0xFFFECACA)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VNRed,
            contentColor = Color.White,
            disabledContainerColor = VNRed.copy(alpha = 0.7f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = "Đăng Nhập",
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================
// Social Login Section
// padding-top: 8dp, gap: 16dp
// Divider text: "HOẶC ĐĂNG NHẬP BẰNG" — 12sp Medium, #9CA3AF, letter-spacing 1.2sp
// Google button: border 1px #E5E7EB, border-radius 12dp, height 46dp
// ============================================================

@Composable
private fun SocialLoginSection(
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Divider text: "HOẶC ĐĂNG NHẬP BẰNG"
        Text(
            text = "HOẶC ĐĂNG NHẬP BẰNG",
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 1.2.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )

        // Google Sign-In Button
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, Color(0xFFE5E7EB)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF374151)
            ),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google multi-color G icon — 20x20
                GoogleIcon(modifier = Modifier.size(20.dp))

                // "Google" — 14sp SemiBold, #374151
                Text(
                    text = "Google",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================
// Google Icon (Multicolor G)
// Colors: Blue #4285F4, Green #34A853, Yellow #FBBC05, Red #EA4335
// ============================================================

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val radius = width * 0.45f

        // Blue arc (right + top-right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = -135f,
            useCenter = true,
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(centerX - radius, centerY - radius)
        )

        // Green arc (bottom-right)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 90f,
            sweepAngle = -135f,
            useCenter = true,
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(centerX - radius, centerY - radius)
        )

        // Yellow arc (bottom-left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(centerX - radius, centerY - radius)
        )

        // Red arc (top-left)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(centerX - radius, centerY - radius)
        )

        // White center circle to create "G" shape
        drawCircle(
            color = Color.White,
            radius = radius * 0.55f,
            center = Offset(centerX, centerY)
        )

        // Blue horizontal bar (right side of G)
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(centerX, centerY - radius * 0.15f),
            size = Size(radius, radius * 0.3f)
        )
    }
}

// ============================================================
// Footer Message
// bg: rgba(249, 250, 251, 0.5), padding: 24dp vertical
// text: "Chưa có tài khoản?" 14sp Normal #6B7280
// link: "Đăng ký" 14sp Bold #C6102E
// ============================================================

@Composable
private fun FooterMessage(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Color(0xFFF9FAFB).copy(alpha = 0.5f))
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chưa có tài khoản? ",
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Đăng ký",
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = VNRed,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRegisterClick
                )
            )
        }
    }
}
