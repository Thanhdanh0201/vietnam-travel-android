package com.example.vietnam_travel_itinerary_android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class PlaceDetail(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    @Json(name = "image_url") val imageUrl: String? = null,
    val rating: Double? = null,
    @Json(name = "review_count") val reviewCount: Int? = null,
    val provinces: ProvinceSummary? = null,
    val cities: CitySummary? = null,
    val reviews: List<PlaceReview> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class PlaceReview(
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_avatar_url") val userAvatarUrl: String? = null,
    val review: String? = null,
    val rating: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "photo_urls") val photoUrls: List<String> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class SubmitPlaceReviewRequest(
    val rating: Int,
    val review: String? = null,
    @Json(name = "photo_urls") val photoUrls: List<String> = emptyList(),
)

fun PlaceDetail.toPlace(): Place = Place(
    id = id,
    name = name,
    type = type,
    lat = lat,
    lng = lng,
    imageUrl = imageUrl,
    rating = rating,
    description = description,
    provinces = provinces,
    cities = cities,
)
