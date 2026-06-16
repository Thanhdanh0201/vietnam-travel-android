package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.AdminReportResponse
import com.example.vietnam_travel_itinerary_android.data.dto.BanUserRequest
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionResponse
import com.example.vietnam_travel_itinerary_android.data.dto.RejectSuggestionRequest
import com.example.vietnam_travel_itinerary_android.data.dto.ResolveReportRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminRepository(
    private val supabase: SupabaseClient,
) {
    private val api = RetrofitInstance.api

    private fun requireToken(): String =
        supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            ?: throw IllegalStateException("Not authenticated")

    // ---- Place Suggestions ----
    suspend fun getPlaceSuggestions(status: String?, page: Int = 0, size: Int = 20): List<PlaceSuggestionResponse> =
        withContext(Dispatchers.IO) {
            api.adminGetPlaceSuggestions(requireToken(), status, page, size).content
        }

    suspend fun approveSuggestion(id: String): Boolean = withContext(Dispatchers.IO) {
        api.adminApproveSuggestion(requireToken(), id).isSuccessful
    }

    suspend fun rejectSuggestion(id: String, adminNote: String?): Boolean = withContext(Dispatchers.IO) {
        api.adminRejectSuggestion(requireToken(), id, RejectSuggestionRequest(adminNote)).isSuccessful
    }

    // ---- Reports ----
    suspend fun getReports(status: String?, page: Int = 0, size: Int = 20): List<AdminReportResponse> =
        withContext(Dispatchers.IO) {
            api.adminGetReports(requireToken(), status, page, size).content
        }

    suspend fun resolveReport(id: String, action: String, adminNote: String? = null): Boolean =
        withContext(Dispatchers.IO) {
            api.adminResolveReport(requireToken(), id, ResolveReportRequest(action, adminNote)).isSuccessful
        }

    suspend fun deleteReportedPost(id: String): Boolean = withContext(Dispatchers.IO) {
        api.adminDeleteReportedPost(requireToken(), id).isSuccessful
    }

    suspend fun deleteReportedComment(id: String): Boolean = withContext(Dispatchers.IO) {
        api.adminDeleteReportedComment(requireToken(), id).isSuccessful
    }

    // ---- Users ----
    suspend fun banUser(userId: String, reason: String?): Boolean = withContext(Dispatchers.IO) {
        api.adminBanUser(requireToken(), userId, BanUserRequest(reason)).isSuccessful
    }

    suspend fun unbanUser(userId: String): Boolean = withContext(Dispatchers.IO) {
        api.adminUnbanUser(requireToken(), userId).isSuccessful
    }
}
