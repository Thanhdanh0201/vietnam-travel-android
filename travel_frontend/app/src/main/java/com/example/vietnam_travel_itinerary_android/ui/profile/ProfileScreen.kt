package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.community.ReportBottomSheet
import com.example.vietnam_travel_itinerary_android.ui.community.ReportTarget
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.AppTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.itinerary.ItineraryCompactCard
import com.example.vietnam_travel_itinerary_android.ui.components.post.CreatePostWidget
import com.example.vietnam_travel_itinerary_android.ui.components.post.PostCard
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
    unreadCount: Int = 0,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    val isOtherUserProfile = userId != null

    when {
        uiState.isLoading -> {
            OtherUserProfileShell(
                isOtherUserProfile = isOtherUserProfile,
                onBack = onBack,
            ) {
                CircularProgressIndicator(color = VNRed)
            }
        }
        uiState.error != null && uiState.profile == null -> {
            OtherUserProfileShell(
                isOtherUserProfile = isOtherUserProfile,
                onBack = onBack,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = { viewModel.loadProfile(userId) },
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        }
        uiState.profile != null -> {
            ProfileContent(
                profile = uiState.profile!!,
                isFollowLoading = uiState.isFollowLoading,
                isRefreshing = uiState.isRefreshing,
                viewModel = viewModel,
                unreadCount = unreadCount,
                onBack = onBack,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick,
                onRefresh = { viewModel.refresh(userId) },
                onToggleFollow = { viewModel.toggleFollow() },
                onLikePost = { postId, liked ->
                    if (liked) viewModel.unlikePost(postId) else viewModel.likePost(postId)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    profile: UserProfile,
    isFollowLoading: Boolean,
    isRefreshing: Boolean,
    viewModel: ProfileViewModel,
    unreadCount: Int,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleFollow: () -> Unit,
    onLikePost: (postId: String, liked: Boolean) -> Unit,
    onMenuClick: (() -> Unit)? = null,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember(profile.isOwnProfile) {
        if (profile.isOwnProfile) {
            listOf("Bài viết", "Câu trả lời", "Lịch trình", "Đã lưu")
        } else {
            listOf("Bài viết", "Câu trả lời", "Lịch trình")
        }
    }
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    val reportContext = LocalContext.current

    val publicItinerariesGrouped = remember(profile.publicItineraries) {
        profile.publicItineraries.filter { it.isPublic }.sortedByStartDateDesc()
    }
    val privateItinerariesGrouped = remember(profile.publicItineraries) {
        profile.publicItineraries.filter { !it.isPublic }.sortedByStartDateDesc()
    }

    if (showReportSheet) {
        ReportBottomSheet(
            target = ReportTarget.USER,
            onDismiss = { showReportSheet = false },
            onSubmit = { reason, description ->
                viewModel.reportUser(reason, description)
                showReportSheet = false
                Toast.makeText(reportContext, "Đã gửi báo cáo. Cảm ơn bạn!", Toast.LENGTH_SHORT).show()
            },
        )
    }

    Scaffold(
        topBar = {
            if (profile.isOwnProfile) {
                AppTopBar(
                    onSearchClick = { onNavigate("search") },
                    onNotificationClick = { onNavigate("notifications") },
                    unreadCount = unreadCount,
                    onMenuClick = onMenuClick,
                )
            } else {
                AppBackTopBar(
                    onBackClick = onBack,
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProfileAvatar(
                                avatarUrl = profile.avatarUrl,
                                initials = profile.avatarInitials,
                                color = Color(profile.avatarColor),
                                size = 32,
                            )
                            Box {
                                IconButton(onClick = { showProfileMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Tùy chọn",
                                        tint = VNRed,
                                    )
                                }
                                DropdownMenu(
                                    expanded = showProfileMenu,
                                    onDismissRequest = { showProfileMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Báo cáo người dùng") },
                                        onClick = {
                                            showProfileMenu = false
                                            showReportSheet = true
                                        },
                                    )
                                }
                            }
                        }
                    },
                )
            }
        },
        containerColor = Color(0xFFF8F6F6),
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            item {
                ProfileHeroSection(
                    profile = profile,
                    isFollowLoading = isFollowLoading,
                    onActionClick = {
                        if (profile.isOwnProfile) onNavigate("edit_profile")
                        else onToggleFollow()
                    },
                    onAvatarClick = if (profile.isOwnProfile) {
                        { onNavigate("edit_profile") }
                    } else null,
                    onFollowersClick = { onNavigate("follow_list/${profile.id}/followers") },
                    onFollowingClick = { onNavigate("follow_list/${profile.id}/following") },
                )
            }

            item {
                ProfileTabRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }

            when (selectedTab) {
                0 -> {
                    if (profile.isOwnProfile) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                CreatePostWidget(
                                    avatarInitials = profile.avatarInitials,
                                    avatarColor = profile.avatarColor,
                                    avatarUrl = profile.avatarUrl,
                                    isPlaceholder = true,
                                    onPlaceholderClick = { onNavigate("community") },
                                )
                            }
                        }
                    }
                    if (profile.posts.isEmpty()) {
                        item { EmptyTabContent("Chưa có bài viết nào.") }
                    } else {
                        items(profile.posts, key = { it.id }) { post ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                PostCard(
                                    post = post,
                                    currentUserId = viewModel.currentUserId,
                                    onLikeClick = { onLikePost(post.id, post.isLiked) },
                                    onCommentClick = {},
                                    onSaveClick = {
                                        if (post.isSaved) viewModel.unsavePost(post.id) else viewModel.savePost(post.id)
                                    },
                                    onDeleteClick = {
                                        postToDelete = post
                                    },
                                    onItineraryClick = { itineraryId -> onNavigate("itinerary_detail/$itineraryId") },
                                    onNavigateToPost = { originalPostId -> onNavigate("post_detail/$originalPostId") }
                                )
                            }
                        }
                    }
                }
                1 -> item { EmptyTabContent("Chưa có câu trả lời nào.") }
                2 -> {
                    if (publicItinerariesGrouped.isEmpty() && privateItinerariesGrouped.isEmpty()) {
                        item { EmptyTabContent("Chưa có lịch trình nào.") }
                    } else {
                        if (publicItinerariesGrouped.isNotEmpty()) {
                            item { ItinerarySectionHeader("Công khai") }
                            items(publicItinerariesGrouped, key = { "public-${it.id}" }) { itinerary ->
                                ProfileItineraryItem(
                                    itinerary = itinerary,
                                    onViewClick = { onNavigate("itinerary_detail/${itinerary.id}") },
                                )
                            }
                        }
                        if (profile.isOwnProfile && privateItinerariesGrouped.isNotEmpty()) {
                            item {
                                ItinerarySectionHeader(
                                    title = "Chỉ mình tôi",
                                    modifier = Modifier.padding(top = if (publicItinerariesGrouped.isNotEmpty()) 8.dp else 0.dp),
                                )
                            }
                            items(privateItinerariesGrouped, key = { "private-${it.id}" }) { itinerary ->
                                ProfileItineraryItem(
                                    itinerary = itinerary,
                                    onViewClick = { onNavigate("itinerary_detail/${itinerary.id}") },
                                )
                            }
                        }
                    }
                }
                3 -> {
                    if (profile.savedPosts.isEmpty()) {
                        item { EmptyTabContent("Chưa có bài viết nào được lưu.") }
                    } else {
                        items(profile.savedPosts, key = { it.id }) { post ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                PostCard(
                                    post = post,
                                    currentUserId = viewModel.currentUserId,
                                    onLikeClick = { onLikePost(post.id, post.isLiked) },
                                    onCommentClick = {},
                                    onSaveClick = {
                                        if (post.isSaved) viewModel.unsavePost(post.id) else viewModel.savePost(post.id)
                                    },
                                    onDeleteClick = {
                                        postToDelete = post
                                    },
                                    onItineraryClick = { itineraryId -> onNavigate("itinerary_detail/$itineraryId") },
                                    onNavigateToPost = { originalPostId -> onNavigate("post_detail/$originalPostId") }
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        postToDelete?.let { post ->
            AlertDialog(
                onDismissRequest = { postToDelete = null },
                title = { Text("Xoá bài viết", fontWeight = FontWeight.Bold) },
                text = { Text("Bạn có chắc chắn muốn xoá bài viết này không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePost(post.id)
                            postToDelete = null
                        }
                    ) {
                        Text("Xoá", color = VNRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { postToDelete = null }) {
                        Text("Huỷ", color = SlateGray500)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtherUserProfileShell(
    isOtherUserProfile: Boolean,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    if (isOtherUserProfile) {
        Scaffold(
            topBar = { AppBackTopBar(onBackClick = onBack) },
            containerColor = Color(0xFFF8F6F6),
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
                content = content,
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@Composable
private fun ProfileHeroSection(
    profile: UserProfile,
    isFollowLoading: Boolean,
    onActionClick: () -> Unit,
    onAvatarClick: (() -> Unit)? = null,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp, top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (profile.isOwnProfile && onAvatarClick != null) {
            EditableProfileAvatar(
                avatarUrl = profile.avatarUrl,
                initials = profile.avatarInitials,
                color = Color(profile.avatarColor),
                size = 80,
                onClick = onAvatarClick,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = VNRed.copy(alpha = 0.2f),
                        spotColor = VNRed.copy(alpha = 0.2f),
                    )
                    .clip(CircleShape)
                    .border(3.dp, VNRed, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                ProfileAvatar(
                    avatarUrl = profile.avatarUrl,
                    initials = profile.avatarInitials,
                    color = Color(profile.avatarColor),
                    size = 70,
                    textSize = 26.sp,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = profile.name,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = (-0.6).sp,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
        )

        if (profile.username.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = profile.username,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
            )
        }

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
                modifier = Modifier.widthIn(max = 280.dp),
            )
        }

        if (profile.websiteUrl.isNotBlank()) {
            Spacer(Modifier.height(7.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { },
            ) {
                Icon(Icons.Outlined.Link, null, tint = VNRed, modifier = Modifier.size(14.dp))
                Text(
                    text = profile.websiteUrl,
                    fontSize = 13.sp,
                    color = VNRed,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(value = profile.postCount, label = "BÀI VIẾT")
            StatItem(
                value = profile.followerCount,
                label = "NGƯỜI THEO DÕI",
                onClick = onFollowersClick,
            )
            StatItem(
                value = profile.followingCount,
                label = "ĐANG THEO DÕI",
                onClick = onFollowingClick,
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onActionClick,
            enabled = !isFollowLoading,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(40.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = VNRed.copy(alpha = 0.3f),
                    spotColor = VNRed.copy(alpha = 0.3f),
                ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VNRed),
        ) {
            if (isFollowLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = when {
                        profile.isOwnProfile -> "Chỉnh sửa trang cá nhân"
                        profile.isFollowing -> "Đang theo dõi"
                        else -> "Theo dõi"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    onClick: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        },
    ) {
        Text(
            text = formatCount(value),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            color = Color(0xFF0F172A),
        )
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            letterSpacing = 1.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatCount(count: Int): String = when {
    count >= 1000 -> "${"%.1f".format(count / 1000.0)}K"
    else -> count.toString()
}

@Composable
private fun ProfileTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .clickable { onTabSelected(index) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Text(
                            text = tab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) VNRed else Color(0xFF94A3B8),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(VNRed.copy(alpha = 0.05f)),
            )
        }
    }
}

@Composable
private fun ProfileItineraryItem(
    itinerary: LinkedItinerary,
    onViewClick: () -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        ItineraryCompactCard(
            itinerary = itinerary,
            onViewClick = onViewClick,
        )
    }
}

@Composable
private fun ItinerarySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFF475569),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}

private fun List<LinkedItinerary>.sortedByStartDateDesc(): List<LinkedItinerary> {
    return sortedWith(
        compareByDescending<LinkedItinerary> { it.sortableStartDate() }
            .thenBy { it.title.lowercase() },
    )
}

private fun LinkedItinerary.sortableStartDate(): java.time.LocalDate {
    if (startDate.isNullOrBlank()) return java.time.LocalDate.MIN
    return try {
        java.time.LocalDate.parse(startDate)
    } catch (_: Exception) {
        java.time.LocalDate.MIN
    }
}

@Composable
private fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("📭", fontSize = 36.sp)
            Text(message, color = Color(0xFF94A3B8), fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
