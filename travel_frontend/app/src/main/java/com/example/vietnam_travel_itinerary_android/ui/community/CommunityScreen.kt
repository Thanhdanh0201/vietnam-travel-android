package com.example.vietnam_travel_itinerary_android.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.ui.components.AppTopBar
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
        ),
        replyCount = 1
    ),
    Comment(
        id = "c2", postId = postId,
        authorName = "Văn Hùng", authorAvatarInitials = "VH", authorAvatarColor = 0xFF0891B2,
        timeAgo = "30 phút trước", content = "Chi phí mình đi 3 ngày hết khoảng 2.5 triệu bạn nhé, rất hợp lý!",
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
    // Case 1: Bài gốc có 1 ảnh + linked itinerary + 8 bình luận
    CommunityPost(
        id = "1", authorName = "Minh Tuấn", authorAvatarInitials = "MT",
        authorAvatarColor = 0xFF2563EB, timeAgo = "2 GIỜ TRƯỚC", postType = "original",
        content = "Thức dậy trong làn sương mù trên Vịnh Hạ Long là một trải nghiệm tôi sẽ không bao giờ quên. Sự tĩnh lặng thật nên thơ. 🛶✨",
        media = listOf(PostMedia("m1", "halong_bay")),
        likeCount = 24, commentCount = 8, repostCount = 3, isLiked = false,
        linkedItinerary = LinkedItinerary("it_1", "Hành trình 3 ngày Ven biển", 12),
        comments = mockComments("1")
    ),
    // Case 2: Bài gốc có 2 ảnh + 15 bình luận (isLiked = true)
    CommunityPost(
        id = "2", authorName = "Lan Anh", authorAvatarInitials = "LA",
        authorAvatarColor = 0xFF7C3AED, timeAgo = "5 GIỜ TRƯỚC", postType = "original",
        content = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. Đã tìm thấy một con hẻm bí mật có món Cao Lầu ngon nhất! 🍜",
        media = listOf(PostMedia("m2", "hoian_lantern"), PostMedia("m3", "hoian_food")),
        likeCount = 47, commentCount = 15, repostCount = 11, isLiked = true,
        comments = mockComments("2")
    ),
    // Case 3: REPOST — chia sẻ lại bài của người khác không có caption
    CommunityPost(
        id = "3", authorName = "Quốc Bảo Trần", authorAvatarInitials = "QB",
        authorAvatarColor = 0xFF059669, timeAgo = "3 GIỜ TRƯỚC", postType = "repost",
        content = "",
        likeCount = 18, commentCount = 4, repostCount = 2, isLiked = false,
        embeddedPost = EmbeddedPost(
            originalPostId = "2", originalAuthorName = "Lan Anh",
            originalAuthorInitials = "LA", originalAuthorColor = 0xFF7C3AED,
            originalContent = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. Đã tìm thấy một con hẻm bí mật có món Cao Lầu ngon nhất! 🍜",
            originalMedia = listOf(PostMedia("m2", "hoian_lantern")),
            originalTimeAgo = "5 GIỜ TRƯỚC"
        ),
        comments = mockComments("3")
    ),
    // Case 4: QUOTE — chia sẻ lại kèm caption
    CommunityPost(
        id = "4", authorName = "Thu Hà", authorAvatarInitials = "TH",
        authorAvatarColor = 0xFFEA580C, timeAgo = "1 NGÀY TRƯỚC", postType = "quote",
        content = "Ai chưa đi Hội An thì nhất định phải thử! Mình đã đi 3 lần rồi mà vẫn muốn quay lại 🥰",
        likeCount = 63, commentCount = 22, repostCount = 5, isLiked = true,
        embeddedPost = EmbeddedPost(
            originalPostId = "2", originalAuthorName = "Lan Anh",
            originalAuthorInitials = "LA", originalAuthorColor = 0xFF7C3AED,
            originalContent = "Những chiếc lồng đèn ở Hội An khi đêm xuống thật huyền ảo. Đã tìm thấy một con hẻm bí mật có món Cao Lầu ngon nhất! 🍜",
            originalTimeAgo = "1 NGÀY TRƯỚC"
        ),
        comments = mockComments("4")
    ),
    // Case 5: Bài chỉ text + itinerary, nhiều like
    CommunityPost(
        id = "5", authorName = "Quốc Bảo Trần", authorAvatarInitials = "QB",
        authorAvatarColor = 0xFF059669, timeAgo = "2 NGÀY TRƯỚC", postType = "original",
        content = "Vừa kết thúc chuyến đi xuyên Việt 2 tuần. Việt Nam ơi, bạn đã chiếm trọn trái tim tôi. Đây là lịch trình đầy đủ cho những ai đang lên kế hoạch! 🇻🇳❤️",
        likeCount = 132, commentCount = 34, repostCount = 28, isLiked = false,
        linkedItinerary = LinkedItinerary("it_2", "Hành trình Xuyên Việt", 21),
        comments = mockComments("5")
    )
)

// ============================================================
// MAIN SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(onNavigate: (String) -> Unit = {}) {
    var postText by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf(mockPosts) }
    var commentSheetPost by remember { mutableStateOf<CommunityPost?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar chung (Logo + Search + Avatar)
            AppTopBar(
                onSearchClick = { onNavigate("search") },
                onNotificationClick = { onNavigate("notifications") }
            )
            HorizontalDivider(color = Color(0xFFF1F5F9))

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { ScreenHeader(title = "Cộng Đồng") }
            item {
                CreatePostWidget(text = postText, onTextChange = { postText = it }, onPost = {
                    if (postText.isNotBlank()) {
                        posts = listOf(CommunityPost(
                            id = System.currentTimeMillis().toString(),
                            authorName = "Bạn", authorAvatarInitials = "BN",
                            authorAvatarColor = 0xFFC6102E, timeAgo = "VỪA XONG",
                            content = postText
                        )) + posts
                        postText = ""
                    }
                })
            }
            item { ScreenHeader(title = "Bảng tin cộng đồng") }
            items(posts, key = { it.id }) { post ->
                var liked by remember(post.id) { mutableStateOf(post.isLiked) }
                var likeCount by remember(post.id) { mutableIntStateOf(post.likeCount) }
                PostCard(
                    post = post.copy(isLiked = liked, likeCount = likeCount),
                    onLikeClick = {
                        liked = !liked
                        likeCount = if (liked) likeCount + 1 else likeCount - 1
                    },
                    onCommentClick = { commentSheetPost = post },
                    onItineraryClick = { onNavigate("itinerary_detail/$it") }
                )
            }
            }
        }

        // Comment bottom sheet
        commentSheetPost?.let { post ->
            CommentBottomSheet(
                postId = post.id,
                comments = post.comments,
                commentCount = post.commentCount,
                onDismiss = { commentSheetPost = null }
            )
        }
    }
}

// ── Reusable accent header
@Composable
fun ScreenHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.width(6.dp).height(24.dp).clip(CircleShape).background(VNRed))
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = (-0.5).sp, color = SlateGray900)
    }
}

// ── Create Post Widget
@Composable
private fun CreatePostWidget(text: String, onTextChange: (String) -> Unit, onPost: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.border(1.dp, VNRed.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                AuthorAvatar("BN", Color(0xFFC6102E), 40)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 8.dp)) {
                        if (text.isEmpty()) Text("Chia sẻ hành trình Việt Nam của bạn...", color = SlateGray400, fontSize = 14.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = text, onValueChange = onTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = SlateGray700, fontSize = 14.sp, lineHeight = 20.sp)
                        )
                    }
                    HorizontalDivider(color = SlateGray100)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Outlined.Image, null, tint = SlateGray400, modifier = Modifier.size(18.dp))
                            Icon(Icons.Outlined.Place, null, tint = SlateGray400, modifier = Modifier.size(18.dp))
                            Icon(Icons.Outlined.Map, null, tint = SlateGray400, modifier = Modifier.size(18.dp))
                        }
                        Button(onClick = onPost, shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)) {
                            Text("ĐĂNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// POST CARD — hiển thị 3 dạng: original / repost / quote
// ============================================================

@Composable
fun PostCard(
    post: CommunityPost,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onItineraryClick: (String) -> Unit = {}
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.border(1.dp, VNRed.copy(alpha = 0.05f), RoundedCornerShape(12.dp))) {
            PostHeader(post)

            // Caption (chỉ hiện nếu có — repost thuần thì content = "")
            if (post.content.isNotBlank()) {
                Text(post.content, fontSize = 14.sp, lineHeight = 23.sp, color = SlateGray700,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }

            // Ảnh bài gốc (chỉ khi original)
            if (post.postType == "original" && post.media.isNotEmpty()) {
                PostImageGrid(post.media)
                Spacer(Modifier.height(16.dp))
            }

            // Embedded post (repost / quote)
            if (post.embeddedPost != null) {
                EmbeddedPostCard(post.embeddedPost, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                post.linkedItinerary?.let {
                    LinkedItineraryCard(it, onViewClick = { onItineraryClick(it.id) })
                }
                HorizontalDivider(color = SlateGray50)
                PostActions(post.likeCount, post.commentCount, post.repostCount, post.isLiked,
                    onLikeClick, onCommentClick)
            }
        }
    }
}

@Composable
private fun PostHeader(post: CommunityPost) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        AuthorAvatar(post.authorAvatarInitials, Color(post.authorAvatarColor), 40)
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGray900)
                    // Badge loại bài
                    if (post.postType != "original") {
                        val label = if (post.postType == "repost") "🔁 Đã chia sẻ lại" else "💬 Đã trích dẫn"
                        Text(label, fontSize = 10.sp, color = SlateGray400, fontWeight = FontWeight.Medium)
                    }
                }
                Text(post.timeAgo, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp, color = SlateGray400)
            }
        }
        Icon(Icons.Default.MoreHoriz, null, tint = SlateGray400, modifier = Modifier.size(20.dp))
    }
}

// ── Embedded original post (repost / quote)
@Composable
private fun EmbeddedPostCard(embedded: EmbeddedPost, modifier: Modifier = Modifier) {
    val redColor = VNRed
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
        .background(SlateGray50)
        .drawBehind {
            drawLine(color = redColor, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 4.dp.toPx())
        }
        .padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Author
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(embedded.originalAuthorColor)),
                contentAlignment = Alignment.Center) {
                Text(embedded.originalAuthorInitials, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Text(embedded.originalAuthorName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateGray900)
            Text("• ${embedded.originalTimeAgo}", fontSize = 10.sp, color = SlateGray400)
        }
        // Content
        Text(embedded.originalContent, fontSize = 13.sp, lineHeight = 19.sp, color = SlateGray700,
            maxLines = 3, overflow = TextOverflow.Ellipsis)
        // Thumbnail
        if (embedded.originalMedia.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(6.dp))) {
                ImagePlaceholderBox(embedded.originalMedia[0].mediaUrl, Modifier.fillMaxSize())
            }
        }
    }
}

// ── Image Grid
@Composable
private fun PostImageGrid(media: List<PostMedia>) {
    val single = media.size == 1
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .height(if (single) 240.dp else 180.dp).clip(RoundedCornerShape(8.dp))) {
        if (single) {
            ImagePlaceholderBox(media[0].mediaUrl, Modifier.fillMaxSize(), gradient = true)
        } else {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ImagePlaceholderBox(media[0].mediaUrl, Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)))
                ImagePlaceholderBox(media[1].mediaUrl, Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)))
            }
        }
    }
}

@Composable
internal fun ImagePlaceholderBox(label: String, modifier: Modifier = Modifier, gradient: Boolean = false) {
    val color = when {
        label.contains("halong", true)  -> Color(0xFF0E7490)
        label.contains("hoian", true)   -> Color(0xFFD97706)
        label.contains("sapa", true)    -> Color(0xFF166534)
        label.contains("hanoi", true)   -> Color(0xFF7C3AED)
        else -> Color(0xFF374151)
    }
    Box(modifier.background(color), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Image, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(28.dp))
            Text(label.replace("_"," ").uppercase(), fontSize = 9.sp, color = Color.White.copy(0.7f), fontWeight = FontWeight.Medium)
        }
        if (gradient) Box(Modifier.fillMaxSize().background(
            androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))))
    }
}

// ── Linked Itinerary
@Composable
private fun LinkedItineraryCard(itinerary: LinkedItinerary, onViewClick: () -> Unit) {
    val red = VNRed
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(VNRed.copy(0.05f))
        .drawBehind { drawLine(red, Offset(0f,0f), Offset(0f,size.height), 8.dp.toPx()) }
        .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Map, null, tint = VNRed, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(itinerary.title.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    letterSpacing = 0.6.sp, color = VNRed, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Lịch trình chia sẻ • ${itinerary.stopCount} Điểm dừng",
                    fontSize = 10.sp, fontWeight = FontWeight.Medium, color = SlateGray500)
            }
        }
        OutlinedButton(onClick = onViewClick, shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(0.1f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = VNRed),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(28.dp)) {
            Text("XEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// ── Post Actions
@Composable
private fun PostActions(
    likeCount: Int, commentCount: Int, repostCount: Int,
    isLiked: Boolean, onLikeClick: () -> Unit, onCommentClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Like
        Row(Modifier.clickable(onClick = onLikeClick), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Thích",
                tint = if (isLiked) VNRed else SlateGray500, modifier = Modifier.size(17.dp))
            Text(likeCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = if (isLiked) VNRed else SlateGray500)
        }
        Spacer(Modifier.width(20.dp))
        // Comment
        Row(Modifier.clickable(onClick = onCommentClick), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.ChatBubbleOutline, "Bình luận", tint = SlateGray500, modifier = Modifier.size(17.dp))
            Text(commentCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray500)
        }
        Spacer(Modifier.width(20.dp))
        // Repost count
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.Share, "Chia sẻ", tint = SlateGray500, modifier = Modifier.size(17.dp))
            if (repostCount > 0)
                Text(repostCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray500)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.BookmarkBorder, "Lưu", tint = SlateGray400, modifier = Modifier.size(17.dp))
    }
}

// ── Avatar
@Composable
internal fun AuthorAvatar(initials: String, color: Color, size: Int) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size * 0.35f).sp)
    }
}

