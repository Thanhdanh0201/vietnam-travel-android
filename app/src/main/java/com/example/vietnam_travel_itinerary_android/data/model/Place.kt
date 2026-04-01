package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class Place(
    val id: String,
    val name: String,
    val type: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    @Json(name = "image_url") val imageUrl: String? = null,
    val rating: Double? = null,
    @Json(name = "search_count") val searchCount: Int? = null,
    val description: String? = null,
    val provinces: ProvinceSummary? = null,
    val cities: CitySummary? = null
)

@JsonClass(generateAdapter = false)
data class ProvinceSummary(
    val name: String? = null,
    val code: String? = null
)

@JsonClass(generateAdapter = false)
data class CitySummary(
    val name: String? = null
)
