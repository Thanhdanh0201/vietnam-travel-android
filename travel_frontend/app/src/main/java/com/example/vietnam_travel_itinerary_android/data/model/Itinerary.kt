package com.example.vietnam_travel_itinerary_android.data.model

data class Itinerary(
    val id: String,
    val title: String,
    val location: String,
    val dateRange: String,
    val statusText: String,
    val statusSubText: String? = null,
    val isUpcoming: Boolean,
    val imageResId: Int, // R.drawable.image
    val participantImages: List<Int>, // Danh sách avatar người tham gia
    val coverUrl: String? = null,
    val status: String? = null,
    val description: String? = null,
    val shareCount: Int = 0,
    val isPublic: Boolean = false,
    val itemCount: Int = 0,
    /** OWNER | EDIT | VIEW - quyền của user hiện tại với itinerary này */
    val myRole: String = "VIEW"
)