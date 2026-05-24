package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class Event(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: String? = null,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "province_name") val provinceName: String? = null,
    @Json(name = "place_name") val placeName: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "province_id") val provinceId: String? = null,
    /** Legacy shape from older APIs; prefer [provinceName] / [placeName]. */
    val places: PlaceSummary? = null,
)

/** Địa danh hiển thị: ưu tiên địa điểm cụ thể, sau đó tỉnh, cuối cùng legacy [places]. */
fun Event.displayLocation(): String =
    placeName?.takeIf { it.isNotBlank() }
        ?: provinceName?.takeIf { it.isNotBlank() }
        ?: places?.name.orEmpty()

@JsonClass(generateAdapter = false)
data class PlaceSummary(
    val name: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)
