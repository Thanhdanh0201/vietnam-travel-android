package com.example.vietnam_travel_itinerary_android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.ui.auth.LoginScreen
import com.example.vietnam_travel_itinerary_android.ui.auth.RegisterScreen
import com.example.vietnam_travel_itinerary_android.ui.auth.OtpScreen
import com.example.vietnam_travel_itinerary_android.ui.auth.OtpViewModel
import com.example.vietnam_travel_itinerary_android.ui.auth.ForgotPasswordRequestScreen
import com.example.vietnam_travel_itinerary_android.ui.auth.ResetPasswordScreen
import android.net.Uri
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val OTP = "otp/{email}/{name}/{type}"
    const val MAIN = "main"

    // SỬA LẠI HÀM NÀY
    fun otp(email: String, name: String, type: String): String {
        // 1. Mã hóa email (để xử lý ký tự @ hoặc các ký tự lạ)
        val encodedEmail = Uri.encode(email)

        // 2. Đảm bảo name không bị rỗng. Nếu rỗng, URL sẽ thành otp/email//type -> Bị lỗi 2 gạch chéo
        val safeName = if (name.isNotBlank()) name else "Traveler"

        // 3. Mã hóa name (để xử lý khoảng trắng như "Nguyen Van A" thành "Nguyen%20Van%20A")
        val encodedName = Uri.encode(safeName)

        return "otp/$encodedEmail/$encodedName/$type"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it }) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it }) }
    ) {
        // ===== LOGIN SCREEN =====
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    // 2. CHỈNH SỬA: Điều hướng vào MAIN thay vì HOME
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Xóa lịch sử Login để không bấm Back về được
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        // ===== MAIN TABS SCREEN (Gồm Home, Itinerary, Profile...) =====
        // 3. XÓA composable(Routes.HOME) cũ và thay bằng composable(Routes.MAIN)
        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
            )
        }
        // ===== REGISTER SCREEN =====
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToOtp = { email, name ->
                    // 3. Truyền cả name vào đây
                    navController.navigate(Routes.otp(email, name, "REGISTER"))
                }
            )
        }

        // ===== FORGOT PASSWORD REQUEST SCREEN =====
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordRequestScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = { email ->
                    // Trường hợp quên mật khẩu không có name thì để mặc định là "User"
                    navController.navigate(Routes.otp(email, "User", "FORGOT_PASSWORD"))
                }
            )
        }

        // ===== OTP SCREEN =====
        composable(
            route = Routes.OTP,
            arguments = listOf(
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("name") { type = androidx.navigation.NavType.StringType }, // 4. THÊM ARGUMENT NAME
                androidx.navigation.navArgument("type") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: "Traveler" // 5. LẤY NAME RA
            val typeStr = backStackEntry.arguments?.getString("type") ?: "REGISTER"
            val type = if (typeStr == "FORGOT_PASSWORD") OtpViewModel.VerificationType.FORGOT_PASSWORD
            else OtpViewModel.VerificationType.REGISTER

            OtpScreen(
                email = email,
                fullName = name, // 6. TRUYỀN NAME VÀO OTPSCREEN
                type = type,
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToResetPassword = {
                    navController.navigate(Routes.RESET_PASSWORD) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                    }
                }
            )
        }

        // ===== RESET PASSWORD SCREEN =====
        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
    }
}