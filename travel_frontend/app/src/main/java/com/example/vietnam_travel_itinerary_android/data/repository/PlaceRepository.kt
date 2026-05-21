package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.PlaceDetail
import com.example.vietnam_travel_itinerary_android.data.model.PlaceReview
import com.example.vietnam_travel_itinerary_android.data.model.SubmitPlaceReviewRequest
import com.example.vietnam_travel_itinerary_android.data.model.TrendingPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

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

    /** Gợi ý trang chủ — GET /api/places/recommended?limit=N (rating DESC). */
    suspend fun getRecommendedPlaces(limit: Int = 10): Result<List<Place>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getRecommendedPlaces(limit = limit))
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

    suspend fun getPlaceDetail(placeId: String): Result<PlaceDetail> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getPlaceDetail(placeId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitPlaceReview(
        placeId: String,
        token: String,
        rating: Int,
        review: String?,
        photoUrls: List<String> = emptyList(),
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.submitPlaceReview(
                token = token,
                placeId = placeId,
                body = SubmitPlaceReviewRequest(
                    rating = rating,
                    review = review?.takeIf { it.isNotBlank() },
                    photoUrls = photoUrls,
                ),
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
