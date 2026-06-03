package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.itinerary.UpdateItineraryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItineraryRepository {

    private val api = RetrofitInstance.api

    suspend fun getItineraries(): Result<List<Itinerary>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getItineraries()

                Result.success(
                    response?.filterNotNull() ?: emptyList()
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    suspend fun updateItinerary(
        id: String,
        request: UpdateItineraryRequest
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                api.updateItinerary(id, request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}