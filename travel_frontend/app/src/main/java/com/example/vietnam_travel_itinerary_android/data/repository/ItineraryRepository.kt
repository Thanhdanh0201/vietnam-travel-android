package com.example.vietnam_travel_itinerary_android.data.repository

import android.util.Log
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.CollaboratorDto
import com.example.vietnam_travel_itinerary_android.data.dto.*
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.itinerary.TimelineItemData
import com.example.vietnam_travel_itinerary_android.ui.itinerary.UpdateItineraryRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItineraryRepository(private val supabase: SupabaseClient) {

    private val api = RetrofitInstance.api

    suspend fun uploadCover(byteArray: ByteArray, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val bucket = supabase.storage["itinerary-covers"]
                val path = "${System.currentTimeMillis()}_$fileName"
                bucket.upload(path, byteArray) {
                    upsert = true
                }
                Result.success(bucket.publicUrl(path))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun getAuthToken(): String {
        val token = supabase.auth.currentAccessTokenOrNull()
        return if (token != null) "Bearer $token" else ""
    }

    private fun ItineraryResponseDto.toItinerary(): Itinerary {
        val statusText = if (startDate != null) {
            try {
                val today = java.time.LocalDate.now()
                val start = java.time.LocalDate.parse(startDate)
                if (start.isAfter(today)) "SẮP DIỄN RA" else "ĐÃ KẾT THÚC"
            } catch (e: Exception) {
                "SẮP DIỄN RA"
            }
        } else {
            "SẮP DIỄN RA"
        }

        val isUpcoming = statusText == "SẮP DIỄN RA"

        val dateRangeStr = if (startDate != null && endDate != null) {
            try {
                val start = java.time.LocalDate.parse(startDate)
                val end = java.time.LocalDate.parse(endDate)
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
                val yearFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy")
                "${start.format(formatter)} - ${end.format(formatter)}/${start.format(yearFormatter)}"
            } catch (e: Exception) {
                "Sắp xếp sau"
            }
        } else {
            "Sắp xếp sau"
        }

        return Itinerary(
            id = id,
            title = title,
            location = location ?: "Việt Nam",
            dateRange = dateRangeStr,
            statusText = statusText,
            statusSubText = if (isUpcoming) "🕒 Lịch trình mới" else null,
            isUpcoming = isUpcoming,
            imageResId = android.R.drawable.ic_menu_gallery,
            participantImages = emptyList(),
            coverUrl = coverUrl,
            status = status,
            description = description,
            shareCount = shareCount ?: 0,
            isPublic = isPublic ?: false,
            myRole = myRole ?: "OWNER"
        )
    }

    private fun ItineraryItemResponseDto.toTimelineItemData(): TimelineItemData {
        val timeStr = if (scheduledTime != null) {
            try {
                val localTime = java.time.LocalTime.parse(scheduledTime)
                val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
                localTime.format(formatter).uppercase()
            } catch (e: Exception) {
                "08:00 AM"
            }
        } else {
            "08:00 AM"
        }

        return TimelineItemData(
            time = timeStr,
            title = placeName,
            location = location ?: "",
            tag = tag ?: "Tham quan",
            imageUrl = imageUrl ?: "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b",
            id = id,
            day = day ?: "",
            note = note,
            warningType = warningType,
            warningValue = warningValue
        )
    }

    suspend fun getItineraries(): Result<List<Itinerary>> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) return@withContext Result.success(emptyList())
                val response = api.getMyItineraries(token)
                Result.success(response.map { it.toItinerary() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getItineraryById(id: String): Result<Itinerary> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                val response = api.getItineraryById(token, id)
                Result.success(response.toItinerary())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createItinerary(
        title: String,
        location: String,
        startDate: String?,
        endDate: String?,
        description: String?,
        coverUrl: String?,
        isPublic: Boolean
    ): Result<Itinerary> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                val request = CreateItineraryRequest(
                    title = title,
                    location = location,
                    startDate = startDate,
                    endDate = endDate,
                    description = description,
                    coverUrl = coverUrl,
                    isPublic = isPublic
                )
                val response = api.createItinerary(token, request)
                Result.success(response.toItinerary())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteItinerary(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                api.deleteItinerary(token, id)
                Result.success(Unit)
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
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                api.updateItinerary(token, id, request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getItineraryItems(itineraryId: String): Result<List<TimelineItemData>> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) {
                    Log.w("ItineraryRepo", "getItineraryItems: No auth token")
                    return@withContext Result.success(emptyList())
                }
                val response = api.getItineraryItems(token, itineraryId)
                Log.d("ItineraryRepo", "getItineraryItems($itineraryId): ${response.size} items")
                Result.success(response.map { it.toTimelineItemData() })
            } catch (e: Exception) {
                Log.e("ItineraryRepo", "getItineraryItems($itineraryId) FAILED", e)
                Result.failure(e)
            }
        }

    suspend fun addItineraryItem(
        itineraryId: String,
        placeId: String,
        scheduledTime: String, // HH:mm:ss
        day: String,
        note: String?,
        orderIndex: Int
    ): Result<TimelineItemData> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                val request = CreateItineraryItemRequest(
                    placeId = placeId,
                    scheduledTime = scheduledTime,
                    day = day,
                    note = note,
                    orderIndex = orderIndex
                )
                val response = api.addItineraryItem(token, itineraryId, request)
                Result.success(response.toTimelineItemData())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteItineraryItem(itineraryId: String, itemId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                api.deleteItineraryItem(token, itineraryId, itemId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCollaborators(itineraryId: String): Result<List<CollaboratorDto>> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) {
                    Log.w("ItineraryRepo", "getCollaborators: No auth token")
                    return@withContext Result.success(emptyList())
                }
                val response = api.getCollaborators(token, itineraryId)
                Log.d("ItineraryRepo", "getCollaborators($itineraryId): ${response.size} collabs")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ItineraryRepo", "getCollaborators($itineraryId) FAILED", e)
                Result.failure(e)
            }
        }

    suspend fun addCollaborator(itineraryId: String, email: String, name: String, role: String): Result<CollaboratorDto> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                val response = api.addCollaborator(token, itineraryId, CollaboratorDto(email, name, role))
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun removeCollaborator(itineraryId: String, email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                if (token.isBlank()) throw Exception("Chưa đăng nhập")
                api.removeCollaborator(token, itineraryId, email)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getProvinces(): Result<List<com.example.vietnam_travel_itinerary_android.data.model.Province>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getProvinces())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCitiesByProvince(code: String): Result<List<CityDto>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getCitiesByProvince(code))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
