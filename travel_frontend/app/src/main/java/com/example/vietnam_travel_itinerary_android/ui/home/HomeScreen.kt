package com.example.vietnam_travel_itinerary_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                onPlaceClick = { place ->
                    onNavigate("place_detail/${place.id}")
                },
                onEventClick = { /* Future: navigate to event detail */ },
                onSearchClick = { onNavigate("search") },
                onNotificationClick = { onNavigate("notifications") },
                onSeeAllPlaces = { onNavigate("places") }
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeViewModel.HomeUiState,
    paddingValues: PaddingValues,
    onPlaceClick: (Place) -> Unit,
    onEventClick: (Event) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSeeAllPlaces: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding()),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ===== HEADER =====
        item {
            AppTopBar(
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick
            )
        }

        // ===== WEATHER WIDGET =====
        item {
            WeatherWidget(
                weather = uiState.weather,
                locationName = uiState.currentLocationName,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // ===== FEATURED BANNER =====
        item {
            FeaturedBanner(
                modifier = Modifier.padding(16.dp),
                onExploreClick = { /* Navigate to featured */ }
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
            if (uiState.recommendedPlaces.isNotEmpty()) {
                PlacesRow(
                    places = uiState.recommendedPlaces,
                    onPlaceClick = onPlaceClick
                )
            } else {
                PlacesRowPlaceholder()
            }
        }

        // ===== FESTIVALS =====
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FestivalsSection(
                events = uiState.activeEvents,
                onEventClick = onEventClick
            )
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
private fun PlacesRowPlaceholder() {
    // Show placeholder cards when data is not yet loaded
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) {
            PlaceCard(
                place = Place(
                    id = "placeholder_$it",
                    name = when (it) {
                        0 -> "Vịnh Hạ Long"
                        1 -> "Phố cổ Hội An"
                        else -> "Sapa"
                    },
                    imageUrl = null,
                    provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary(
                        name = when (it) {
                            0 -> "Quảng Ninh"
                            1 -> "Quảng Nam"
                            else -> "Lào Cai"
                        }
                    )
                )
            )
        }
    }
}

@Composable
private fun FestivalsSection(
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val displayEvents = events.ifEmpty {
        // Placeholder events when API hasn't loaded yet
        listOf(
            Event(
                id = "placeholder_1",
                name = "Lễ hội Áo Dài 2024",
                startDate = "2024-10-15",
                endDate = "2024-10-20",
                places = com.example.vietnam_travel_itinerary_android.data.model.PlaceSummary(
                    name = "TP. Hồ Chí Minh"
                )
            ),
            Event(
                id = "placeholder_2",
                name = "Festival Hoa Đà Lạt",
                startDate = "2024-10-20",
                endDate = "2024-10-25",
                places = com.example.vietnam_travel_itinerary_android.data.model.PlaceSummary(
                    name = "Lâm Đồng"
                )
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VNRed.copy(alpha = 0.05f),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = "Lễ hội đang diễn ra")

            displayEvents.forEach { event ->
                FestivalCard(
                    event = event,
                    onLearnMoreClick = onEventClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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
