package com.example.vietnam_travel_itinerary_android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.data.repository.EventRepository
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import com.example.vietnam_travel_itinerary_android.data.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class HomeViewModel : ViewModel() {

    data class HomeUiState(
        val isLoading: Boolean = false,
        val weather: WeatherData? = null,
        val recommendedPlaces: List<Place> = emptyList(),
        val trendingPlaces: List<TrendingPlace> = emptyList(),
        val activeEvents: List<Event> = emptyList(),
        val error: String? = null,
        val currentLocationName: String = "Đà Lạt",
        val isUsingPlaceholder: Boolean = true
    )

    private val placeRepo = PlaceRepository()
    private val weatherRepo = WeatherRepository()
    private val eventRepo = EventRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Show placeholder data IMMEDIATELY so UI is never empty
        showPlaceholderData()
        // Then try to load real data in background
        loadHomeData()
    }

    private fun showPlaceholderData() {
        _uiState.update {
            it.copy(
                isLoading = false,
                isUsingPlaceholder = true,
                weather = WeatherData(
                    placeId = "placeholder",
                    forecastDate = "2024-10-15",
                    tempMax = 26.0,
                    tempMin = 18.0,
                    rainMm = 0.0,
                    condition = "sunny"
                ),
                recommendedPlaces = placeholderPlaces(),
                activeEvents = placeholderEvents()
            )
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            try {
                // Timeout after 10s to prevent ANR
                val result = withTimeoutOrNull(10_000L) {
                    val placesDeferred = async { placeRepo.getPlaces(limit = 10) }
                    val trendingDeferred = async { placeRepo.getTrendingPlaces(limit = 10) }

                    val placesResult = placesDeferred.await()
                    val trendingResult = trendingDeferred.await()

                    Pair(placesResult, trendingResult)
                }

                if (result != null) {
                    val (placesResult, trendingResult) = result
                    val places = placesResult.getOrNull()
                    val trending = trendingResult.getOrNull()

                    if (!places.isNullOrEmpty() || !trending.isNullOrEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isUsingPlaceholder = false,
                                recommendedPlaces = places?.ifEmpty { placeholderPlaces() }
                                    ?: placeholderPlaces(),
                                trendingPlaces = trending ?: emptyList(),
                                error = null
                            )
                        }
                    }
                }
                // If timeout or failure, placeholder data remains — no error shown

                // Load weather in background (non-blocking)
                loadWeather()
                // Load events in background
                loadEvents()

            } catch (_: Exception) {
                // Silently keep placeholder data on failure
            }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            try {
                val placeId = _uiState.value.recommendedPlaces.firstOrNull()?.id
                if (placeId != null && !placeId.startsWith("placeholder")) {
                    withTimeoutOrNull(8_000L) {
                        weatherRepo.getWeather(placeId).onSuccess { weather ->
                            _uiState.update { it.copy(weather = weather) }
                        }
                    }
                }
            } catch (_: Exception) { /* Keep placeholder weather */ }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                withTimeoutOrNull(8_000L) {
                    eventRepo.getEventsByProvince("79").onSuccess { events ->
                        if (events.isNotEmpty()) {
                            _uiState.update {
                                it.copy(activeEvents = events, isUsingPlaceholder = false)
                            }
                        }
                    }
                }
            } catch (_: Exception) { /* Keep placeholder events */ }
        }
    }

    fun refresh() {
        showPlaceholderData()
        loadHomeData()
    }

    // ============================================================
    // Placeholder / Sample Data
    // ============================================================

    private fun placeholderPlaces(): List<Place> = listOf(
        Place(
            id = "placeholder_1",
            name = "Vịnh Hạ Long",
            type = "thiên nhiên",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBuYPhU7veBhvyka3NMHanAx-Y6N4yOw_4B95lEq0nJ-OzAHD7tl9ytpqQG0-CAx_Q6zykZG51Ici2sRDg1BOKlPdCkEEWbxqoii178ChGB4MSA57L9I1fyLRHRc2CO5ej5XVIquw65kGR8BTpMbFc90ND0tCjPbbO5_ze9udApBzZXI-PH0JOJve86j2OTgV8_FS_T7oV5j8u16EE_Cfhyzl_iyaFlF_GtL9_kgPmSIk3SNnItk09Ej8RhC4sydSfovuwW0CLlXg",
            rating = 4.8,
            provinces = ProvinceSummary(name = "Quảng Ninh", code = "22")
        ),
        Place(
            id = "placeholder_2",
            name = "Phố cổ Hội An",
            type = "văn hóa",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCqQIFXQo_FEd8pAnt_27xOwgp06ex_ELbEr4Y8QnoVzQAKBuUTAuJJOj9VjhJnj0jIbKGWzRDzE8ZyKgjz7dEEXgLyAsFpuMeCesn_MagOCqPa0ZEXLMUnvjZ08_vlXHYoM6UyFoA6xjUbpnR2u3NzuOKCnHZ9JUT0DmOD8ITJXOoXQ8YRrqdh9YcW3bbtpKYB50UZ9QKY3Jf2-KAbmHaJAqRQDJojZvhyk12WYij-LVtnrBheMpfTITSjqONlYRD05CDCDU0IdQ",
            rating = 4.7,
            provinces = ProvinceSummary(name = "Quảng Nam", code = "49")
        ),
        Place(
            id = "placeholder_3",
            name = "Sapa",
            type = "thiên nhiên",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDZZxPFedvoaOhbzXxMv9k6jB3FOdDdf2g4pShyyV30ssGdEZaG3xu3Lpt5bhxy8xfqHvfJOnAEL9RWMWib8LIzWxdgq0BLk6P692h69F3ykZyNs7cVNxmS737g0kARtNd0_Ds1opyN0SjZd5xMOKjP8Qks_LQvJ5PREcLnZgp5KwH_ZVOcLvpNSvyGAHRV4BETRZfFA6MSeJm017zpr2XEX5RBmjsxRQy0OJoe_SASaSESVVEGoBN37S7fO58usQlDmHON0lU5Gw",
            rating = 4.6,
            provinces = ProvinceSummary(name = "Lào Cai", code = "10")
        ),
        Place(
            id = "placeholder_4",
            name = "Đà Lạt",
            type = "thiên nhiên",
            imageUrl = null,
            rating = 4.7,
            provinces = ProvinceSummary(name = "Lâm Đồng", code = "68")
        ),
    )

    private fun placeholderEvents(): List<Event> = listOf(
        Event(
            id = "placeholder_e1",
            name = "Lễ hội Áo Dài 2024",
            startDate = "2024-10-15",
            endDate = "2024-10-20",
            places = PlaceSummary(name = "TP. Hồ Chí Minh")
        ),
        Event(
            id = "placeholder_e2",
            name = "Festival Hoa Đà Lạt",
            startDate = "2024-10-20",
            endDate = "2024-10-25",
            places = PlaceSummary(name = "Lâm Đồng")
        ),
    )
}
