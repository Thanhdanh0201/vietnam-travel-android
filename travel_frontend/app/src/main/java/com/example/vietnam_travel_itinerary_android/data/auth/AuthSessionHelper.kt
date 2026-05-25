package com.example.vietnam_travel_itinerary_android.data.auth

import com.example.vietnam_travel_itinerary_android.SupabaseObject
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.model.UserSyncRequest
import io.github.jan.supabase.auth.auth
import retrofit2.HttpException

object AuthSessionHelper {

    suspend fun bearerTokenOrNull(): String? =
        SupabaseObject.client.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }

    /** Đảm bảo user Supabase đã có trong bảng users (Spring) trước khi gọi API cần JWT. */
    suspend fun ensureBackendUserSynced(): Result<String> {
        val user = SupabaseObject.client.auth.currentUserOrNull()
            ?: return Result.failure(NotLoggedInException())
        val token = bearerTokenOrNull()
            ?: return Result.failure(NotLoggedInException())

        val name = user.userMetadata?.get("full_name")?.toString()?.takeIf { it.isNotBlank() }
            ?: user.userMetadata?.get("name")?.toString()?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Khách du lịch"

        return try {
            val response = RetrofitInstance.api.syncUser(
                token = token,
                request = UserSyncRequest(
                    id = user.id,
                    email = user.email.orEmpty(),
                    name = name,
                ),
            )
            if (response.isSuccessful) {
                Result.success(token)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun humanReadableError(throwable: Throwable): String = when (throwable) {
        is NotLoggedInException ->
            "Vui lòng đăng nhập để gửi đánh giá."
        is HttpException -> when (throwable.code()) {
            401, 403 -> "Phiên đăng nhập hết hạn. Đăng xuất rồi đăng nhập lại."
            404 -> "Không tìm thấy địa điểm hoặc tài khoản trên máy chủ."
            else -> "Gửi đánh giá thất bại (${throwable.code()}). Thử lại sau."
        }
        else -> "Gửi đánh giá thất bại. Kiểm tra mạng và thử lại."
    }

    class NotLoggedInException : Exception("User not logged in")
}
