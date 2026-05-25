package com.example.vietnam_travel_itinerary_android.data.api

import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.data.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VietnamTravelApi {

    @POST("api/auth/sync")
    suspend fun syncUser(
        @Header("Authorization") token: String,
        @Body request: UserSyncRequest // THÊM DÒNG NÀY VÀO ĐÂY
    ): Response<ResponseBody>

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

    // ---- Users ----
    @GET("api/users/{id}")
    suspend fun getProfile(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): UserProfileResponseDto

    // ---- Posts ----
    @GET("api/posts/public")
    suspend fun getPublicFeed(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Header("Authorization") token: String? = null
    ): List<PostResponseBackendDto>

    @GET("api/posts/following")
    suspend fun getFollowingFeed(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Header("Authorization") token: String
    ): List<PostResponseBackendDto>

    @GET("api/posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Header("Authorization") token: String? = null
    ): List<PostResponseBackendDto>

    @GET("api/posts/{postId}")
    suspend fun getPostDetails(
        @Path("postId") postId: String,
        @Header("Authorization") token: String? = null
    ): PostResponseBackendDto

    @POST("api/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: CreatePostRequest
    ): PostResponseBackendDto

    @DELETE("api/posts")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Query("id") postId: String
    ): Response<ResponseBody>

    @GET("api/posts/reactions/check-likes")
    suspend fun checkLikedPosts(
        @Header("Authorization") token: String,
        @Query("post_ids") postIds: List<String>
    ): List<String>

    @POST("api/post_reactions")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Body request: ReactionRequest
    ): Response<ResponseBody>

    @DELETE("api/post_reactions")
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Query("post_id") postId: String
    ): Response<ResponseBody>

    // ---- Reposts ----
    @POST("api/reposts")
    suspend fun repostPost(
        @Header("Authorization") token: String,
        @Body request: RepostRequest
    ): Response<ResponseBody>

    @DELETE("api/reposts")
    suspend fun undoRepost(
        @Header("Authorization") token: String,
        @Query("post_id") postId: String
    ): Response<ResponseBody>

    // ---- Comments ----
    @GET("api/comments_with_author")
    suspend fun getComments(
        @Query("post_id") postId: String? = null,
        @Query("parent_comment_id") parentCommentId: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<CommentResponseBackendDto>

    @GET("api/comment_reactions/check-likes")
    suspend fun checkLikedComments(
        @Header("Authorization") token: String,
        @Query("comment_ids") commentIds: List<String>
    ): List<String>

    @POST("api/comments")
    suspend fun postComment(
        @Header("Authorization") token: String,
        @Body request: CommentRequest
    ): CommentResponseBackendDto

    @POST("api/comment_reactions")
    suspend fun likeComment(
        @Header("Authorization") token: String,
        @Body request: CommentReactionRequest
    ): Response<ResponseBody>

    @DELETE("api/comment_reactions")
    suspend fun unlikeComment(
        @Header("Authorization") token: String,
        @Query("comment_id") commentId: String
    ): Response<ResponseBody>

    // ---- Notifications ----
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): List<NotificationResponseBackendDto>

    @PATCH("api/notifications")
    suspend fun markNotificationsAsRead(
        @Header("Authorization") token: String,
        @Body request: NotificationPatchDto
    ): Response<ResponseBody>

    // ---- Reports ----
    @POST("api/reports")
    suspend fun report(
        @Header("Authorization") token: String,
        @Body request: ReportRequest
    ): Response<ResponseBody>
}

