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
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val OTP = "otp/{email}/{type}"
    const val HOME = "home"

    fun otp(email: String, type: String) = "otp/$email/$type"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it })
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it })
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it })
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it })
        }
    ) {
        // ===== LOGIN SCREEN =====
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        // ===== HOME SCREEN =====
        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { route ->
                    when {
                        route.startsWith("place_detail/") -> {
                            // Future: navController.navigate(route)
                        }
                        route == "search" -> {
                            // Already on search, do nothing
                        }
                        route == "itinerary" -> {
                            // Future: navController.navigate("itinerary")
                        }
                        route == "profile" -> {
                            // Future: navController.navigate("profile")
                        }
                        else -> {
                            // Other future routes
                        }
                    }
                }
            )
        }

        // ===== REGISTER SCREEN =====
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToOtp = { email ->
                    navController.navigate(Routes.otp(email, "REGISTER"))
                }
            )
        }

        // ===== FORGOT PASSWORD REQUEST SCREEN =====
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordRequestScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOtp = { email ->
                    navController.navigate(Routes.otp(email, "FORGOT_PASSWORD"))
                }
            )
        }

        // ===== OTP SCREEN =====
        composable(
            route = Routes.OTP,
            arguments = listOf(
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("type") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val typeStr = backStackEntry.arguments?.getString("type") ?: "REGISTER"
            val type = if (typeStr == "FORGOT_PASSWORD") OtpViewModel.VerificationType.FORGOT_PASSWORD 
                       else OtpViewModel.VerificationType.REGISTER
            
            OtpScreen(
                email = email,
                type = type,
                onBackClick = {
                    navController.popBackStack()
                },
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
