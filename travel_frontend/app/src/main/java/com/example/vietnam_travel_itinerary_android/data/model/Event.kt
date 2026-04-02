package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class Event(
    val id: String,
    val name: String,
    val description: String? = null,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "province_id") val provinceId: String? = null,
    val places: PlaceSummary? = null
)

@JsonClass(generateAdapter = false)
data class PlaceSummary(
    val name: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)
