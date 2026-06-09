package com.example.vietnam_travel_itinerary_android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityScreen
import com.example.vietnam_travel_itinerary_android.ui.components.BottomNavBar
import com.example.vietnam_travel_itinerary_android.ui.components.bottomNavItems
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.CreateItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.EditItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileScreen

private val mainTabRoutes: Set<String> by lazy {
    bottomNavItems.map { it.route }.toSet()
}

@Composable
fun MainScreen(
    itineraryViewModel: ItineraryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bottomNavController = rememberNavController()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute =
        bottomNavItems.firstOrNull { item ->
            val destRoute = currentDestination?.route ?: ""
            destRoute.substringBefore("?") == item.route
        }?.route
            ?: ""

    val uiState by itineraryViewModel.uiState.collectAsState()
    val itinerariesState = uiState.itineraries

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

    val showBottomBar = currentDestination?.route?.let { route ->
        route.substringBefore("?") in mainTabRoutes
    } ?: false

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
                        if (route == "community") {
                            bottomNavController.navigate("community") {
                                popUpTo("community?shareItineraryId={shareItineraryId}") {
                                    inclusive = true
                                }
                            }
                        } else if (route in mainTabRoutes) {
                            navigateToMainTab(route)
                        } else {
                            bottomNavController.navigate(route)
                        }
                    }
                )
            }

            composable(
                route = "community?shareItineraryId={shareItineraryId}",
                arguments = listOf(
                    androidx.navigation.navArgument("shareItineraryId") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val shareItineraryId = backStackEntry.arguments?.getString("shareItineraryId")
                CommunityScreen(
                    shareItineraryId = shareItineraryId,
                    itineraryViewModel = itineraryViewModel,
                    onNavigate = { route ->
                        if (route == "community") {
                            bottomNavController.navigate("community") {
                                popUpTo("community?shareItineraryId={shareItineraryId}") {
                                    inclusive = true
                                }
                            }
                        } else if (route in mainTabRoutes) {
                            navigateToMainTab(route)
                        } else {
                            bottomNavController.navigate(route)
                        }
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
                    viewModel = itineraryViewModel,
                    onCreateClick = { bottomNavController.navigate("create_itinerary") },
                    onEditClick = { itineraryId -> bottomNavController.navigate("edit_itinerary/$itineraryId") },
                    onShareClick = { itineraryId ->
                        bottomNavController.navigate("community?shareItineraryId=$itineraryId")
                    }
                )
            }

            composable("create_itinerary") {
                CreateItineraryScreen(
                    viewModel = itineraryViewModel,
                    onBackClick = { bottomNavController.popBackStack() },
                    onCreate = { newItineraryId ->
                        bottomNavController.navigate("edit_itinerary/$newItineraryId") {
                            popUpTo("create_itinerary") { inclusive = true }
                        }
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
                    viewModel = itineraryViewModel,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable(
                route = "itinerary_detail/{itineraryId}",
                arguments = listOf(
                    androidx.navigation.navArgument("itineraryId") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: ""
                LaunchedEffect(itineraryId) {
                    itineraryViewModel.fetchItineraryDetail(itineraryId)
                }
                val itinerary = itinerariesState.find { it.id == itineraryId }
                EditItineraryScreen(
                    itinerary = itinerary,
                    viewModel = itineraryViewModel,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onNavigate = { route ->
                        if (route == "community") {
                            bottomNavController.navigate("community") {
                                popUpTo("community?shareItineraryId={shareItineraryId}") {
                                    inclusive = true
                                }
                            }
                        } else if (route in mainTabRoutes) {
                            navigateToMainTab(route)
                        } else {
                            bottomNavController.navigate(route)
                        }
                    }
                )
            }
        }
    }
}
