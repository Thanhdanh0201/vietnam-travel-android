package com.example.vietnam_travel_itinerary_android.data.api

import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.data.dto.*
import okhttp3.ResponseBody
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
import com.example.vietnam_travel_itinerary_android.ui.itinerary.UpdateItineraryRequest
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

    // ---- Search ----
    @GET("api/search/trending")
    suspend fun getTrendingKeywords(
        @Query("limit") limit: Int = 10
    ): List<String>

    @POST("api/search/log")
    suspend fun logSearchKeyword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    // ---- Places ----
    @GET("api/places/search")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): List<Place>

    @GET("api/places")
    suspend fun getPlaces(
        @Query("province_code") provinceCode: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
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

    @GET("api/provinces/search")
    suspend fun searchProvinces(
        @Query("q") query: String,
        @Query("limit") limit: Int = 8,
    ): List<Province>

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
        @Query("limit") limit: Int = 40,
        @Query("offset") offset: Int = 0
    ): List<Event>

    /** Toàn bộ lễ hội — dùng cho màn Xem tất cả. */
    @GET("api/events")
    suspend fun getAllEvents(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
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
        @Header("Authorization") token: String? = null
    ): UserProfileResponseDto

    @PATCH("api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): UserProfileResponseDto

    // ---- Follows ----
    @POST("api/follows/{followingId}")
    suspend fun followUser(
        @Header("Authorization") token: String,
        @Path("followingId") followingId: String
    ): Response<ResponseBody>

    @DELETE("api/follows/{followingId}")
    suspend fun unfollowUser(
        @Header("Authorization") token: String,
        @Path("followingId") followingId: String
    ): Response<ResponseBody>

    @GET("api/follows/check/{userId}")
    suspend fun checkIsFollowing(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Boolean

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

    @POST("api/saved_posts")
    suspend fun savePost(
        @Header("Authorization") token: String,
        @Query("post_id") postId: String
    ): Response<ResponseBody>

    @DELETE("api/saved_posts")
    suspend fun unsavePost(
        @Header("Authorization") token: String,
        @Query("post_id") postId: String
    ): Response<ResponseBody>

    @GET("api/saved_posts/check-saved")
    suspend fun checkSavedPosts(
        @Header("Authorization") token: String,
        @Query("post_ids") postIds: List<String>
    ): List<String>

    @GET("api/saved_posts")
    suspend fun getSavedPosts(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<PostResponseBackendDto>

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
        @Query("offset") offset: Int = 0,
        @Query("category") category: String? = null
    ): List<NotificationResponseBackendDto>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadNotificationCount(
        @Header("Authorization") token: String
    ): UnreadCountDto

    @PATCH("api/notifications/{id}")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Path("id") notifId: String,
        @Body request: NotificationPatchDto
    ): Response<ResponseBody>

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

    // ---- Place Suggestions (user) ----
    @POST("api/place-suggestions")
    suspend fun createPlaceSuggestion(
        @Header("Authorization") token: String,
        @Body request: PlaceSuggestionRequest
    ): PlaceSuggestionResponse

    @GET("api/place-suggestions/my")
    suspend fun getMyPlaceSuggestions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageDto<PlaceSuggestionResponse>

    // ---- Admin: Place Suggestions ----
    @GET("api/admin/place-suggestions")
    suspend fun adminGetPlaceSuggestions(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageDto<PlaceSuggestionResponse>

    @PATCH("api/admin/place-suggestions/{id}/approve")
    suspend fun adminApproveSuggestion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @PATCH("api/admin/place-suggestions/{id}/reject")
    suspend fun adminRejectSuggestion(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: RejectSuggestionRequest
    ): Response<ResponseBody>

    // ---- Admin: Reports ----
    @GET("api/admin/reports")
    suspend fun adminGetReports(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageDto<AdminReportResponse>

    @PATCH("api/admin/reports/{id}")
    suspend fun adminResolveReport(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ResolveReportRequest
    ): Response<ResponseBody>

    @DELETE("api/admin/reports/{id}/post")
    suspend fun adminDeleteReportedPost(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @DELETE("api/admin/reports/{id}/comment")
    suspend fun adminDeleteReportedComment(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    // ---- Admin: Users ----
    @POST("api/admin/users/{id}/ban")
    suspend fun adminBanUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: BanUserRequest
    ): Response<ResponseBody>

    @POST("api/admin/users/{id}/unban")
    suspend fun adminUnbanUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    // ---- Itineraries ----

    @GET("api/itineraries")
    suspend fun getPublicItinerariesByUser(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String,
        @Query("is_public") isPublic: Boolean = true,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): List<ItineraryResponseDto>

    @GET("api/itineraries")
    suspend fun getItineraries(): List<Itinerary>

    @PATCH("api/itineraries")
    suspend fun updateItinerary(
        @Header("Authorization") token: String,
        @Query("id") id: String,
        @Body request: UpdateItineraryRequest
    ): Response<Unit>

    @GET("api/itineraries/{id}")
    suspend fun getItineraryById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ItineraryResponseDto

    @GET("api/itineraries/me")
    suspend fun getMyItineraries(
        @Header("Authorization") token: String
    ): List<ItineraryResponseDto>

    @POST("api/itineraries")
    suspend fun createItinerary(
        @Header("Authorization") token: String,
        @Body request: CreateItineraryRequest
    ): ItineraryResponseDto

    @DELETE("api/itineraries")
    suspend fun deleteItinerary(
        @Header("Authorization") token: String,
        @Query("id") id: String
    ): Response<okhttp3.ResponseBody>

    @GET("api/itineraries/{id}/items")
    suspend fun getItineraryItems(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): List<ItineraryItemResponseDto>

    @POST("api/itineraries/{id}/items")
    suspend fun addItineraryItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CreateItineraryItemRequest
    ): ItineraryItemResponseDto

    @DELETE("api/itineraries/{id}/items/{itemId}")
    suspend fun deleteItineraryItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("itemId") itemId: String
    ): Response<okhttp3.ResponseBody>

    @PATCH("api/itineraries/{id}/items/{itemId}")
    suspend fun updateItineraryItemNote(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItineraryItemNoteRequest
    ): ItineraryItemResponseDto

    @GET("api/itineraries/{id}/notes")
    suspend fun getItineraryNotes(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("item_id") itemId: String? = null
    ): List<ItineraryNoteDto>

    @POST("api/itineraries/{id}/notes")
    suspend fun addItineraryNote(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CreateItineraryNoteRequest
    ): ItineraryNoteDto

    @DELETE("api/itineraries/{id}/notes/{noteId}")
    suspend fun deleteItineraryNote(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("noteId") noteId: String
    ): Response<okhttp3.ResponseBody>

    @GET("api/itineraries/{id}/collaborators")
    suspend fun getCollaborators(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): List<CollaboratorDto>

    @POST("api/itineraries/{id}/collaborators")
    suspend fun addCollaborator(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CollaboratorDto
    ): CollaboratorDto

    @POST("api/itineraries/{id}/invites/accept")
    suspend fun acceptItineraryInvite(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @POST("api/itineraries/{id}/invites/decline")
    suspend fun declineItineraryInvite(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @DELETE("api/itineraries/{id}/collaborators/{email}")
    suspend fun removeCollaborator(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("email") email: String
    ): Response<okhttp3.ResponseBody>

    @GET("api/provinces/{code}/cities")
    suspend fun getCitiesByProvince(
        @Path("code") code: String
    ): List<CityDto>
}

