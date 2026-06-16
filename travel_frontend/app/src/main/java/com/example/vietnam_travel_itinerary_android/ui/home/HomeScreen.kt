package com.example.vietnam_travel_itinerary_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.components.*
import com.example.vietnam_travel_itinerary_android.ui.components.introduction.FestivalIntroductionOverlay
import com.example.vietnam_travel_itinerary_android.ui.components.introduction.PlaceIntroductionOverlay
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    unreadCount: Int = 0,
    onMenuClick: (() -> Unit)? = null,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            if (uiState.isLoading) {
                LoadingContent(paddingValues)
            } else if (uiState.error != null && uiState.recommendedPlaces.isEmpty()) {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.refresh() },
                    paddingValues = paddingValues
                )
            } else {
                HomeContent(
                    uiState = uiState,
                    paddingValues = paddingValues,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onRetryPlaces = { viewModel.loadHomeData() },
                    onPlaceClick = { selectedPlace = it },
                    onEventClick = { selectedEvent = it },
                    onSearchClick = { onNavigate("search") },
                    onNotificationClick = { onNavigate("notifications") },
                    unreadCount = unreadCount,
                    onSeeAllPlaces = { onNavigate("places") },
                    onFavoriteWeatherCityChange = viewModel::setFavoriteWeatherCity,
                    onExploreClick = { onNavigate("explore") },
                    onMenuClick = onMenuClick,
                )
            }
        }
        selectedPlace?.let { place ->
            PlaceIntroductionOverlay(
                place = place,
                onDismiss = { selectedPlace = null },
                onExplore = {
                    selectedPlace = null
                    onNavigate("explore")
                },
            )
        }
        selectedEvent?.let { event ->
            FestivalIntroductionOverlay(
                event = event,
                onDismiss = { selectedEvent = null },
                onSchedule = {
                    selectedEvent = null
                    onNavigate("itinerary")
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeViewModel.HomeUiState,
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRetryPlaces: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onEventClick: (Event) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int = 0,
    onSeeAllPlaces: () -> Unit,
    onFavoriteWeatherCityChange: (String) -> Unit,
    onExploreClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding()),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
        // ===== HEADER =====
        item {
            AppTopBar(
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick,
                unreadCount = unreadCount,
                onMenuClick = onMenuClick,
            )
        }

        // ===== WEATHER WIDGET (vuốt ngang) =====
        item {
            WeatherWidgetCarousel(
                slides = uiState.weatherSlides,
                favoriteCityId = uiState.favoriteWeatherCityId,
                favoriteScrollTick = uiState.favoriteWeatherScrollTick,
                onFavoriteCityChange = onFavoriteWeatherCityChange,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }

        // ===== FEATURED BANNER (auto vuốt ngang) =====
        item {
            FeaturedBanner(
                onExploreClick = { onExploreClick() },
            )
        }

        // ===== RECOMMENDED PLACES =====
        item {
            SectionHeader(
                title = "Gợi ý cho bạn",
                showSeeAll = true,
                onSeeAllClick = onSeeAllPlaces,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        item {
            when {
                !uiState.placesFetched -> PlacesRowLoading()
                uiState.recommendedPlaces.isNotEmpty() -> PlacesRow(
                    places = uiState.recommendedPlaces,
                    onPlaceClick = onPlaceClick,
                )
                else -> PlacesRowError(
                    failed = uiState.placesLoadFailed,
                    onRetry = onRetryPlaces,
                )
            }
        }

        // ===== FESTIVALS =====
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FestivalsSection(
                events = uiState.activeEvents,
                eventsFetched = uiState.eventsFetched,
                eventsLoadFailed = uiState.eventsLoadFailed,
                onEventClick = onEventClick,
            )
        }
        }
    }
}


@Composable
private fun PlacesRow(
    places: List<Place>,
    onPlaceClick: (Place) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(places, key = { it.id }) { place ->
            PlaceCard(
                place = place,
                onPlaceClick = onPlaceClick
            )
        }
    }
}

@Composable
private fun PlacesRowLoading() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(3) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = VNRed,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
private fun PlacesRowError(
    failed: Boolean,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (failed) {
                "Chưa tải được gợi ý từ máy chủ."
            } else {
                "Chưa có địa điểm gợi ý."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (failed) {
            TextButton(onClick = onRetry) {
                Text("Thử lại", color = VNRed)
            }
        }
    }
}

@Composable
private fun FestivalsSection(
    events: List<Event>,
    eventsFetched: Boolean,
    eventsLoadFailed: Boolean,
    onEventClick: (Event) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VNRed.copy(alpha = 0.05f),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = "Lễ hội sắp tới")

            when {
                !eventsFetched && events.isEmpty() -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = VNRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đang tải lễ hội…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                eventsFetched && events.isEmpty() -> {
                    Text(
                        text = if (eventsLoadFailed) {
                            "Chưa tải được lễ hội từ máy chủ. Kéo xuống từ đầu trang để thử lại."
                        } else {
                            "Chưa có lễ hội trong 3 tháng tới."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
                else -> {
                    events.forEach { event ->
                        FestivalCard(
                            event = event,
                            onLearnMoreClick = onEventClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = VNRed,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Đang tải dữ liệu...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "😔",
                fontSize = 48.sp
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = VNRed)
            ) {
                Text("Thử lại")
            }
        }
    }
}
