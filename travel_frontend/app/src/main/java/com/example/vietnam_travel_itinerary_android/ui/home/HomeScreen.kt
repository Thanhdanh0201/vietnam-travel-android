package com.example.vietnam_travel_itinerary_android.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.location.fetchBestLocation
import com.example.vietnam_travel_itinerary_android.location.reverseGeocodeLocality
import com.example.vietnam_travel_itinerary_android.ui.components.*
import com.example.vietnam_travel_itinerary_android.ui.components.introduction.FestivalIntroductionOverlay
import com.example.vietnam_travel_itinerary_android.ui.components.introduction.PlaceIntroductionOverlay
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fused = remember {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }
    var gpsWeatherPassDone by remember { mutableStateOf(false) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        gpsWeatherPassDone = true
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) {
            scope.launch {
                val loc = fused.fetchBestLocation() ?: return@launch
                val label = reverseGeocodeLocality(context, loc.latitude, loc.longitude)
                viewModel.applyGpsWeather(loc.latitude, loc.longitude, label)
            }
        }
    }

    val firstRealPlaceId = uiState.recommendedPlaces.firstOrNull { !it.id.startsWith("placeholder") }?.id
    LaunchedEffect(uiState.isUsingPlaceholder, firstRealPlaceId) {
        if (uiState.isUsingPlaceholder) {
            gpsWeatherPassDone = false
            return@LaunchedEffect
        }
        if (gpsWeatherPassDone) return@LaunchedEffect
        if (firstRealPlaceId == null) return@LaunchedEffect

        if (hasLocationPermission()) {
            scope.launch {
                try {
                    val loc = fused.fetchBestLocation()
                    if (loc != null) {
                        val label = reverseGeocodeLocality(context, loc.latitude, loc.longitude)
                        viewModel.applyGpsWeather(loc.latitude, loc.longitude, label)
                    }
                } finally {
                    gpsWeatherPassDone = true
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

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
                    onPlaceClick = { selectedPlace = it },
                    onEventClick = { selectedEvent = it },
                    onSearchClick = { onNavigate("search") },
                    onNotificationClick = { onNavigate("notifications") },
                    onSeeAllPlaces = { onNavigate("places") }
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
            HomeHeader(
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick
            )
        }

        // ===== WEATHER WIDGET =====
        item {
            WeatherWidget(
                weather = uiState.weather,
                locationName = uiState.currentLocationName.ifBlank { "Đang cập nhật" },
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
                PlacesRowPlaceholder(onPlaceClick = onPlaceClick)
            }
        }

        // ===== FESTIVALS =====
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FestivalsSection(
                events = uiState.activeEvents,
                eventsFetched = uiState.eventsFetched,
                onEventClick = onEventClick
            )
        }
    }
}

@Composable
private fun HomeHeader(
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VNRed,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        text = "VIETNAM TRAVEL",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = VNRed,
                        letterSpacing = 0.5.sp
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = onNotificationClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = VNRed.copy(alpha = 0.1f),
                            contentColor = VNRed
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Thông báo",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    FilledIconButton(
                        onClick = onSearchClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = VNRed.copy(alpha = 0.1f),
                            contentColor = VNRed
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Tìm kiếm",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
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
private fun PlacesRowPlaceholder(onPlaceClick: (Place) -> Unit) {
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
                ),
                onPlaceClick = onPlaceClick,
            )
        }
    }
}

@Composable
private fun FestivalsSection(
    events: List<Event>,
    eventsFetched: Boolean,
    onEventClick: (Event) -> Unit
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
                        text = "Chưa có lễ hội trong 3 tháng tới hoặc chưa tải được dữ liệu từ máy chủ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
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
