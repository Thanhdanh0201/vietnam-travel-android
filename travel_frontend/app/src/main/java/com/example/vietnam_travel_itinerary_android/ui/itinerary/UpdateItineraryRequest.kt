package com.example.vietnam_travel_itinerary_android.ui.itinerary

data class UpdateItineraryRequest(
    val title: String,
    val description: String,
    val is_public: Boolean
)