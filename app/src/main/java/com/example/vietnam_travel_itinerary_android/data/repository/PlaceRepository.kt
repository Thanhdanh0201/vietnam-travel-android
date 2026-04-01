package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.TrendingPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceRepository {
    private val api = RetrofitInstance.api

    suspend fun getPlaces(
        provinceCode: String? = null,
        type: String? = null,
        limit: Int = 20
    ): Result<List<Place>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getPlaces(provinceCode, type, limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingPlaces(
        provinceCode: String? = null,
        limit: Int = 10
    ): Result<List<TrendingPlace>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getTrendingPlaces(provinceCode, limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlace(placeId: String): Result<Place> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getPlace(placeId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
