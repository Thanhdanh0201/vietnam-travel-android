package com.example.vietnam_travel_itinerary_android.data.api

import com.example.vietnam_travel_itinerary_android.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VietnamTravelApi {

    // ---- Places ----
    @GET("places")
    suspend fun getPlaces(
        @Query("province_code") provinceCode: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Place>

    @GET("places/trending")
    suspend fun getTrendingPlaces(
        @Query("province_code") provinceCode: String? = null,
        @Query("limit") limit: Int = 10
    ): List<TrendingPlace>

    @GET("places/{placeId}")
    suspend fun getPlace(
        @Path("placeId") placeId: String
    ): Place

    // ---- Provinces ----
    @GET("provinces")
    suspend fun getProvinces(): List<Province>

    @GET("provinces/{code}")
    suspend fun getProvince(
        @Path("code") code: String
    ): Province

    @GET("provinces/{code}/events")
    suspend fun getEventsByProvince(
        @Path("code") code: String
    ): List<Event>

    @GET("provinces/{code}/places")
    suspend fun getPlacesByProvince(
        @Path("code") code: String,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Place>

    // ---- Weather ----
    @GET("weather/{placeId}")
    suspend fun getWeather(
        @Path("placeId") placeId: String
    ): WeatherData

    @GET("weather/{placeId}/forecast")
    suspend fun getWeatherForecast(
        @Path("placeId") placeId: String,
        @Query("days") days: Int = 7
    ): List<WeatherData>
}
