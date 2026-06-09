package com.example.vietnam_travel_itinerary_android.ui.community

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.ui.components.AppTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.post.CreatePostWidget
import com.example.vietnam_travel_itinerary_android.ui.components.post.PostCard
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ── Mock Comments
private fun mockComments(postId: String) = listOf(
    Comment(
        id = "c1", postId = postId,
        authorName = "Hồng Nhung", authorAvatarInitials = "HN", authorAvatarColor = 0xFFDB2777,
        timeAgo = "1 giờ trước", content = "Đẹp quá bạn ơi! Mình cũng muốn đi lắm 😍",
        reactionCount = 5, isLiked = true,
        replies = listOf(
            Comment(
                id = "r1", postId = postId, parentCommentId = "c1",
                authorName = "Minh Tuấn", authorAvatarInitials = "MT", authorAvatarColor = 0xFF2563EB,
                timeAgo = "45 phút trước", content = "Đi đi bạn! Tháng 10 thời tiết rất đẹp 🌤️",
                reactionCount = 2, replyCount = 0
            )
        ), replyCount = 1
    ),
    Comment(
        id = "c2", postId = postId,
        authorName = "Văn Hùng", authorAvatarInitials = "VH", authorAvatarColor = 0xFF0891B2,
        timeAgo = "30 phút trước",
        content = "Chi phí mình đi 3 ngày hết khoảng 2.5 triệu bạn nhé, rất hợp lý!",
        reactionCount = 8, isLiked = false, replyCount = 0
    ),
    Comment(
        id = "c3", postId = postId,
        authorName = "Mai Linh", authorAvatarInitials = "ML", authorAvatarColor = 0xFF7C3AED,
        timeAgo = "10 phút trước", content = "Bạn ơi cho mình xin lịch trình với! 🙏",
        reactionCount = 1, isLiked = false, replyCount = 0
    )
)

// ── Mock Posts (5 trường hợp)
private val mockPosts = listOf(
    CommunityPost(
        id = "1", authorName = "Minh Tuấn", authorAvatarInitials = "MT",
        authorAvatarColor = 0xFF2563EB, timeAgo = "2 GIỜ TRƯỚC", postType = "original",
        content = "Thức dậy trong làn sương mù trên Vịnh Hạ Long là một trải nghiệm tôi sẽ không bao giờ quên. 🛶✨",
        media = listOf(PostMedia("m1", "halong_bay")),
        likeCount = 24, commentCount = 8, repostCount = 3, isLiked = false,
        linkedItinerary = LinkedItinerary("it_1", "Hành trình 3 ngày Ven biển", 12),
        comments = mockComments("1")
    ),
    CommunityPost(
        id = "2", authorName = "Lan Anh", authorAvatarInitials = "LA",
        authorAvatarColor = 0xFF7C3AED, timeAgo = "5 GIỜ TRƯỚC", postType = "original",
        content = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. Đã tìm thấy một con hẻm bí mật có món Cao Lầu ngon nhất! 🍜",
        media = listOf(PostMedia("m2", "hoian_lantern"), PostMedia("m3", "hoian_food")),
        likeCount = 47, commentCount = 15, repostCount = 11, isLiked = true,
        comments = mockComments("2")
    ),
    CommunityPost(
        id = "3", authorName = "Quốc Bảo Trần", authorAvatarInitials = "QB",
        authorAvatarColor = 0xFF059669, timeAgo = "3 GIỜ TRƯỚC", postType = "repost",
        content = "", likeCount = 18, commentCount = 4, repostCount = 2, isLiked = false,
        embeddedPost = EmbeddedPost(
            originalPostId = "2", originalAuthorName = "Lan Anh",
            originalAuthorInitials = "LA", originalAuthorColor = 0xFF7C3AED,
            originalContent = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. 🍜",
            originalMedia = listOf(PostMedia("m2", "hoian_lantern")),
            originalTimeAgo = "5 GIỜ TRƯỚC"
        ),
        comments = mockComments("3")
    ),
    CommunityPost(
        id = "4", authorName = "Thu Hà", authorAvatarInitials = "TH",
        authorAvatarColor = 0xFFEA580C, timeAgo = "1 NGÀY TRƯỚC", postType = "quote",
        content = "Ai chưa đi Hội An thì nhất định phải thử! Mình đã đi 3 lần rồi vẫn muốn quay lại 🥰",
        likeCount = 63, commentCount = 22, repostCount = 5, isLiked = true,
        embeddedPost = EmbeddedPost(
            originalPostId = "2", originalAuthorName = "Lan Anh",
            originalAuthorInitials = "LA", originalAuthorColor = 0xFF7C3AED,
            originalContent = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. 🍜",
            originalTimeAgo = "1 NGÀY TRƯỚC"
        ),
        comments = mockComments("4")
    ),
    CommunityPost(
        id = "5", authorName = "Quốc Bảo Trần", authorAvatarInitials = "QB",
        authorAvatarColor = 0xFF059669, timeAgo = "2 NGÀY TRƯỚC", postType = "original",
        content = "Vừa kết thúc chuyến đi xuyên Việt 2 tuần. Việt Nam ơi, bạn đã chiếm trọn trái tim tôi! 🇻🇳❤️",
        likeCount = 132, commentCount = 34, repostCount = 28, isLiked = false,
        linkedItinerary = LinkedItinerary("it_2", "Hành trình Xuyên Việt", 21),
        comments = mockComments("5")
    )
)

// ============================================================
// COMMUNITY SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigate: (String) -> Unit = {},
    shareItineraryId: String? = null,
    itineraryViewModel: com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel? = null,
    viewModel: CommunityViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var postText by remember { mutableStateOf("") }
    var activeShareItineraryId by remember(shareItineraryId) { mutableStateOf(shareItineraryId) }

    val sharedItinerary = remember(activeShareItineraryId, itineraryViewModel) {
        if (itineraryViewModel != null && activeShareItineraryId != null) {
            itineraryViewModel.uiState.value.itineraries.find { it.id == activeShareItineraryId }
        } else null
    }

    val linkedItinerary = remember(sharedItinerary) {
        sharedItinerary?.let {
            LinkedItinerary(
                id = it.id,
                title = it.title,
                stopCount = 0,
                location = it.location,
                isPublic = it.isPublic,
                coverImageKey = it.coverUrl ?: ""
            )
        }
    }
    val posts by viewModel.posts.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // null = feed, non-null = PostDetailScreen
    var openedPost by remember { mutableStateOf<CommunityPost?>(null) }

    // ── PostDetail overlay (Threads-style full screen)
    openedPost?.let { post ->
        DisposableEffect(post.id) {
            viewModel.subscribeToPostDetails(post.id)
            onDispose {
                viewModel.unsubscribeFromPostDetails()
            }
        }
        PostDetailScreen(
            post = post,
            viewModel = viewModel,
            onBack = { openedPost = null },
            onItineraryClick = { onNavigate("itinerary_detail/$it") }
        )
        return@CommunityScreen
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                onSearchClick = { onNavigate("search") },
                onNotificationClick = { onNavigate("notifications") }
            )
            HorizontalDivider(color = Color(0xFFF1F5F9))

            if (isLoading && posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VNRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item(contentType = "header") { ScreenHeader(title = "Cộng Đồng") }
                    item(contentType = "create_post") {
                        CreatePostWidget(
                            avatarInitials = currentUserProfile?.avatarInitials ?: "BN",
                            avatarColor = currentUserProfile?.avatarColor ?: 0xFFC6102E,
                            avatarUrl = currentUserProfile?.avatarUrl ?: "",
                            text = postText,
                            onTextChange = { postText = it },
                            linkedItinerary = linkedItinerary,
                            onUnlinkClick = { activeShareItineraryId = null },
                            onPost = {
                                if (postText.isNotBlank() || activeShareItineraryId != null) {
                                    viewModel.createPost(
                                        content = postText,
                                        itineraryId = activeShareItineraryId
                                    )
                                    postText = ""
                                    activeShareItineraryId = null
                                    onNavigate("community")
                                }
                            }
                        )
                    }
                    item(contentType = "header") { ScreenHeader(title = "Bảng tin cộng đồng") }
                    items(
                        items = posts,
                        key = { it.id },
                        contentType = { "post" }
                    ) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = {
                                if (post.isLiked) viewModel.unlikePost(post.id) else viewModel.likePost(post.id)
                            },
                            onCommentClick = { openedPost = post },
                            onItineraryClick = { onNavigate("itinerary_detail/$it") },
                            onAuthorClick = {
                                val authorId = post.userId.takeIf { it.isNotBlank() } ?: return@PostCard
                                if (authorId == viewModel.currentUserId) {
                                    onNavigate("profile")
                                } else {
                                    onNavigate("profile/$authorId")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Accent section header — dùng ở Community + Profile + Itinerary
@Composable
fun ScreenHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.width(6.dp).height(24.dp).clip(CircleShape).background(VNRed))
        Text(
            title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = (-0.5).sp,
            color = SlateGray900
        )
    }
}
