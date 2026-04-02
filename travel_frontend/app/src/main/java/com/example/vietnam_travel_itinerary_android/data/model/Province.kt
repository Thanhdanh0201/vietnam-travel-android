package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class Province(
    val id: String,
    val name: String,
    val code: String,
    val region: String? = null
)
