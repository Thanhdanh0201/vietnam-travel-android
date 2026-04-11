package com.example.vietnam_travel_itinerary_android.data.model

data class UserSettingRequest(
    val pushReactions: Boolean? = null,
    val pushComments: Boolean? = null,
    val pushFollows: Boolean? = null,
    val pushReposts: Boolean? = null,
    val pushMentions: Boolean? = null,
    val pushAchievements: Boolean? = null,
    val language: String? = null,
    val theme: String? = null
)