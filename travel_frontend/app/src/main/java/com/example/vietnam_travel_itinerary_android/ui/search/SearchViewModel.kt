package com.example.vietnam_travel_itinerary_android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.repository.ItineraryRepository
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchViewModel(
    private val placeRepository: PlaceRepository,
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())

    val uiState: StateFlow<SearchUiState> =
        _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(query = query)

        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                places = emptyList(),
                itineraries = emptyList(),
                isLoading = false,
                error = null
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // debounce

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val placesResult = placeRepository.searchPlaces(query)
                val itineraryResult = itineraryRepository.getItineraries()

                val places = placesResult.getOrDefault(emptyList())

                val itineraries = itineraryResult
                    .getOrDefault(emptyList())
                    .filter {
                        it.title.contains(query, ignoreCase = true)
                    }

                _uiState.value = _uiState.value.copy(
                    places = places,
                    itineraries = itineraries,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

}

