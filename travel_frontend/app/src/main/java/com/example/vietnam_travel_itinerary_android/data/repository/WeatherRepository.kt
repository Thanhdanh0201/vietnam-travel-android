package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.model.WeatherData
import com.example.vietnam_travel_itinerary_android.data.model.WeatherNearby
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository {
    private val api = RetrofitInstance.api

    suspend fun getFeaturedWeather(): Result<List<WeatherNearby>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getFeaturedWeather())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getWeatherNearby(lat: Double, lng: Double): Result<WeatherNearby> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getWeatherNearby(lat, lng))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getWeather(placeId: String): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getWeather(placeId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeatherForecast(
        placeId: String,
        days: Int = 7
    ): Result<List<WeatherData>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getWeatherForecast(placeId, days))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
