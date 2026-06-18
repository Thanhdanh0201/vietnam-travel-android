package com.example.vietnam_travel_itinerary_android.ui.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AllPlacesUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
)

private const val PAGE_SIZE = 20

class AllPlacesViewModel(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllPlacesUiState())
    val uiState: StateFlow<AllPlacesUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            placeRepository.getPlaces(limit = PAGE_SIZE, offset = 0)
                .onSuccess { places ->
                    _uiState.value = _uiState.value.copy(
                        places = places,
                        isLoading = false,
                        hasMore = places.size >= PAGE_SIZE
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)
            val offset = state.places.size
            placeRepository.getPlaces(limit = PAGE_SIZE, offset = offset)
                .onSuccess { newPlaces ->
                    _uiState.value = _uiState.value.copy(
                        places = _uiState.value.places + newPlaces,
                        isLoadingMore = false,
                        hasMore = newPlaces.size >= PAGE_SIZE
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = e.message
                    )
                }
        }
    }
}
