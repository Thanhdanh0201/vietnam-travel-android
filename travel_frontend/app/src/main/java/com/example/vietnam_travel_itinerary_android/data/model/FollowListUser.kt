package com.example.vietnam_travel_itinerary_android.data.model

data class FollowListUser(
    val id: String,
    val name: String,
    val username: String = "",
    val avatarUrl: String = "",
    val avatarInitials: String = "",
    val avatarColor: Long = 0xFF64748B,
    val isVerified: Boolean = false,
    val explorerLevel: ExplorerLevel = ExplorerLevel.NEWBIE,
)

enum class FollowListType {
    FOLLOWERS,
    FOLLOWING,
}
