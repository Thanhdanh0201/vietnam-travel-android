package com.example.vietnam_travel_itinerary_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigate = { route ->
                    when {
                        route.startsWith("place_detail/") -> {
                            // Future: navController.navigate(route)
                        }
                        route == "search" -> {
                            // Future: navController.navigate("search")
                        }
                        route == "notifications" -> {
                            // Future: navController.navigate("notifications")
                        }
                        route == "home" -> {
                            // Already on home, do nothing
                        }
                        route == "community" -> {
                            // Future: navController.navigate("community")
                        }
                        route == "explore" -> {
                            // Future: navController.navigate("explore")
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

        // Future screen destinations:
        // composable("search") { SearchScreen(...) }
        // composable("place_detail/{placeId}") { PlaceDetailScreen(...) }
        // composable("community") { CommunityScreen(...) }
        // composable("explore") { ExploreScreen(...) }
        // composable("itinerary") { ItineraryScreen(...) }
        // composable("profile") { ProfileScreen(...) }
    }
}
