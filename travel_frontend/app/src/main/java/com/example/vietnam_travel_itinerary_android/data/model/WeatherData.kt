package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class WeatherData(
    @Json(name = "place_id") val placeId: String? = null,
    @Json(name = "forecast_date") val forecastDate: String? = null,
    @Json(name = "temp_max") val tempMax: Double = 0.0,
    @Json(name = "temp_min") val tempMin: Double = 0.0,
    @Json(name = "rain_mm") val rainMm: Double = 0.0,
    val condition: String = "sunny" // "sunny", "partly_cloudy", "cloudy", "rainy", "stormy"
)
