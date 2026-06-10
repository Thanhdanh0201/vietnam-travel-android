package com.example.vietnam_travel_itinerary_android.data.model

// Tương ứng bảng: users (Supabase)
data class UserProfile(
    val id: String,
    val email: String = "",
    val name: String,
    val avatarUrl: String = "",             // avatar_url
    val coverUrl: String = "",              // cover_url
    val bio: String = "",
    val username: String = "",              // @handle — derived/display field
    val websiteUrl: String = "",

    // Gamification
    val totalProvinces: Int = 0,            // total_provinces
    val totalPlacesVisited: Int = 0,        // total_places_visited
    val explorerLevel: ExplorerLevel = ExplorerLevel.NEWBIE, // explorer_level

    // Social stats (denormalized for display)
    val followerCount: Int = 0,             // follower_count
    val followingCount: Int = 0,            // following_count
    val postCount: Int = 0,                 // post_count

    // Flags
    val isVerified: Boolean = false,        // is_verified
    val isPrivate: Boolean = false,         // is_private

    // Timestamps
    val createdAt: String = "",             // created_at
    val lastActiveAt: String = "",          // last_active_at

    // UI-only fields (không có trong DB, computed client-side)
    val avatarInitials: String = "",        // hiển thị khi chưa có avatar_url
    val avatarColor: Long = 0xFF64748B,     // màu fallback avatar
    val isOwnProfile: Boolean = false,
    val isFollowing: Boolean = false,

    // Dữ liệu phụ (load riêng, không trong bảng users)
    val posts: List<CommunityPost> = emptyList(),
    val savedPosts: List<CommunityPost> = emptyList(),
    val publicItineraries: List<LinkedItinerary> = emptyList()   // tab "Lịch trình"
)

enum class ExplorerLevel(val label: String, val minProvinces: Int) {
    NEWBIE("Người mới", 0),
    EXPLORER("Nhà thám hiểm", 5),
    ADVENTURER("Phượt thủ", 20),
    VETERAN("Chiến binh đường xa", 40),
    MASTER("Huyền thoại xuyên Việt", 63);

    companion object {
        fun from(value: String): ExplorerLevel =
            entries.firstOrNull { it.name.lowercase() == value } ?: NEWBIE
    }
}
