package com.example.vietnam_travel_itinerary_android.data.dto

// Generic Spring Data Page wrapper
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0
)

// ---- Place Suggestions ----
data class PlaceSuggestionRequest(
    val name: String,
    val provinceId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val type: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

data class PlaceSuggestionResponse(
    val id: String,
    val name: String? = null,
    val provinceName: String? = null,
    val provinceId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val type: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val status: String? = "pending",
    val adminNote: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val userAvatar: String? = null
)

data class RejectSuggestionRequest(
    val adminNote: String? = null
)

// ---- Admin: Reports ----
data class AdminReportResponse(
    val id: String,
    val reason: String? = null,
    val description: String? = null,
    val status: String? = "pending",
    val createdAt: String? = null,
    val reviewedAt: String? = null,
    val reporterId: String? = null,
    val reporterName: String? = null,
    val reporterAvatar: String? = null,
    val reportedPostId: String? = null,
    val reportedPostContent: String? = null,
    val reportedPostAuthorId: String? = null,
    val reportedPostAuthorName: String? = null,
    val reportedPostAuthorAvatar: String? = null,
    val reportedCommentId: String? = null,
    val reportedCommentPostId: String? = null,
    val reportedCommentContent: String? = null,
    val reportedCommentAuthorId: String? = null,
    val reportedCommentAuthorName: String? = null,
    val reportedCommentAuthorAvatar: String? = null,
    val reportedUserId: String? = null,
    val reportedUserName: String? = null,
    val reportedUserAvatar: String? = null
)

data class ResolveReportRequest(
    val action: String,
    val adminNote: String? = null
)

data class BanUserRequest(
    val reason: String? = null
)
