package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class WeatherNearby(
    @Json(name = "city_key") val cityKey: String? = null,
    @Json(name = "place_id") val placeId: String,
    @Json(name = "place_name") val placeName: String,
    @Json(name = "province_name") val provinceName: String? = null,
    @Json(name = "city_name") val cityName: String? = null,
    @Json(name = "forecast_date") val forecastDate: String? = null,
    @Json(name = "temp_max") val tempMax: Double = 0.0,
    @Json(name = "temp_min") val tempMin: Double = 0.0,
    @Json(name = "rain_mm") val rainMm: Double = 0.0,
    val humidity: Int? = null,
    val condition: String = "sunny",
    @Json(name = "fetched_at") val fetchedAt: String? = null,
) {
    fun toWeatherData(): WeatherData = WeatherData(
        placeId = placeId,
        forecastDate = forecastDate,
        tempMax = tempMax,
        tempMin = tempMin,
        rainMm = rainMm,
        humidity = humidity,
        condition = condition,
        fetchedAt = fetchedAt,
    )
}
