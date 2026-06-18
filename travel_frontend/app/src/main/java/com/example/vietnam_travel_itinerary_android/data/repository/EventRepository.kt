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

    suspend fun getUpcomingEvents(months: Int = 3, limit: Int = 40): Result<List<Event>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getUpcomingEvents(months, limit))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getUpcomingEventsPaged(
        months: Int = 12,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getUpcomingEvents(months, limit, offset))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllEventsPaged(
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getAllEvents(limit, offset))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
