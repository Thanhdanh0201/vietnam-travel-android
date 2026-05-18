package com.example.vietnam_travel_itinerary_android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityScreen
import com.example.vietnam_travel_itinerary_android.ui.components.BottomNavBar
import com.example.vietnam_travel_itinerary_android.ui.components.bottomNavItems
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.CreateItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.EditItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileScreen

private val mainTabRoutes: Set<String> by lazy {
    bottomNavItems.map { it.route }.toSet()
}

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute =
        bottomNavItems.firstOrNull { item ->
            currentDestination?.hierarchy?.any { it.route == item.route } == true
        }?.route
            ?: currentDestination?.route?.takeIf { it in mainTabRoutes }
            ?: ""

    var itinerariesState by remember {
        mutableStateOf(
            listOf(
                Itinerary(
                    id = "1",
                    title = "Kỳ nghỉ Vịnh Hạ Long",
                    location = "Quảng Ninh, Việt Nam",
                    dateRange = "15/10 - 18/10/2024",
                    statusText = "SẮP DIỄN RA",
                    statusSubText = "🕒 Còn 5 ngày nữa",
                    isUpcoming = true,
                    imageResId = android.R.drawable.ic_menu_gallery,
                    participantImages = listOf(android.R.drawable.ic_menu_report_image)
                ),
                Itinerary(
                    id = "2",
                    title = "Khám phá Hội An",
                    location = "Quảng Nam, Việt Nam",
                    dateRange = "01/09 - 04/09/2024",
                    statusText = "ĐÃ KẾT THÚC",
                    statusSubText = null,
                    isUpcoming = false,
                    imageResId = android.R.drawable.ic_menu_gallery,
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
                    imageResId = android.R.drawable.ic_menu_gallery,
                    participantImages = listOf(android.R.drawable.ic_menu_report_image)
                )
            )
        )
    }

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

    val showBottomBar = currentDestination?.route in mainTabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { route -> navigateToMainTab(route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigate = { route ->
                        if (route in mainTabRoutes) navigateToMainTab(route)
                    }
                )
            }

            composable("community") {
                CommunityScreen(
                    onNavigate = { route ->
                        if (route in mainTabRoutes) navigateToMainTab(route)
                    }
                )
            }

            composable("explore") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Khám phá")
                }
            }

            composable("itinerary") {
                ItineraryScreen(
                    itineraries = itinerariesState,
                    onCreateClick = { bottomNavController.navigate("create_itinerary") },
                    onEditClick = { itineraryId -> bottomNavController.navigate("edit_itinerary/$itineraryId") }
                )
            }

            composable("create_itinerary") {
                CreateItineraryScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCreate = { newItinerary ->
                        itinerariesState = itinerariesState + newItinerary
                    }
                )
            }

            composable(
                route = "edit_itinerary/{itineraryId}",
                arguments = listOf(
                    androidx.navigation.navArgument("itineraryId") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val itineraryId = backStackEntry.arguments?.getString("itineraryId")
                val itinerary = itinerariesState.find { it.id == itineraryId }
                EditItineraryScreen(
                    itinerary = itinerary,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

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
