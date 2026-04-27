package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.ui.community.ScreenHeader
import com.example.vietnam_travel_itinerary_android.ui.components.AppTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.itinerary.ItineraryCompactCard
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.components.post.CreatePostWidget
import com.example.vietnam_travel_itinerary_android.ui.components.post.PostCard
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ── Mock Data
private val mockPublicItineraries = listOf(
    LinkedItinerary(
        id = "it1", title = "Hành trình Vịnh Hạ Long 3 ngày",
        stopCount = 8, location = "Quảng Ninh", durationDays = 3,
        isPublic = true, likeCount = 47,
        coverImageKey = "halong_bay",
        authorName = "Danh Nguyen", authorAvatarInitials = "DN", authorAvatarColor = 0xFFC6102E,
        timeAgo = "1 tuần trước"
    ),
    LinkedItinerary(
        id = "it2", title = "Phố cổ Hội An & Mỹ Sơn",
        stopCount = 6, location = "Quảng Nam", durationDays = 2,
        isPublic = true, likeCount = 63,
        coverImageKey = "hoian_lantern",
        authorName = "Danh Nguyen", authorAvatarInitials = "DN", authorAvatarColor = 0xFFC6102E,
        timeAgo = "2 tuần trước"
    ),
    LinkedItinerary(
        id = "it3", title = "Chinh phục Sapa & Fansipan",
        stopCount = 5, location = "Lào Cai", durationDays = 3,
        isPublic = false, likeCount = 29,
        coverImageKey = "sapa",
        authorName = "Danh Nguyen", authorAvatarInitials = "DN", authorAvatarColor = 0xFFC6102E,
        timeAgo = "1 tháng trước"
    )
)

private val mockOwnProfile = UserProfile(
    id = "u1", name = "Danh Nguyen",
    email = "danh@vietnam-travel.app",
    username = "@danhnguyen.o2o1",
    bio = "Khám phá vẻ đẹp tiềm ẩn của Việt Nam qua từng bước chân và khung hình.",
    websiteUrl = "vietnam-travel.app",
    avatarInitials = "DN", avatarColor = 0xFFC6102E,
    postCount = 24, followerCount = 1280, followingCount = 348,
    totalProvinces = 12, totalPlacesVisited = 38,
    explorerLevel = ExplorerLevel.ADVENTURER,
    isVerified = false, isPrivate = false,
    isOwnProfile = true,
    posts = listOf(
        CommunityPost(
            id = "p1", authorName = "Danh Nguyen", authorAvatarInitials = "DN",
            authorAvatarColor = 0xFFC6102E, timeAgo = "2 GIỜ TRƯỚC", postType = "original",
            content = "Hoàng hôn rực rỡ tại Vịnh Hạ Long chiều nay. Cảm giác thật bình yên khi được hòa mình vào thiên nhiên hùng vĩ của quê hương. 🇻🇳",
            media = listOf(PostMedia("m1", "halong_bay")),
            likeCount = 47, commentCount = 12, repostCount = 5, isLiked = false,
            linkedItinerary = mockPublicItineraries[0]
        ),
        CommunityPost(
            id = "p2", authorName = "Danh Nguyen", authorAvatarInitials = "DN",
            authorAvatarColor = 0xFFC6102E, timeAgo = "5 GIỜ TRƯỚC", postType = "original",
            content = "Dạo quanh phố cổ Hội An, góc nào cũng thấy đẹp và tình. Rất đáng để ghé thăm trong mùa hè này!",
            media = listOf(PostMedia("m2", "hoian_lantern"), PostMedia("m3", "hoian_food")),
            likeCount = 63, commentCount = 22, repostCount = 8, isLiked = true
        )
    ),
    publicItineraries = mockPublicItineraries
)

private val mockOtherProfile = UserProfile(
    id = "u2", name = "Lan Anh",
    username = "@lananh.travel",
    bio = "Mê mải khám phá từng góc đẹp của dải đất hình chữ S. 📸✈️",
    avatarInitials = "LA", avatarColor = 0xFF7C3AED,
    followerCount = 5420, followingCount = 212, postCount = 89,
    totalProvinces = 30, totalPlacesVisited = 95,
    explorerLevel = ExplorerLevel.VETERAN,
    isOwnProfile = false, isFollowing = false
)


// ============================================================
// PROFILE SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile = mockOwnProfile,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var isFollowing by remember { mutableStateOf(profile.isFollowing) }
    var followerCount by remember { mutableIntStateOf(profile.followerCount) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Bài viết", "Câu trả lời", "Lịch trình")

    Scaffold(
        topBar = {
            if (profile.isOwnProfile) {
                // Own profile — dùng AppTopBar chung
                Column {
                    AppTopBar(
                        onSearchClick = { onNavigate("search") },
                        onNotificationClick = { onNavigate("notifications") }
                    )
                    HorizontalDivider(color = VNRed.copy(alpha = 0.05f))
                }
            } else {
                // Other user — Back + title + avatar
                OtherUserTopBar(profile = profile, onBack = onBack)
            }
        },
        containerColor = Color(0xFFF8F6F6)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Hero Section
            item {
                ProfileHeroSection(
                    profile = profile,
                    isFollowing = isFollowing,
                    followerCount = followerCount,
                    onActionClick = {
                        if (profile.isOwnProfile) onNavigate("edit_profile")
                        else {
                            isFollowing = !isFollowing
                            followerCount = if (isFollowing) followerCount + 1 else followerCount - 1
                        }
                    }
                )
            }

            // ── Tab Row
            item {
                ProfileTabRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // ── Feed (chỉ hiện Bài viết)
            when (selectedTab) {
                0 -> {
                    // Create post widget (chỉ own profile)
                    if (profile.isOwnProfile) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                CreatePostWidget(
                                    avatarInitials = profile.avatarInitials,
                                    avatarColor = profile.avatarColor,
                                    onPost = { onNavigate("create_post") }
                                )
                            }
                        }
                    }
                    if (profile.posts.isEmpty()) {
                        item { EmptyTabContent("Chưa có bài viết nào.") }
                    } else {
                        items(profile.posts, key = { it.id }) { post ->
                            var liked by remember(post.id) { mutableStateOf(post.isLiked) }
                            var likeCount by remember(post.id) { mutableIntStateOf(post.likeCount) }
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                PostCard(
                                    post = post.copy(isLiked = liked, likeCount = likeCount),
                                    onLikeClick = {
                                        liked = !liked
                                        likeCount = if (liked) likeCount + 1 else likeCount - 1
                                    },
                                    onCommentClick = {}
                                )
                            }
                        }
                    }
                }
                1 -> item { EmptyTabContent("Chưa có câu trả lời nào.") }
                2 -> {
                    // ── Lịch trình công khai — dùng chung ItineraryCompactCard
                    val itineraries = profile.publicItineraries
                    if (itineraries.isEmpty()) {
                        item { EmptyTabContent("Chưa có lịch trình nào được chia sẻ.") }
                    } else {
                        items(itineraries, key = { it.id }) { itinerary ->
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Badge riêng tư (chỉ owner mới thấy item riêng tư)
                                if (!itinerary.isPublic && profile.isOwnProfile) {
                                    Text(
                                        text = "🔒 Chỉ mình tôi",
                                        fontSize = 11.sp,
                                        color = SlateGray400,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                    )
                                }
                                ItineraryCompactCard(
                                    itinerary = itinerary,
                                    onViewClick = { onNavigate("itinerary_detail/${itinerary.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Other User TopBar — back + title + small avatar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtherUserTopBar(profile: UserProfile, onBack: () -> Unit) {
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xCCF8F6F6), // rgba(248,246,246,0.8)
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, "Quay lại",
                            tint = VNRed, modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Hồ sơ",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp,
                        color = VNRed
                    )
                }
                // Small avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(profile.avatarColor))
                        .border(2.dp, VNRed.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.avatarInitials, color = Color.White,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        HorizontalDivider(color = VNRed.copy(alpha = 0.05f))
    }
}

// ── Hero Section
@Composable
private fun ProfileHeroSection(
    profile: UserProfile,
    isFollowing: Boolean,
    followerCount: Int,
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp, top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Avatar with red ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = VNRed.copy(alpha = 0.2f),
                    spotColor = VNRed.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .border(3.dp, VNRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(profile.avatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile.avatarInitials, color = Color.White,
                    fontSize = 26.sp, fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Name
        Text(
            text = profile.name,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = (-0.6).sp,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(2.dp))

        // ── Username
        Text(
            text = profile.username,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        // ── Bio
        if (profile.bio.isNotBlank()) {
            Spacer(Modifier.height(11.dp))
            Text(
                text = profile.bio,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }

        // ── Website link
        if (profile.websiteUrl.isNotBlank()) {
            Spacer(Modifier.height(7.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { }
            ) {
                Icon(Icons.Outlined.Link, null, tint = VNRed, modifier = Modifier.size(14.dp))
                Text(
                    text = profile.websiteUrl,
                    fontSize = 13.sp,
                    color = VNRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Stats: Posts | Followers | Following
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(value = profile.postCount, label = "BÀI VIẾT")
            StatItem(value = followerCount, label = "NGƯỜI THEO DÕI")
            StatItem(value = profile.followingCount, label = "ĐANG THEO DÕI")
        }

        Spacer(Modifier.height(16.dp))

        // ── Action button
        Button(
            onClick = onActionClick,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(40.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = VNRed.copy(alpha = 0.3f),
                    spotColor = VNRed.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VNRed)
        ) {
            Text(
                text = if (profile.isOwnProfile) "Chỉnh sửa trang cá nhân"
                       else if (isFollowing) "Đang theo dõi" else "Theo dõi",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

// ── Stat item (count + label)
@Composable
private fun StatItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatCount(value),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            color = Color(0xFF0F172A)
        )
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            letterSpacing = 1.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCount(count: Int): String = when {
    count >= 1000 -> "${"%.1f".format(count / 1000.0)}K"
    else -> count.toString()
}

// ── Tab Row
@Composable
private fun ProfileTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .clickable { onTabSelected(index) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = tab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) VNRed else Color(0xFF94A3B8)
                        )
                    }
                }
            }
            // Bottom border
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(VNRed.copy(alpha = 0.05f)))
        }
    }
}


// ── Empty state for tabs
@Composable
private fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📭", fontSize = 36.sp)
            Text(message, color = Color(0xFF94A3B8), fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
