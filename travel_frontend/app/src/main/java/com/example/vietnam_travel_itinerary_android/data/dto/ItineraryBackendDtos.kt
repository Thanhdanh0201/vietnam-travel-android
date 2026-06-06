package com.example.vietnam_travel_itinerary_android.data.dto

// Response structure for Itinerary from Spring Boot backend
data class ItineraryResponseDto(
    val id: String,
    val title: String,
    val location: String? = null,
    val description: String? = null,
    val coverUrl: String? = null,
    val startDate: String? = null, // yyyy-MM-dd
    val endDate: String? = null,   // yyyy-MM-dd
    val shareCount: Int? = 0,
    val itemCount: Int? = 0,
    val createdAt: String? = null  // ISO DateTime
)

// Request body for creating an itinerary
data class CreateItineraryRequest(
    val title: String,
    val location: String? = null,
    val startDate: String? = null, // yyyy-MM-dd
    val endDate: String? = null,   // yyyy-MM-dd
    val description: String? = null,
    val coverUrl: String? = null,
    val isPublic: Boolean = false
)

// Response structure for an Itinerary Item from backend
data class ItineraryItemResponseDto(
    val id: String,
    val placeId: String,
    val placeName: String,
    val imageUrl: String? = null,
    val tag: String? = null,
    val location: String? = null,
    val scheduledTime: String? = null, // HH:mm:ss
    val day: String? = null,
    val note: String? = null,
    val orderIndex: Int? = 0
)

// Request body for adding an itinerary item
data class CreateItineraryItemRequest(
    val placeId: String,
    val scheduledTime: String, // HH:mm:ss
    val day: String,
    val note: String? = null,
    val orderIndex: Int = 0
)
