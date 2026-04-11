package com.example.vietnam_travel_itinerary_android.data.model

import java.util.UUID

data class UserSettingResponseDto(
    val id: UUID,
    val pushReactions: Boolean,
    val pushComments: Boolean,
    val pushFollows: Boolean,
    val pushReposts: Boolean,
    val pushMentions: Boolean,
    val pushAchievements: Boolean,
    val language: String,
    val theme: String
)