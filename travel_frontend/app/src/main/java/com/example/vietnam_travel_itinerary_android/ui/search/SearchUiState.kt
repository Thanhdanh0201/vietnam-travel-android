package com.example.vietnam_travel_itinerary_android.ui.search

import com.example.vietnam_travel_itinerary_android.data.model.CommunityPost
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.UserProfile

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val posts: List<CommunityPost> = emptyList(),
    val users: List<UserProfile> = emptyList(),
    val itineraries: List<Itinerary> = emptyList(),
    val error: String? = null
)
