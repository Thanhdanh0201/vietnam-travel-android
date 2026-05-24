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
    @Json(name = "review_count") val reviewCount: Int? = null,
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

/** Tên hiển thị thời tiết: địa điểm + tỉnh/thành (nếu khác tên). */
fun Place.locationParts(): Pair<String, String?> {
    val title = name.trim()
    val region = provinces?.name?.takeIf { it.isNotBlank() }
        ?: cities?.name?.takeIf { it.isNotBlank() }
    val subtitle = region?.takeIf { !it.equals(title, ignoreCase = true) }
    return title to subtitle
}
