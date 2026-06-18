package com.example.vietnam_travel_itinerary_android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vietnam_travel_itinerary_android.ui.admin.AdminPlaceSuggestionsScreen
import com.example.vietnam_travel_itinerary_android.ui.admin.AdminReportsScreen
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityScreen
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityViewModel
import com.example.vietnam_travel_itinerary_android.ui.components.AppNavigationDrawer
import com.example.vietnam_travel_itinerary_android.ui.components.BottomNavBar
import com.example.vietnam_travel_itinerary_android.ui.components.bottomNavItems
import com.example.vietnam_travel_itinerary_android.ui.home.HomeScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.CreateItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.EditItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryScreen
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel
import com.example.vietnam_travel_itinerary_android.ui.profile.EditProfileScreen
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileScreen
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileViewModel
import com.example.vietnam_travel_itinerary_android.ui.suggestion.MyPlaceSuggestionsScreen
import com.example.vietnam_travel_itinerary_android.ui.suggestion.SubmitPlaceSuggestionScreen
import com.example.vietnam_travel_itinerary_android.ui.notification.NotificationScreen
import com.example.vietnam_travel_itinerary_android.ui.notification.NotificationViewModel
import com.example.vietnam_travel_itinerary_android.ui.search.SearchScreen
import com.example.vietnam_travel_itinerary_android.ui.search.SearchViewModel
import com.example.vietnam_travel_itinerary_android.ui.places.AllPlacesScreen
import com.example.vietnam_travel_itinerary_android.ui.places.AllPlacesViewModel
import com.example.vietnam_travel_itinerary_android.ui.events.AllEventsScreen
import com.example.vietnam_travel_itinerary_android.ui.events.AllEventsViewModel
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.Event
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.vietnam_travel_itinerary_android.ui.components.introduction.PlaceIntroductionOverlay
import com.example.vietnam_travel_itinerary_android.ui.components.introduction.FestivalIntroductionOverlay
import com.example.vietnam_travel_itinerary_android.data.session.UserSessionCache
import kotlinx.coroutines.launch

private val mainTabRoutes: Set<String> by lazy {
    bottomNavItems.map { it.route }.toSet()
}

@Composable
fun MainScreen(
    itineraryViewModel: ItineraryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    communityViewModel: CommunityViewModel = viewModel(factory = AppViewModelProvider.Factory),
    notificationViewModel: NotificationViewModel = viewModel(factory = AppViewModelProvider.Factory),
    sessionProfileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val bottomNavController = rememberNavController()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sessionState by sessionProfileViewModel.uiState.collectAsState()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()

    LaunchedEffect(Unit) {
        notificationViewModel.refreshAll()
        sessionProfileViewModel.loadProfile()
    }

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        notificationViewModel.loadUnreadCount()
    }
    val currentDestination = navBackStackEntry?.destination
    val currentRoute =
        bottomNavItems.firstOrNull { item ->
            val destRoute = currentDestination?.route ?: ""
            destRoute.substringBefore("?") == item.route
        }?.route
            ?: ""

    val uiState by itineraryViewModel.uiState.collectAsState()
    val itinerariesState = uiState.itineraries
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    fun navigateToMainTab(route: String) {
        if (route !in mainTabRoutes) return
        if (route == "itinerary") {
            itineraryViewModel.fetchItineraries()
        }
        bottomNavController.navigate(route) {
            popUpTo(bottomNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun popToMainTabRoot() {
        val startId = bottomNavController.graph.findStartDestination().id
        var safety = 0
        while (safety++ < 20) {
            val current = bottomNavController.currentBackStackEntry
                ?.destination
                ?.route
                ?.substringBefore("?")
            if (current == null || current in mainTabRoutes) break
            if (!bottomNavController.popBackStack()) break
        }
        if (bottomNavController.currentBackStackEntry == null) {
            bottomNavController.navigate("home") {
                popUpTo(startId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun navigateToProfile(userId: String) {
        bottomNavController.navigate("profile/$userId")
    }

    val isAdmin = (sessionState.profile?.role ?: UserSessionCache.get()?.role) == "admin"

    fun navigateFromDrawer(route: String) {
        scope.launch { drawerState.close() }
        if (route.startsWith("admin_") && !isAdmin) {
            popToMainTabRoot()
            navigateToMainTab("home")
            return
        }
        if (route in mainTabRoutes) {
            popToMainTabRoot()
            navigateToMainTab(route)
        } else {
            bottomNavController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    val showBottomBar = currentDestination?.route?.let { route ->
        route.substringBefore("?") in mainTabRoutes
    } ?: false

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawer(
                profile = sessionState.profile ?: UserSessionCache.get(),
                currentRoute = currentDestination?.route?.substringBefore("?") ?: "home",
                onDestination = { route -> navigateFromDrawer(route) },
            )
        },
    ) {
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
                    unreadCount = unreadCount,
                    onMenuClick = openDrawer,
                    onNavigate = { route ->
                        if (route in mainTabRoutes) {
                            navigateToMainTab(route)
                        } else {
                            bottomNavController.navigate(route)
                        }
                    }
                )
            }

            composable("community") {
                CommunityScreen(
                    itineraryViewModel = itineraryViewModel,
                    viewModel = communityViewModel,
                    unreadCount = unreadCount,
                    onMenuClick = openDrawer,
                    onNavigate = { route ->
                        when {
                            route.startsWith("profile/") -> navigateToProfile(route.removePrefix("profile/"))
                            route == "profile" -> navigateToMainTab("profile")
                            route in mainTabRoutes -> navigateToMainTab(route)
                            else -> bottomNavController.navigate(route)
                        }
                    }
                )
            }


            composable("places") {
                val allPlacesViewModel: AllPlacesViewModel =
                    viewModel(factory = AppViewModelProvider.Factory)
                val state by allPlacesViewModel.uiState.collectAsState()
                AllPlacesScreen(
                    state = state,
                    onBackClick = { bottomNavController.navigateUp() },
                    onPlaceClick = { place -> selectedPlace = place },
                    onLoadMore = { allPlacesViewModel.loadMore() }
                )
            }

            composable("events") {
                val allEventsViewModel: AllEventsViewModel =
                    viewModel(factory = AppViewModelProvider.Factory)
                val state by allEventsViewModel.uiState.collectAsState()
                AllEventsScreen(
                    state = state,
                    onBackClick = { bottomNavController.navigateUp() },
                    onEventClick = { event -> selectedEvent = event },
                    onLoadMore = { allEventsViewModel.loadMore() }
                )
            }

            composable("search") {
                val searchViewModel: SearchViewModel =
                    viewModel(factory = AppViewModelProvider.Factory)

                val state by searchViewModel.uiState.collectAsState()

                SearchScreen(
                    state = state,
                    currentUserId = communityViewModel.currentUserId,
                    onQueryChange = { searchViewModel.search(it) },
                    onBackClick = { bottomNavController.navigateUp() },
                    onPlaceClick = { place ->
                        selectedPlace = place
                    },
                    onFilterChange = {
                        searchViewModel.setFilter(it)
                    },
                    onNavigate = { route ->
                        bottomNavController.navigate(route)
                    },
                    onTrendingClick = { keyword ->
                        searchViewModel.onTrendingClick(keyword)
                    }
                )
            }

            composable("itinerary") {
                ItineraryScreen(
                    viewModel = itineraryViewModel,
                    onMenuClick = openDrawer,
                    onCreateClick = { bottomNavController.navigate("create_itinerary") },
                    onEditClick = { itineraryId -> bottomNavController.navigate("edit_itinerary/$itineraryId") },
                    onShareClick = { itineraryId ->
                        communityViewModel.setShareItineraryId(itineraryId)
                        navigateToMainTab("community")
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
                val profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
                ProfileScreen(
                    viewModel = profileViewModel,
                    unreadCount = unreadCount,
                    onMenuClick = openDrawer,
                    onBack = { bottomNavController.popBackStack() },
                    onNavigate = { route ->
                        when {
                            route == "edit_profile" -> bottomNavController.navigate("edit_profile")
                            route.startsWith("post_detail/") -> {
                                val postId = route.removePrefix("post_detail/")
                                communityViewModel.setOpenedPostId(postId)
                                navigateToMainTab("community")
                            }
                            route.startsWith("profile/") -> {
                                navigateToProfile(route.removePrefix("profile/"))
                            }
                            route in mainTabRoutes -> {
                                navigateToMainTab(route)
                            }
                            else -> {
                                bottomNavController.navigate(route)
                            }
                        }
                    },
                )
            }

            composable("edit_profile") { backStackEntry ->
                val profileParentEntry = remember(backStackEntry) {
                    try {
                        bottomNavController.getBackStackEntry("profile")
                    } catch (e: Exception) {
                        backStackEntry
                    }
                }
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = AppViewModelProvider.Factory,
                    viewModelStoreOwner = profileParentEntry,
                )
                EditProfileScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onSaved = {
                        profileViewModel.loadProfile()
                        bottomNavController.popBackStack()
                    },
                )
            }

            composable("notifications") {
                NotificationScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onNavigate = { route ->
                        when {
                            route.startsWith("post_detail/") -> {
                                val postId = route.removePrefix("post_detail/")
                                communityViewModel.setOpenedPostId(postId)
                                navigateToMainTab("community")
                            }
                            route.startsWith("profile/") -> navigateToProfile(route.removePrefix("profile/"))
                            route.startsWith("itinerary_detail/") -> bottomNavController.navigate(route)
                            route in mainTabRoutes -> navigateToMainTab(route)
                            else -> bottomNavController.navigate(route)
                        }
                    },
                    viewModel = notificationViewModel,
                )
            }

            composable(
                route = "profile/{userId}",
                arguments = listOf(
                    androidx.navigation.navArgument("userId") {
                        type = androidx.navigation.NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val profileUserId = backStackEntry.arguments?.getString("userId")
                ProfileScreen(
                    userId = profileUserId,
                    unreadCount = unreadCount,
                    onBack = { bottomNavController.popBackStack() },
                    onNavigate = { route ->
                        when {
                            route.startsWith("post_detail/") -> {
                                val postId = route.removePrefix("post_detail/")
                                communityViewModel.setOpenedPostId(postId)
                                navigateToMainTab("community")
                            }
                            route.startsWith("profile/") -> navigateToProfile(route.removePrefix("profile/"))
                            route == "community" -> {
                                bottomNavController.navigate("community") {
                                    popUpTo("community?shareItineraryId={shareItineraryId}") {
                                        inclusive = true
                                    }
                                }
                            }
                            route in mainTabRoutes -> navigateToMainTab(route)
                            else -> bottomNavController.navigate(route)
                        }
                    },
                )
            }


            composable("my_place_suggestions") {
                MyPlaceSuggestionsScreen(
                    onMenuClick = openDrawer,
                    onAddNew = { bottomNavController.navigate("submit_place_suggestion") },
                )
            }

            composable("submit_place_suggestion") {
                SubmitPlaceSuggestionScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onSubmitted = { bottomNavController.popBackStack() },
                )
            }

            composable("admin_place_suggestions") {
                AdminPlaceSuggestionsScreen(
                    onMenuClick = openDrawer,
                )
            }

            composable("admin_reports") {
                AdminReportsScreen(
                    onMenuClick = openDrawer,
                    onOpenProfile = { userId -> navigateToProfile(userId) },
                    onOpenPost = { postId, commentId ->
                        communityViewModel.setHighlightCommentId(commentId)
                        communityViewModel.setOpenedPostId(postId)
                        navigateToMainTab("community")
                    },
                )
            }
        }
        selectedPlace?.let { place ->
            PlaceIntroductionOverlay(
                place = place,
                onDismiss = { selectedPlace = null },
                onExplore = { selectedPlace = null },
            )
        }
        selectedEvent?.let { event ->
            FestivalIntroductionOverlay(
                event = event,
                onDismiss = { selectedEvent = null },
                onSchedule = {
                    selectedEvent = null
                    navigateToMainTab("itinerary")
                }
            )
        }

    }
    }
}
