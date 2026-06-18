package com.example.vietnam_travel_itinerary_android.data.repository

import android.util.Log
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val supabase: SupabaseClient) {

    private val api = RetrofitInstance.api

    private fun getAuthToken(): String {
        val token = supabase.auth.currentAccessTokenOrNull()
        return if (token != null) "Bearer $token" else ""
    }

    suspend fun getTrending(limit: Int = 10): List<String> = withContext(Dispatchers.IO) {
        try {
            val result = api.getTrendingKeywords(limit)
            result.ifEmpty { mockTrendingKeywords.take(limit) }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to fetch trending keywords", e)
            mockTrendingKeywords.take(limit)
        }
    }

    companion object {
        val mockTrendingKeywords = listOf(
            "Hội An", "Hà Nội", "Đà Lạt", "Phú Quốc",
            "Sapa", "Hạ Long", "Đà Nẵng", "Nha Trang",
            "Mù Cang Chải", "Ninh Bình"
        )
    }

    suspend fun logKeyword(query: String) = withContext(Dispatchers.IO) {
        try {
            val token = getAuthToken()
            if (token.isBlank()) return@withContext
            api.logSearchKeyword(token, mapOf("query" to query))
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to log search keyword", e)
        }
    }
}
