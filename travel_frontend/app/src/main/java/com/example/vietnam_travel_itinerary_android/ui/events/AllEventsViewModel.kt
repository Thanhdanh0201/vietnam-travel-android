package com.example.vietnam_travel_itinerary_android.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AllEventsUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
)

private const val PAGE_SIZE = 20

class AllEventsViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllEventsUiState())
    val uiState: StateFlow<AllEventsUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            eventRepository.getAllEventsPaged(limit = PAGE_SIZE, offset = 0)
                .onSuccess { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false,
                        hasMore = events.size >= PAGE_SIZE
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
            val offset = state.events.size
            eventRepository.getAllEventsPaged(limit = PAGE_SIZE, offset = offset)
                .onSuccess { newEvents ->
                    _uiState.value = _uiState.value.copy(
                        events = _uiState.value.events + newEvents,
                        isLoadingMore = false,
                        hasMore = newEvents.size >= PAGE_SIZE
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
