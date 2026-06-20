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
    data class AdminActionResult(
        val success: Boolean,
        val errorMessage: String? = null,
    )

    private val api = RetrofitInstance.api

    private fun requireToken(): String =
        supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            ?: throw IllegalStateException("Not authenticated")

    private fun parseError(code: Int, body: String?): String = when (code) {
        401 -> "Phiên đăng nhập hết hạn (401)."
        403 -> "Không có quyền admin (403)."
        404 -> "Không tìm thấy báo cáo hoặc bài viết (404)."
        else -> body?.takeIf { it.isNotBlank() } ?: "Lỗi API ($code)."
    }

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

    suspend fun resolveReport(id: String, action: String, adminNote: String? = null): AdminActionResult =
        withContext(Dispatchers.IO) {
            try {
                val response = api.adminResolveReport(requireToken(), id, ResolveReportRequest(action, adminNote))
                if (response.isSuccessful) {
                    AdminActionResult(success = true)
                } else {
                    val body = response.errorBody()?.string()
                    android.util.Log.e("AdminRepo", "resolveReport failed: ${response.code()} $body")
                    AdminActionResult(success = false, errorMessage = parseError(response.code(), body))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AdminActionResult(success = false, errorMessage = e.message ?: "Lỗi mạng.")
            }
        }

    suspend fun deleteReportedPost(id: String): AdminActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.adminDeleteReportedPost(requireToken(), id)
            if (response.isSuccessful) {
                AdminActionResult(success = true)
            } else {
                val body = response.errorBody()?.string()
                android.util.Log.e("AdminRepo", "deleteReportedPost failed: ${response.code()} $body")
                AdminActionResult(success = false, errorMessage = parseError(response.code(), body))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AdminActionResult(success = false, errorMessage = e.message ?: "Lỗi mạng.")
        }
    }

    suspend fun deleteReportedComment(id: String): AdminActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.adminDeleteReportedComment(requireToken(), id)
            if (response.isSuccessful) {
                AdminActionResult(success = true)
            } else {
                val body = response.errorBody()?.string()
                android.util.Log.e("AdminRepo", "deleteReportedComment failed: ${response.code()} $body")
                AdminActionResult(success = false, errorMessage = parseError(response.code(), body))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AdminActionResult(success = false, errorMessage = e.message ?: "Lỗi mạng.")
        }
    }

    // ---- Users ----
    suspend fun banUser(userId: String, reason: String?): Boolean = withContext(Dispatchers.IO) {
        api.adminBanUser(requireToken(), userId, BanUserRequest(reason)).isSuccessful
    }

    suspend fun unbanUser(userId: String): Boolean = withContext(Dispatchers.IO) {
        api.adminUnbanUser(requireToken(), userId).isSuccessful
    }
}
