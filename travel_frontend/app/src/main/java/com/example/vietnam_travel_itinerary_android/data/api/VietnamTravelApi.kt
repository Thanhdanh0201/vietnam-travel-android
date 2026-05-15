package com.example.vietnam_travel_itinerary_android.data.api

import com.example.vietnam_travel_itinerary_android.data.model.*
import retrofit2.Response
import retrofit2.http.Body
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

    @GET("api/places/trending")
    suspend fun getTrendingPlaces(
        @Query("province_code") provinceCode: String? = null,
        @Query("limit") limit: Int = 10
    ): List<TrendingPlace>

    @GET("api/places/{placeId}")
    suspend fun getPlace(
        @Path("placeId") placeId: String
    ): Place

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
    @GET("api/weather/{placeId}")
    suspend fun getWeather(
        @Path("placeId") placeId: String
    ): WeatherData

    @GET("api/weather/{placeId}/forecast")
    suspend fun getWeatherForecast(
        @Path("placeId") placeId: String,
        @Query("days") days: Int = 7
    ): List<WeatherData>
}

