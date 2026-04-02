package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository {
    private val api = RetrofitInstance.api

    suspend fun getEventsByProvince(provinceCode: String): Result<List<Event>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getEventsByProvince(provinceCode))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
