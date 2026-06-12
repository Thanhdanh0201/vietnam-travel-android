package com.example.vietnam_travel_itinerary_android.ui.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
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
import com.example.vietnam_travel_itinerary_android.data.model.CommunityPost
import com.example.vietnam_travel_itinerary_android.data.model.EmbeddedPost
import com.example.vietnam_travel_itinerary_android.data.model.LinkedItinerary
import com.example.vietnam_travel_itinerary_android.data.model.PostMedia
import com.example.vietnam_travel_itinerary_android.data.model.PostPlace
import com.example.vietnam_travel_itinerary_android.ui.components.itinerary.ItineraryCompactCard
import com.example.vietnam_travel_itinerary_android.ui.theme.*
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// ============================================================
// SHARED POST COMPONENTS
// Dùng được ở: CommunityScreen, ProfileScreen, và bất kỳ
// màn hình nào cần hiển thị bài đăng.
// ============================================================

// ── Post Card — entry point chính
@Composable
fun PostCard(
    post: CommunityPost,
    currentUserId: String? = null,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onItineraryClick: (String) -> Unit = {},
    onPlaceClick: ((Double, Double, String) -> Unit)? = null,
    onAuthorClick: (() -> Unit)? = null,
    onNavigateToPost: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.border(1.dp, VNRed.copy(alpha = 0.05f), RoundedCornerShape(12.dp))) {
            PostHeader(
                post = post,
                showDeleteOption = currentUserId != null && post.userId == currentUserId,
                onDeleteClick = onDeleteClick,
                onAuthorClick = onAuthorClick
            )

            // Caption (repost thuần thì content = "")
            if (post.content.isNotBlank()) {
                Text(
                    post.content, fontSize = 14.sp, lineHeight = 23.sp, color = SlateGray700,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            // Place badge
            post.place?.let { place ->
                val badgeModifier = if (onPlaceClick != null && place.lat != 0.0) {
                    Modifier.clickable { onPlaceClick(place.lat, place.lng, place.name) }
                } else Modifier
                Row(
                    modifier = badgeModifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Place,
                        contentDescription = "Địa điểm",
                        tint = VNRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        buildString {
                            append(place.name)
                            if (place.provinceName.isNotBlank()) {
                                append(", ").append(place.provinceName)
                            }
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = VNRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Media (chỉ khi original)
            if (post.postType == "original" && post.media.isNotEmpty()) {
                PostImageGrid(post.media)
                Spacer(Modifier.height(16.dp))
            }

            // Embedded post (repost / quote)
            post.embeddedPost?.let {
                EmbeddedPostCard(
                    embedded = it,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onNavigateToOriginal = { originalId ->
                        onNavigateToPost?.invoke(originalId)
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                post.linkedItinerary?.let { itinerary ->
                    ItineraryCompactCard(
                        itinerary = itinerary,
                        onViewClick = { onItineraryClick(itinerary.id) }
                    )
                }
                HorizontalDivider(color = SlateGray50)
                PostActions(
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    repostCount = post.repostCount,
                    isLiked = post.isLiked,
                    isSaved = post.isSaved,
                    onLikeClick = onLikeClick,
                    onCommentClick = onCommentClick,
                    onShareClick = onShareClick,
                    onSaveClick = onSaveClick
                )
            }
        }
    }
}

// ── Post Header: avatar + name + post type badge + time + more
@Composable
fun PostHeader(
    post: CommunityPost,
    showDeleteOption: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onAuthorClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (onAuthorClick != null) Modifier.clickable(onClick = onAuthorClick)
                    else Modifier
                ),
        ) {
            AuthorAvatar(
                initials = post.authorAvatarInitials,
                color = Color(post.authorAvatarColor),
                avatarUrl = post.authorAvatarUrl,
                size = 40,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onAuthorClick != null) Modifier.clickable(onClick = onAuthorClick)
                    else Modifier
                ),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGray900)
                    if (post.postType != "original") {
                        val label = if (post.postType == "repost") "🔁 Đã chia sẻ lại" else "💬 Đã trích dẫn"
                        Text(label, fontSize = 10.sp, color = SlateGray400, fontWeight = FontWeight.Medium)
                    }
                }
                Text(post.timeAgo, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp, color = SlateGray400)
            }
        }
        
        Box {
            IconButton(
                onClick = { if (showDeleteOption) showMenu = true },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(Icons.Default.MoreHoriz, null, tint = SlateGray400)
            }

            if (showDeleteOption) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Xoá bài viết", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Xoá",
                                tint = Color.Red
                            )
                        }
                    )
                }
            }
        }
    }
}

// ── Embedded original post (repost / quote)
@Composable
fun EmbeddedPostCard(
    embedded: EmbeddedPost,
    modifier: Modifier = Modifier,
    onNavigateToOriginal: (String) -> Unit = {}
) {
    val redColor = VNRed
    val isTextOnly = embedded.originalMedia.isEmpty() && !embedded.hasItinerary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateGray50)
            .clickable { onNavigateToOriginal(embedded.originalPostId) }
            .drawBehind {
                drawLine(color = redColor, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 4.dp.toPx())
            }
            .padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AuthorAvatar(
                initials = embedded.originalAuthorInitials,
                color = Color(embedded.originalAuthorColor),
                avatarUrl = embedded.originalAuthorAvatarUrl,
                size = 22,
            )
            Text(embedded.originalAuthorName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateGray900)
            Text("• ${embedded.originalTimeAgo}", fontSize = 10.sp, color = SlateGray400)
        }

        if (isTextOnly) {
            if (embedded.originalContent.isNotBlank()) {
                Text(
                    text = embedded.originalContent,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = SlateGray700,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            val labelText = when {
                embedded.hasItinerary -> "Đã chia sẻ một lịch trình"
                else -> "Đã chia sẻ một ảnh"
            }
            Text(
                text = labelText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SlateGray500
            )
        }
    }
}

// ── Image Grid (1 or 2 images)
@Composable
fun PostImageGrid(media: List<PostMedia>, modifier: Modifier = Modifier) {
    val single = media.size == 1
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(if (single) 240.dp else 180.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (single) {
            ImagePlaceholderBox(media[0].mediaUrl, Modifier.fillMaxSize(), gradient = true)
        } else {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ImagePlaceholderBox(
                    media[0].mediaUrl,
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                )
                ImagePlaceholderBox(
                    media[1].mediaUrl,
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                )
            }
        }
    }
}

// ── Linked Itinerary Card
@Composable
fun LinkedItineraryCard(itinerary: LinkedItinerary, onViewClick: () -> Unit) {
    val red = VNRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VNRed.copy(0.05f))
            .drawBehind { drawLine(red, Offset(0f, 0f), Offset(0f, size.height), 8.dp.toPx()) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Map, null, tint = VNRed, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    itinerary.title.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    letterSpacing = 0.6.sp, color = VNRed, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Lịch trình chia sẻ • ${itinerary.stopCount} Điểm dừng",
                    fontSize = 10.sp, fontWeight = FontWeight.Medium, color = SlateGray500
                )
            }
        }
        OutlinedButton(
            onClick = onViewClick,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(0.1f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = VNRed),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text("XEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// ── Post Actions bar
@Composable
fun PostActions(
    likeCount: Int,
    commentCount: Int,
    repostCount: Int,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Like
        Row(
            Modifier.clickable(onClick = onLikeClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Thích",
                tint = if (isLiked) VNRed else SlateGray500, modifier = Modifier.size(17.dp)
            )
            Text(likeCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isLiked) VNRed else SlateGray500)
        }
        Spacer(Modifier.width(20.dp))
        // Comment
        Row(
            Modifier.clickable(onClick = onCommentClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.ChatBubbleOutline, "Bình luận", tint = SlateGray500, modifier = Modifier.size(17.dp))
            Text(commentCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray500)
        }
        Spacer(Modifier.width(20.dp))
        // Repost
        Row(
            modifier = Modifier.clickable(onClick = onShareClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.Share, "Chia sẻ", tint = SlateGray500, modifier = Modifier.size(17.dp))
            if (repostCount > 0) Text(repostCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray500)
        }
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = "Lưu",
            tint = if (isSaved) VNRed else SlateGray400,
            modifier = Modifier
                .size(17.dp)
                .clickable { onSaveClick() }
        )
    }
}

// ── Author Avatar — image URL with initials fallback
@Composable
fun AuthorAvatar(
    initials: String,
    color: Color,
    size: Int,
    avatarUrl: String = "",
    modifier: Modifier = Modifier,
) {
    val url = avatarUrl.trim().takeIf { it.isNotBlank() }
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(if (url == null) Modifier.background(color) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.35f).sp,
            )
        }
    }
}

// ── Image Placeholder Box (dùng đến khi có Coil AsyncImage)
@Composable
fun ImagePlaceholderBox(label: String, modifier: Modifier = Modifier, gradient: Boolean = false) {
    if (label.startsWith("http://") || label.startsWith("https://")) {
        Box(modifier) {
            AsyncImage(
                model = label,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (gradient) Box(
                Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))
                )
            )
        }
    } else {
        val color = when {
            label.contains("halong", true) -> Color(0xFF0E7490)
            label.contains("hoian", true)  -> Color(0xFFD97706)
            label.contains("sapa", true)   -> Color(0xFF166534)
            label.contains("hanoi", true)  -> Color(0xFF7C3AED)
            else -> Color(0xFF374151)
        }
        Box(modifier.background(color), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Image, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(28.dp))
                Text(label.replace("_", " ").uppercase(), fontSize = 9.sp, color = Color.White.copy(0.7f), fontWeight = FontWeight.Medium)
            }
            if (gradient) Box(
                Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))
                )
            )
        }
    }
}
