package com.example.vietnam_travel_itinerary_android.data.auth

/** @see AuthSessionHelper */
object AuthTokenProvider {
    suspend fun bearerTokenOrNull(): String? = AuthSessionHelper.bearerTokenOrNull()
}
