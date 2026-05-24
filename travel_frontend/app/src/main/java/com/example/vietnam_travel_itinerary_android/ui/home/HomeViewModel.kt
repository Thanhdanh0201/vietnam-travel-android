package com.example.vietnam_travel_itinerary_android.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.local.WeatherFavoritePreferences
import com.example.vietnam_travel_itinerary_android.data.model.WeatherCityPreset
import com.example.vietnam_travel_itinerary_android.data.model.WeatherCityPresets
import com.example.vietnam_travel_itinerary_android.data.model.WeatherCitySlide
import com.example.vietnam_travel_itinerary_android.data.model.WeatherNearby
import com.example.vietnam_travel_itinerary_android.data.model.TrendingPlace
import com.example.vietnam_travel_itinerary_android.data.repository.EventRepository
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import com.example.vietnam_travel_itinerary_android.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    data class HomeUiState(
        val isLoading: Boolean = false,
        val weatherSlides: List<WeatherCitySlide> = WeatherCityPresets.initialSlides(),
        val recommendedPlaces: List<Place> = emptyList(),
        val placesFetched: Boolean = false,
        val placesLoadFailed: Boolean = false,
        val trendingPlaces: List<TrendingPlace> = emptyList(),
        val activeEvents: List<Event> = emptyList(),
        /** Đã gọi API lễ hội sắp tới xong (kể cả lỗi/rỗng). */
        val eventsFetched: Boolean = false,
        /** API lễ hội lỗi/timeout — khác với danh sách thật sự rỗng. */
        val eventsLoadFailed: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val isUsingPlaceholder: Boolean = true,
        val favoriteWeatherCityId: String = WeatherCityPresets.DEFAULT_FAVORITE_CITY_ID,
        /** Tăng khi user nhấn ♥ — carousel cuộn tới thành phố mặc định. */
        val favoriteWeatherScrollTick: Long = 0L,
    )

    private val weatherFavorites = WeatherFavoritePreferences(application)
    private val placeRepo = PlaceRepository()
    private val weatherRepo = WeatherRepository()
    private val eventRepo = EventRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var weatherSlidesJob: Job? = null

    init {
        _uiState.update {
            it.copy(favoriteWeatherCityId = weatherFavorites.getFavoriteCityId())
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                weatherSlides = WeatherCityPresets.initialSlides(),
                recommendedPlaces = emptyList(),
                placesFetched = false,
                placesLoadFailed = false,
            )
        }
        loadRecommendedPlaces()
        loadTrendingPlaces()
        loadWeatherSlides()
        loadUpcomingEvents()
    }

    fun loadHomeData() {
        loadRecommendedPlaces()
        loadTrendingPlaces()
    }

    private fun loadRecommendedPlaces() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update {
                    it.copy(placesFetched = false, placesLoadFailed = false)
                }
            }
            val (places, failed) = fetchRecommendedPlacesWithRetry()
            _uiState.update {
                it.copy(
                    recommendedPlaces = places,
                    placesFetched = true,
                    placesLoadFailed = failed,
                    isUsingPlaceholder = false,
                    error = null,
                )
            }
        }
    }

    private suspend fun fetchRecommendedPlacesWithRetry(): Pair<List<Place>, Boolean> {
        var lastFailure = true
        repeat(4) { attempt ->
            val places = withTimeoutOrNull(12_000L) {
                placeRepo.getRecommendedPlaces(limit = 10).getOrNull()
            }
            if (!places.isNullOrEmpty()) {
                return places to false
            }
            if (places != null) lastFailure = false
            if (attempt < 3) delay(1_500)
        }
        return emptyList<Place>() to lastFailure
    }

    private fun loadTrendingPlaces() {
        viewModelScope.launch {
            val trending = withTimeoutOrNull(8_000L) {
                placeRepo.getTrendingPlaces(limit = 10).getOrNull()
            }
            if (!trending.isNullOrEmpty()) {
                _uiState.update { it.copy(trendingPlaces = trending) }
            }
        }
    }

    fun setFavoriteWeatherCity(cityId: String) {
        if (WeatherCityPresets.cities.none { it.id == cityId }) return
        weatherFavorites.setFavoriteCityId(cityId)
        _uiState.update {
            it.copy(
                favoriteWeatherCityId = cityId,
                favoriteWeatherScrollTick = System.currentTimeMillis(),
            )
        }
    }

    /** Đọc weather_cache từ backend (GET /api/weather/featured) — không gọi Open-Meteo từ app. */
    fun loadWeatherSlides() {
        weatherSlidesJob?.cancel()
        weatherSlidesJob = viewModelScope.launch {
            val presets = WeatherCityPresets.cities
            markWeatherSlidesLoading(presets)

            val featuredByCity = fetchFeaturedWeatherWithRetry()
            val weatherByCityId = featuredByCity.toMutableMap()
            for (preset in presets) {
                if (weatherByCityId.containsKey(preset.id)) continue
                val nearby = withTimeoutOrNull(6_000L) {
                    weatherRepo.getWeatherNearby(preset.lat, preset.lng).getOrNull()
                }
                if (nearby != null) {
                    weatherByCityId[preset.id] = nearby
                }
            }

            applyWeatherSlides(presets, weatherByCityId)
        }
    }

    private fun markWeatherSlidesLoading(presets: List<WeatherCityPreset>) {
        _uiState.update { state ->
            val current = state.weatherSlides
            state.copy(
                weatherSlides = presets.map { preset ->
                    val existing = current.find { it.id == preset.id }
                    when {
                        existing?.weather != null -> existing.copy(isLoading = false, loadFailed = false)
                        else -> WeatherCitySlide(
                            id = preset.id,
                            displayName = preset.displayName,
                            subtitle = preset.subtitle,
                            lat = preset.lat,
                            lng = preset.lng,
                            isLoading = true,
                            loadFailed = false,
                        )
                    }
                },
            )
        }
    }

    private suspend fun fetchFeaturedWeatherWithRetry(): Map<String, WeatherNearby> {
        val list = (1..4).firstNotNullOfOrNull { attempt ->
            val response = withTimeoutOrNull(10_000L) {
                weatherRepo.getFeaturedWeather().getOrNull()
            }
            if (!response.isNullOrEmpty()) return@firstNotNullOfOrNull response
            if (attempt < 4) delay(1_500)
            null
        }.orEmpty()

        return list.mapNotNull { item ->
            val key = item.cityKey?.trim().orEmpty()
            if (key.isNotEmpty()) key to item else null
        }.toMap()
    }

    private fun applyWeatherSlides(
        presets: List<WeatherCityPreset>,
        weatherByCityId: Map<String, WeatherNearby>,
    ) {
        _uiState.update { state ->
            state.copy(
                weatherSlides = presets.map { preset ->
                    val nearby = weatherByCityId[preset.id]
                    WeatherCitySlide(
                        id = preset.id,
                        displayName = preset.displayName,
                        subtitle = preset.subtitle,
                        lat = preset.lat,
                        lng = preset.lng,
                        weather = nearby?.toWeatherData(),
                        isLoading = false,
                        loadFailed = nearby == null,
                    )
                },
            )
        }
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update {
                    it.copy(eventsFetched = false, eventsLoadFailed = false)
                }
            }
            val (filtered, failed) = fetchUpcomingEventsWithRetry()
            _uiState.update {
                it.copy(
                    activeEvents = filtered,
                    eventsFetched = true,
                    eventsLoadFailed = failed,
                )
            }
        }
    }

    private suspend fun fetchUpcomingEventsWithRetry(): Pair<List<Event>, Boolean> {
        var lastFailure = true
        repeat(4) { attempt ->
            val result = withTimeoutOrNull(12_000L) {
                eventRepo.getUpcomingEvents(months = 3, limit = 40)
            }
            when {
                result?.isSuccess == true -> {
                    val raw = result.getOrNull().orEmpty()
                    return filterEventsUpcomingWindow(raw, months = 3) to false
                }
                result?.isFailure == true -> lastFailure = true
            }
            if (attempt < 3) delay(1_500)
        }
        return emptyList<Event>() to lastFailure
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, eventsLoadFailed = false) }
            try {
                coroutineScope {
                    val placesJob = async { fetchPlacesAndTrending() }
                    val eventsJob = async {
                        val (events, failed) = fetchUpcomingEventsWithRetry()
                        _uiState.update {
                            it.copy(
                                activeEvents = events,
                                eventsFetched = true,
                                eventsLoadFailed = failed,
                            )
                        }
                    }
                    loadWeatherSlides()
                    placesJob.await()
                    eventsJob.await()
                    weatherSlidesJob?.join()
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private suspend fun fetchPlacesAndTrending() {
        val (places, failed) = fetchRecommendedPlacesWithRetry()
        _uiState.update {
            it.copy(
                recommendedPlaces = places,
                placesFetched = true,
                placesLoadFailed = failed,
                isUsingPlaceholder = false,
            )
        }
        val trending = withTimeoutOrNull(8_000L) {
            placeRepo.getTrendingPlaces(limit = 10).getOrNull()
        }
        if (!trending.isNullOrEmpty()) {
            _uiState.update { it.copy(trendingPlaces = trending) }
        }
    }

    /**
     * Lọc cục bộ (đồng bộ với backend): sự kiện giao với [hôm nay, hôm nay + months] theo start_date/end_date.
     */
    private fun filterEventsUpcomingWindow(events: List<Event>, months: Int): List<Event> {
        if (events.isEmpty()) return emptyList()
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val today = LocalDate.now(zone)
        val windowEnd = today.plusMonths(months.toLong())

        fun parseIso(s: String): LocalDate? =
            try {
                LocalDate.parse(s.trim().take(10))
            } catch (_: Exception) {
                null
            }

        return events.mapNotNull { e ->
            val start = parseIso(e.startDate) ?: return@mapNotNull null
            val end = parseIso(e.endDate) ?: start
            if (!end.isBefore(today) && !start.isAfter(windowEnd)) e else null
        }
            .sortedBy { parseIso(it.startDate) ?: LocalDate.MAX }
            .take(30)
    }
}
