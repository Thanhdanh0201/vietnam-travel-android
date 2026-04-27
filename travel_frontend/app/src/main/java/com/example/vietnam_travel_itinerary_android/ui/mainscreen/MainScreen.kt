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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityScreen
import com.example.vietnam_travel_itinerary_android.ui.components.BottomNavBar
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen
import com.example.vietnam_travel_itinerary_android.ui.home.ItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileScreen

@Composable
fun MainScreen() {
    // 1. Khởi tạo một NavController riêng cho các tab
    val bottomNavController = rememberNavController()

    // 2. Theo dõi route hiện tại để thanh Nav biết đang ở tab nào (để bôi đỏ icon)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    // 3. Xử lý logic chuyển trang khi click vào icon
                    if (currentRoute != route) {
                        bottomNavController.navigate(route) {
                            // popUpTo giúp không bị chồng quá nhiều màn hình khi bấm qua lại
                            popUpTo(bottomNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true // Tránh mở 2 màn hình giống nhau cùng lúc
                            restoreState = true    // Giữ lại trạng thái (ví dụ đang cuộn dở) khi quay lại
                        }
                    }
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
                        // Nếu trong HomeScreen có nút gì đó muốn chuyển trang, gọi qua đây
                        bottomNavController.navigate(route)
                    }
                )
            }

            // TAB 2: Cộng đồng
            composable("community") {
                CommunityScreen(
                    onNavigate = { route ->
                        bottomNavController.navigate(route)
                    }
                )
            }

            // NÚT GIỮA: Khám phá
            composable("explore") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Khám phá") }
            }

            // TAB 3: Lịch trình
            composable("itinerary") {
                // ĐÃ FIX: Không cần tạo mock data ở đây nữa vì ItineraryViewModel đã lo việc đó!
                ItineraryScreen()
            }

            // TAB 4: Cá nhân
            composable("profile") {
                ProfileScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onNavigate = { route -> bottomNavController.navigate(route) }
                )
            }
        }
    }
}