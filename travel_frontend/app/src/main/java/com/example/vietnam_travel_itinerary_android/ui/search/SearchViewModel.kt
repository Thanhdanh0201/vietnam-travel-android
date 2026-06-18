package com.example.vietnam_travel_itinerary_android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.repository.ItineraryRepository
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import com.example.vietnam_travel_itinerary_android.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.vietnam_travel_itinerary_android.data.model.CommunityPost
import kotlinx.coroutines.Job
import android.util.Log
import com.example.vietnam_travel_itinerary_android.data.model.UserProfile
import com.example.vietnam_travel_itinerary_android.data.repository.CommunityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class SearchViewModel(
    private val placeRepository: PlaceRepository,
    private val itineraryRepository: ItineraryRepository,
    private val communityRepository: CommunityRepository,
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())

    val uiState: StateFlow<SearchUiState> =
        _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        loadTrending()
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTrendingLoading = true)
            val keywords = searchRepository.getTrending()
            _uiState.value = _uiState.value.copy(
                trendingKeywords = keywords,
                isTrendingLoading = false
            )
        }
    }

    fun onTrendingClick(keyword: String) {
        search(keyword)
    }

    fun setFilter(filter: SearchFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter
        )
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(query = query)

        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                places = emptyList(),
                itineraries = emptyList(),
                posts = emptyList(),
                users = emptyList(),
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
                Log.d("SEARCH_DEBUG", "Searching for: $query")
                Log.d("SEARCH_DEBUG", "Raw result: $placesResult")
                val itineraryResult = itineraryRepository.getItineraries()
                val postResult = communityRepository.getPublicFeed(
                    limit = 100
                )

                val users = postResult
                    .map { post ->
                        UserProfile(
                            id = post.userId,
                            name = post.authorName,
                            avatarUrl = post.authorAvatarUrl,
                            avatarInitials = post.authorAvatarInitials,
                            avatarColor = post.authorAvatarColor,
                            isVerified = false // use real value if your CommunityPost has it
                        )
                    }
                    .distinctBy { it.id }
                    .filter {
                        it.name.contains(query, ignoreCase = true)
                    }

                val places = placesResult.getOrDefault(emptyList())
                Log.d("SEARCH_DEBUG", "Number of places: ${places.size}")
                Log.d("SEARCH_DEBUG", "Places: $places")

                val itineraries = itineraryResult
                    .getOrDefault(emptyList())
                    .filter {
                        it.title.contains(query, ignoreCase = true)
                    }
                val posts = postResult
                    .map { post ->

                        val score = when {
                            post.place?.name?.contains(query, true) == true -> 100

                            post.linkedItinerary
                                ?.title
                                ?.contains(query, true) == true -> 80

                            post.content.contains(query, true) -> 50

                            post.authorName.contains(query, true) -> 30

                            else -> 0
                        }

                        post to score
                    }
                    .filter {
                        it.second > 0
                    }
                    .sortedByDescending {
                        it.second
                    }
                    .map {
                        it.first
                    }

                _uiState.value = _uiState.value.copy(
                    places = places,
                    posts = posts,
                    itineraries = itineraries,
                    users = users,
                    isLoading = false
                )

                // Log keyword in background — không block UI
                launch { searchRepository.logKeyword(query) }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("SEARCH_DEBUG", "Search failed", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

}

