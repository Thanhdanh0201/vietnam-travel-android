package com.example.vietnam_travel_itinerary_android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityScreen
import com.example.vietnam_travel_itinerary_android.ui.components.BottomNavBar
import com.example.vietnam_travel_itinerary_android.ui.components.bottomNavItems
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen
import com.example.vietnam_travel_itinerary_android.ui.home.ItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileScreen

private val mainTabRoutes: Set<String> by lazy {
    bottomNavItems.map { it.route }.toSet()
}

@Composable
fun MainScreen() {
    // 1. Khởi tạo một NavController riêng cho các tab
    val bottomNavController = rememberNavController()

    // 2. Route tab đang chọn — không default "home" khi route null (tránh coi nhầm đang ở Trang chủ → bấm Home không navigate)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute =
        bottomNavItems.firstOrNull { item ->
            currentDestination?.hierarchy?.any { it.route == item.route } == true
        }?.route
            ?: currentDestination?.route?.takeIf { it in mainTabRoutes }
            ?: ""

    fun navigateToMainTab(route: String) {
        if (route !in mainTabRoutes) return
        bottomNavController.navigate(route) {
            popUpTo(bottomNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    // Luôn navigate kèm NavOptions (đồng bộ với navigate từ màn con) — tránh kẹt khi so sánh route lệch
                    navigateToMainTab(route)
                }
            )
        }
    ) { innerPadding ->
        // 4. Khung nội dung sẽ thay đổi dựa vào tab được chọn
        NavHost(
            navController = bottomNavController,
            startDestination = "home", // Mặc định mở app lên là vào Home
            modifier = Modifier.padding(innerPadding)
        ) {
            // TAB 1: Trang chủ
            composable("home") {
                HomeScreen(
                    onNavigate = { route ->
                        if (route in mainTabRoutes) navigateToMainTab(route)
                    }
                )
            }

            // TAB 2: Cộng đồng
            composable("community") {
                CommunityScreen(
                    onNavigate = { route ->
                        if (route in mainTabRoutes) navigateToMainTab(route)
                    }
                )
            }

            // NÚT GIỮA: Khám phá
            composable("explore") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Khám phá") }
            }

            // TAB 3: Lịch trình
            composable("itinerary") {
                // Khởi tạo danh sách dữ liệu mẫu (Mock Data)
                val mockItineraries = listOf(
                    Itinerary(
                        id = "1",
                        title = "Kỳ nghỉ Vịnh Hạ Long",
                        location = "Quảng Ninh, Việt Nam",
                        dateRange = "15/10 - 18/10/2024",
                        statusText = "SẮP DIỄN RA",
                        statusSubText = "🕒 Còn 5 ngày nữa",
                        isUpcoming = true,
                        imageResId = android.R.drawable.ic_menu_gallery, // TODO: Thay bằng R.drawable.ten_anh_halong của bạn
                        participantImages = listOf(android.R.drawable.ic_menu_report_image) // TODO: Thay bằng ảnh avatar
                    ),
                    Itinerary(
                        id = "2",
                        title = "Khám phá Hội An",
                        location = "Quảng Nam, Việt Nam",
                        dateRange = "01/09 - 04/09/2024",
                        statusText = "ĐÃ KẾT THÚC",
                        statusSubText = null,
                        isUpcoming = false,
                        imageResId = android.R.drawable.ic_menu_gallery, // TODO: Thay bằng R.drawable.ten_anh_hoian
                        participantImages = listOf(android.R.drawable.ic_menu_report_image)
                    ),
                    Itinerary(
                        id = "3",
                        title = "Hành trình Sapa",
                        location = "Lào Cai, Việt Nam",
                        dateRange = "10/11 - 14/11/2024",
                        statusText = "SẮP DIỄN RA",
                        statusSubText = "🕒 Còn 24 ngày",
                        isUpcoming = true,
                        imageResId = android.R.drawable.ic_menu_gallery, // TODO: Thay bằng R.drawable.ten_anh_sapa
                        participantImages = listOf(android.R.drawable.ic_menu_report_image)
                    )
                )

                // Truyền dữ liệu vào màn hình
                ItineraryScreen(itineraries = mockItineraries)
            }

            // TAB 4: Cá nhân
            composable("profile") {
                ProfileScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onNavigate = { route ->
                        if (route in mainTabRoutes) navigateToMainTab(route)
                    }
                )
            }
        }
    }
}