package com.example.vietnam_travel_itinerary_android.data.api

import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.PlaceDetail
import com.example.vietnam_travel_itinerary_android.data.model.PlaceReview
import com.example.vietnam_travel_itinerary_android.data.model.Province
import com.example.vietnam_travel_itinerary_android.data.model.SubmitPlaceReviewRequest
import com.example.vietnam_travel_itinerary_android.data.model.TrendingPlace
import com.example.vietnam_travel_itinerary_android.data.model.UserSettingRequest
import com.example.vietnam_travel_itinerary_android.data.model.UserSettingResponseDto
import com.example.vietnam_travel_itinerary_android.data.model.UserSyncRequest
import com.example.vietnam_travel_itinerary_android.data.model.WeatherData
import com.example.vietnam_travel_itinerary_android.data.model.WeatherNearby
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VietnamTravelApi {

    @POST("api/auth/sync")
    suspend fun syncUser(
        @Header("Authorization") token: String,
        @Body request: UserSyncRequest // THÊM DÒNG NÀY VÀO ĐÂY
    ): Response<Unit>

    @GET("api/user-settings/me")
    suspend fun getUserSettings(@Header("Authorization") token: String): Response<UserSettingResponseDto>

    @PUT("api/user-settings/me")
    suspend fun updateUserSettings(
        @Header("Authorization") token: String,
        @Body settings: UserSettingRequest
    ): Response<UserSettingResponseDto>

    // ---- Places ----
    @GET("api/places")
    suspend fun getPlaces(
        @Query("province_code") provinceCode: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Place>

    @GET("api/places/recommended")
    suspend fun getRecommendedPlaces(
        @Query("limit") limit: Int = 10,
    ): List<Place>

    @GET("api/places/trending")
    suspend fun getTrendingPlaces(
        @Query("province_code") provinceCode: String? = null,
        @Query("limit") limit: Int = 10
    ): List<TrendingPlace>

    @GET("api/places/{placeId}")
    suspend fun getPlaceDetail(
        @Path("placeId") placeId: String,
    ): PlaceDetail

    @POST("api/places/{placeId}/reviews")
    suspend fun submitPlaceReview(
        @Header("Authorization") token: String,
        @Path("placeId") placeId: String,
        @Body body: SubmitPlaceReviewRequest,
    ): Response<Unit>

    // ---- Provinces ----
    @GET("api/provinces")
    suspend fun getProvinces(): List<Province>

    @GET("api/provinces/{code}")
    suspend fun getProvince(
        @Path("code") code: String
    ): Province

    @GET("api/provinces/{code}/events")
    suspend fun getEventsByProvince(
        @Path("code") code: String
    ): List<Event>

    /** Lễ hội sắp tới (toàn quốc), cửa sổ theo tháng — khớp bảng events.start_date / end_date. */
    @GET("api/events/upcoming")
    suspend fun getUpcomingEvents(
        @Query("months") months: Int = 3,
        @Query("limit") limit: Int = 40
    ): List<Event>

    @GET("api/provinces/{code}/places")
    suspend fun getPlacesByProvince(
        @Path("code") code: String,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Place>

    // ---- Weather (Open-Meteo qua cache backend — mục 2.4) ----
    @GET("api/weather/featured")
    suspend fun getFeaturedWeather(): List<WeatherNearby>

    @GET("api/weather/nearby")
    suspend fun getWeatherNearby(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): WeatherNearby

    @GET("api/weather/{placeId}")
    suspend fun getWeather(
        @Path("placeId") placeId: String
    ): WeatherData

    @GET("api/weather/{placeId}/forecast")
    suspend fun getWeatherForecast(
        @Path("placeId") placeId: String,
        @Query("days") days: Int = 7
    ): List<WeatherData>

    // ---- Itineraries ----

    @GET("api/itineraries")
    suspend fun getItineraries(): List<Itinerary>

    @POST("api/itineraries")
    suspend fun createItinerary(
        @Body itinerary: Itinerary
    ): Response<Unit>

    @DELETE("api/itineraries/{id}")
    suspend fun deleteItinerary(
        @Path("id") id: String
    ): Response<Unit>
}

