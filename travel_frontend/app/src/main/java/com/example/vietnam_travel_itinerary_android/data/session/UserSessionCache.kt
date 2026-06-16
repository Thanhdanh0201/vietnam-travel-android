package com.example.vietnam_travel_itinerary_android.data.session

import com.example.vietnam_travel_itinerary_android.data.model.UserProfile

object UserSessionCache {
    @Volatile
    private var cachedProfile: UserProfile? = null

    fun get(): UserProfile? = cachedProfile

    fun set(profile: UserProfile) {
        cachedProfile = profile
    }

    fun clear() {
        cachedProfile = null
    }
}
