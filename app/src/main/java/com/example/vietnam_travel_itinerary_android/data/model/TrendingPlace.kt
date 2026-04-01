package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class TrendingPlace(
    @Json(name = "place_id") val placeId: String? = null,
    @Json(name = "province_id") val provinceId: String? = null,
    @Json(name = "rank_position") val rankPosition: Int = 0,
    @Json(name = "total_searches") val totalSearches: Int = 0,
    val places: TrendingPlaceDetail? = null
)

@JsonClass(generateAdapter = false)
data class TrendingPlaceDetail(
    val name: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    val type: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)
