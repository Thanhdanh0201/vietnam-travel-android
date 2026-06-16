package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionRequest
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceSuggestionRepository(
    private val supabase: SupabaseClient,
) {
    private val api = RetrofitInstance.api

    private fun requireToken(): String =
        supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            ?: throw IllegalStateException("Not authenticated")

    suspend fun createSuggestion(request: PlaceSuggestionRequest): PlaceSuggestionResponse =
        withContext(Dispatchers.IO) {
            api.createPlaceSuggestion(requireToken(), request)
        }

    suspend fun getMySuggestions(page: Int = 0, size: Int = 20): List<PlaceSuggestionResponse> =
        withContext(Dispatchers.IO) {
            api.getMyPlaceSuggestions(requireToken(), page, size).content
        }

    suspend fun uploadImage(bytes: ByteArray, fileName: String): String =
        withContext(Dispatchers.IO) {
            val bucket = supabase.storage["post-media"]
            val path = "${System.currentTimeMillis()}_$fileName"
            bucket.upload(path, bytes) { upsert = true }
            bucket.publicUrl(path)
        }
}
