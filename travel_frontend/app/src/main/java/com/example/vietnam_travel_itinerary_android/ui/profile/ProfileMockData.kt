package com.example.vietnam_travel_itinerary_android.ui.profile

import com.example.vietnam_travel_itinerary_android.data.model.CommunityPost
import com.example.vietnam_travel_itinerary_android.data.model.ExplorerLevel
import com.example.vietnam_travel_itinerary_android.data.model.PostMedia
import com.example.vietnam_travel_itinerary_android.data.model.UserProfile

object ProfileMockData {
    const val MOCK_OTHER_USER_ID = "mock-user-lan-anh"

    fun isMockUserId(userId: String?): Boolean = userId == MOCK_OTHER_USER_ID

    fun mockOtherUserProfile(currentUserId: String?): UserProfile = UserProfile(
        id = MOCK_OTHER_USER_ID,
        name = "Lan Anh",
        username = "@lananh.travel",
        bio = "Mê mải khám phá từng góc đẹp của dải đất hình chữ S. 📸✈️",
        avatarInitials = "LA",
        avatarColor = 0xFF7C3AED,
        followerCount = 5420,
        followingCount = 212,
        postCount = 2,
        totalProvinces = 30,
        totalPlacesVisited = 95,
        explorerLevel = ExplorerLevel.VETERAN,
        isVerified = false,
        isPrivate = false,
        isOwnProfile = false,
        isFollowing = false,
        posts = listOf(
            CommunityPost(
                id = "mock-p1",
                userId = MOCK_OTHER_USER_ID,
                authorName = "Lan Anh",
                authorAvatarInitials = "LA",
                authorAvatarColor = 0xFF7C3AED,
                timeAgo = "5 GIỜ TRƯỚC",
                content = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. Đã tìm thấy một con hẻm bí mật có món Cao Lầu ngon nhất! 🍜",
                media = listOf(
                    PostMedia("m2", "hoian_lantern"),
                    PostMedia("m3", "hoian_food"),
                ),
                likeCount = 63,
                commentCount = 22,
                repostCount = 8,
                isLiked = false,
            ),
            CommunityPost(
                id = "mock-p2",
                userId = MOCK_OTHER_USER_ID,
                authorName = "Lan Anh",
                authorAvatarInitials = "LA",
                authorAvatarColor = 0xFF7C3AED,
                timeAgo = "2 NGÀY TRƯỚC",
                content = "Sapa mùa này lạnh thật sự nhưng cảnh đẹp không tưởng. Ai đi cùng mình không? 🏔️",
                media = listOf(PostMedia("m4", "sapa")),
                likeCount = 41,
                commentCount = 9,
                repostCount = 3,
                isLiked = true,
            ),
        ),
    )
}
