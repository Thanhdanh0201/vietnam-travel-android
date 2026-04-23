package com.example.vietnam_travel_itinerary_android.ui.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.Comment
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ============================================================
// COMMENT BOTTOM SHEET
// Hiển thị khi user bấm nút bình luận trên bài đăng
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postId: String,
    comments: List<Comment>,
    commentCount: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var inputText by remember { mutableStateOf("") }
    var allComments by remember { mutableStateOf(comments) }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            // Drag handle + tiêu đề
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SlateGray200)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .clip(CircleShape)
                            .background(VNRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bình luận",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = SlateGray900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($commentCount)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SlateGray400
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SlateGray100)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // ── Danh sách comments
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 480.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (allComments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💬", fontSize = 32.sp)
                                Text(
                                    text = "Hãy là người đầu tiên bình luận!",
                                    color = SlateGray400,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(allComments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            onReply = { replyingTo = comment },
                            onLike = { /* toggle like */ }
                        )
                        // ── Nested replies
                        if (comment.replies.isNotEmpty()) {
                            comment.replies.forEach { reply ->
                                CommentItem(
                                    comment = reply,
                                    isReply = true,
                                    onReply = { replyingTo = comment },
                                    onLike = {}
                                )
                            }
                        }
                    }
                }
            }

            // ── Reply banner
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                replyingTo?.let { target ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VNRed.copy(alpha = 0.05f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↩ Đang trả lời ${target.authorName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = VNRed
                        )
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            color = SlateGray400,
                            modifier = Modifier.clickable { replyingTo = null }
                        )
                    }
                }
            }

            // ── Input box
            HorizontalDivider(color = SlateGray100)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar người dùng hiện tại
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(VNRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Text field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SlateGray50)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (replyingTo != null) "Viết trả lời..." else "Thêm bình luận...",
                            color = SlateGray400,
                            fontSize = 14.sp
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = SlateGray700,
                            fontSize = 14.sp
                        )
                    )
                }

                // Send button
                val canSend = inputText.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (canSend) VNRed else SlateGray200)
                        .clickable(enabled = canSend) {
                            val parent = replyingTo
                            if (parent != null) {
                                // Thêm reply
                                val newReply = Comment(
                                    id = "r_${System.currentTimeMillis()}",
                                    postId = postId,
                                    parentCommentId = parent.id,
                                    authorName = "Bạn",
                                    authorAvatarInitials = "BN",
                                    authorAvatarColor = 0xFFC6102E,
                                    timeAgo = "vừa xong",
                                    content = inputText,
                                    reactionCount = 0
                                )
                                allComments = allComments.map { c ->
                                    if (c.id == parent.id)
                                        c.copy(replies = c.replies + newReply, replyCount = c.replyCount + 1)
                                    else c
                                }
                                replyingTo = null
                            } else {
                                // Thêm top-level comment
                                val newComment = Comment(
                                    id = "c_${System.currentTimeMillis()}",
                                    postId = postId,
                                    authorName = "Bạn",
                                    authorAvatarInitials = "BN",
                                    authorAvatarColor = 0xFFC6102E,
                                    timeAgo = "vừa xong",
                                    content = inputText,
                                    reactionCount = 0
                                )
                                allComments = listOf(newComment) + allComments
                            }
                            inputText = ""
                            keyboard?.hide()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Gửi",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Single Comment Row
@Composable
fun CommentItem(
    comment: Comment,
    isReply: Boolean = false,
    onReply: () -> Unit,
    onLike: () -> Unit
) {
    var liked by remember(comment.id) { mutableStateOf(comment.isLiked) }
    var likeCount by remember(comment.id) { mutableIntStateOf(comment.reactionCount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isReply) Modifier.padding(start = 48.dp) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (isReply) 28.dp else 34.dp)
                .clip(CircleShape)
                .background(Color(comment.authorAvatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorAvatarInitials,
                color = Color.White,
                fontSize = if (isReply) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isReply) 12.dp else 4.dp,
                            topEnd = 12.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp
                        )
                    )
                    .background(if (isReply) VNRed.copy(alpha = 0.04f) else SlateGray50)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = comment.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SlateGray900
                    )
                    Text(
                        text = comment.content,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = SlateGray700
                    )
                }
            }

            // Actions under bubble
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.timeAgo,
                    fontSize = 11.sp,
                    color = SlateGray400
                )
                // Like
                Row(
                    modifier = Modifier.clickable {
                        liked = !liked
                        likeCount = if (liked) likeCount + 1 else likeCount - 1
                        onLike()
                    },
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (liked) VNRed else SlateGray400,
                        modifier = Modifier.size(12.dp)
                    )
                    if (likeCount > 0) {
                        Text(
                            text = likeCount.toString(),
                            fontSize = 11.sp,
                            color = if (liked) VNRed else SlateGray400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Reply (chỉ hiện ở comment top-level)
                if (!isReply) {
                    Text(
                        text = "Trả lời",
                        fontSize = 11.sp,
                        color = SlateGray400,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onReply() }
                    )
                    if (comment.replyCount > 0) {
                        Text(
                            text = "• ${comment.replyCount} trả lời",
                            fontSize = 11.sp,
                            color = SlateGray400
                        )
                    }
                }
            }
        }
    }
}
