package com.example.vietnam_travel_itinerary_android.ui.itinerary

import com.squareup.moshi.Json

data class UpdateItineraryRequest(
    val title: String?,
    val description: String?,
    @Json(name = "is_public") val isPublic: Boolean?,
    val status: String?,
    @Json(name = "cover_url") val coverUrl: String?
)