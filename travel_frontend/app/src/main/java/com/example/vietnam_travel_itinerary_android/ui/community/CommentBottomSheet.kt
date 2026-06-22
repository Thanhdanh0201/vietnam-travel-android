package com.example.vietnam_travel_itinerary_android.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.model.Comment
import com.example.vietnam_travel_itinerary_android.data.model.CommunityPost
import com.example.vietnam_travel_itinerary_android.data.session.UserSessionCache
import com.example.vietnam_travel_itinerary_android.ui.components.itinerary.ItineraryCompactCard
import com.example.vietnam_travel_itinerary_android.ui.components.post.*
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ============================================================
// POST DETAIL SCREEN — kiểu Threads/Facebook
// Post hiển thị ở trên, comments cuộn bên dưới inline
// Input bar cố định ở đáy
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: CommunityPost,
    viewModel: CommunityViewModel,
    onBack: () -> Unit = {},
    onItineraryClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    highlightCommentId: String? = null,
) {
    val allComments by viewModel.comments.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val sessionProfile = remember { UserSessionCache.get() }
    val resolvedProfile = currentUserProfile ?: sessionProfile
    val context = LocalContext.current

    // Find the latest post details from state to reflect realtime changes
    val currentPost = posts.find { it.id == post.id } ?: post

    var inputText by remember { mutableStateOf("") }
    var commentImageUri by remember { mutableStateOf<Uri?>(null) }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var commentToReport by remember { mutableStateOf<Comment?>(null) }
    var postToReport by remember { mutableStateOf(false) }
    var showDeletePostDialog by remember { mutableStateOf(false) }
    var showPostMenu by remember { mutableStateOf(false) }
    val reportContext = LocalContext.current
    var quoteText by remember { mutableStateOf("") }
    var commentSortMode by remember { mutableStateOf(CommentSortMode.TOP) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val displayedComments = remember(allComments, commentSortMode) {
        sortComments(allComments, commentSortMode)
    }

    val isOwnPost = currentPost.userId == viewModel.currentUserId

    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val focusCommentInput by viewModel.focusCommentInput.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusCommentInput) {
        if (focusCommentInput) {
            focusRequester.requestFocus()
            keyboard?.show()
            viewModel.setFocusCommentInput(false)
        }
    }

    fun openAuthorProfile(userId: String) {
        if (userId.isNotBlank()) onAuthorClick(userId)
    }

    val commentPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> commentImageUri = uri }

    // Fetch comments from Supabase initially
    LaunchedEffect(post.id) {
        viewModel.loadComments(post.id)
        viewModel.loadUserProfile()
    }

    LaunchedEffect(highlightCommentId, displayedComments) {
        val targetId = highlightCommentId ?: return@LaunchedEffect
        val commentIndex = displayedComments.indexOfFirst { comment ->
            comment.id == targetId || comment.replies.any { it.id == targetId }
        }
        if (commentIndex >= 0) {
            listState.animateScrollToItem(index = 2 + commentIndex)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F6F6),
        topBar = {
            // ── TopBar: back + "Bài đăng" title
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8F6F6),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack, "Quay lại",
                                tint = SlateGray900, modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "Bài đăng",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = SlateGray900
                        )
                    }
                }
                HorizontalDivider(color = SlateGray100)
            }
        },
        // ── Fixed input bar at bottom
        bottomBar = {
            CommentInputBar(
                text = inputText,
                replyingTo = replyingTo,
                selectedImageUri = commentImageUri,
                currentUserAvatarColor = resolvedProfile?.avatarColor ?: 0xFFC6102E,
                currentUserAvatarInitials = resolvedProfile?.avatarInitials ?: "BN",
                currentUserAvatarUrl = resolvedProfile?.avatarUrl ?: "",
                onTextChange = { inputText = it },
                onCancelReply = { replyingTo = null },
                onImageClick = {
                    commentPhotoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { commentImageUri = null },
                onSend = {
                    val parent = replyingTo
                    val text = inputText
                    val imageUri = commentImageUri
                    if (text.isBlank() && imageUri == null) {
                        android.widget.Toast.makeText(context, "Vui lòng nhập nội dung bình luận hoặc chọn ảnh", android.widget.Toast.LENGTH_SHORT).show()
                        return@CommentInputBar
                    }
                    if (parent != null) {
                        viewModel.postComment(
                            post.id,
                            text,
                            parent.id,
                            imageUri,
                            context.contentResolver,
                        )
                        replyingTo = null
                    } else {
                        viewModel.postComment(
                            post.id,
                            text,
                            imageUri = imageUri,
                            contentResolver = context.contentResolver,
                        )
                    }
                    inputText = ""
                    commentImageUri = null
                    keyboard?.hide()
                },
                focusRequester = focusRequester
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ──────────────────────────────────────────────
            // 1. ORIGINAL POST (full — không card shadow)
            // ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Author row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthorAvatar(
                            initials = currentPost.authorAvatarInitials,
                            color = Color(currentPost.authorAvatarColor),
                            avatarUrl = currentPost.authorAvatarUrl,
                            size = 42,
                            modifier = Modifier.clickable {
                                openAuthorProfile(currentPost.userId)
                            },
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { openAuthorProfile(currentPost.userId) },
                        ) {
                            Text(
                                currentPost.authorName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateGray900
                            )
                            Text(
                                currentPost.timeAgo,
                                fontSize = 11.sp,
                                color = SlateGray400
                            )
                        }
                        Box {
                            Icon(
                                Icons.Default.MoreHoriz,
                                contentDescription = "Tuỳ chọn",
                                tint = SlateGray400,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showPostMenu = true },
                            )
                            DropdownMenu(
                                expanded = showPostMenu,
                                onDismissRequest = { showPostMenu = false },
                            ) {
                                if (isOwnPost) {
                                    DropdownMenuItem(
                                        text = { Text("Xoá bài viết", color = Color.Red) },
                                        onClick = {
                                            showPostMenu = false
                                            showDeletePostDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = null,
                                                tint = Color.Red,
                                            )
                                        },
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Báo cáo") },
                                        onClick = {
                                            showPostMenu = false
                                            postToReport = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Flag,
                                                contentDescription = null,
                                                tint = SlateGray400,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Content
                    if (post.content.isNotBlank()) {
                        Text(
                            post.content,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            color = SlateGray900
                        )
                    }

                    // Media
                    if (post.postType == "original" && post.media.isNotEmpty()) {
                        PostImageGrid(
                            post.media,
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                    }

                    // Embedded post
                    post.embeddedPost?.let {
                        EmbeddedPostCard(
                            embedded = it,
                            onNavigateToOriginal = { originalId ->
                                viewModel.setOpenedPostId(originalId)
                            }
                        )
                    }

                    // Linked itinerary
                    post.linkedItinerary?.let {
                        ItineraryCompactCard(it, onViewClick = { onItineraryClick(it.id) })
                    }

                    // ── Post stats row
                    HorizontalDivider(color = SlateGray100)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (currentPost.likeCount > 0) {
                            Text(
                                "${currentPost.likeCount} lượt thích",
                                fontSize = 13.sp,
                                color = SlateGray500
                            )
                        }
                        if (allComments.isNotEmpty()) {
                            if (currentPost.likeCount > 0) Text("·", color = SlateGray300, fontSize = 13.sp)
                            Text(
                                "${allComments.size} bình luận",
                                fontSize = 13.sp,
                                color = SlateGray500
                            )
                        }
                    }

                    // ── Action buttons row (Threads-style)
                    HorizontalDivider(color = SlateGray100)
                    val actionScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(actionScrollState)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Like
                            ActionButton(
                                icon = if (currentPost.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                label = "Thích",
                                tint = if (currentPost.isLiked) VNRed else SlateGray500,
                                onClick = {
                                    if (currentPost.isLiked) viewModel.unlikePost(currentPost.id) else viewModel.likePost(currentPost.id)
                                }
                            )
                            // Comment — scroll to first comment
                            ActionButton(
                                icon = Icons.Outlined.ChatBubbleOutline,
                                label = "Bình luận",
                                tint = SlateGray500,
                                onClick = {}
                            )
                            // Share
                            ActionButton(
                                icon = Icons.Outlined.Share,
                                label = "Chia sẻ",
                                tint = SlateGray500,
                                onClick = {
                                    quoteText = ""
                                    showShareDialog = true
                                }
                            )
                            // Save
                            ActionButton(
                                icon = if (currentPost.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                label = if (currentPost.isSaved) "Đã lưu" else "Lưu",
                                tint = if (currentPost.isSaved) VNRed else SlateGray500,
                                onClick = {
                                    if (currentPost.isSaved) viewModel.unsavePost(currentPost.id) else viewModel.savePost(currentPost.id)
                                }
                            )
                        }

                        val canScrollRight = actionScrollState.value < actionScrollState.maxValue
                        androidx.compose.animation.AnimatedVisibility(
                            visible = canScrollRight,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(Color.White.copy(alpha = 0f), Color.White)
                                        )
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowRight,
                                    contentDescription = "Cuộn sang phải",
                                    tint = VNRed,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = SlateGray100)
                }
            }

            // ──────────────────────────────────────────────
            // 2. COMMENT SORT BAR (Threads-style)
            // ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { sortMenuExpanded = true },
                    ) {
                        Text(
                            text = when (commentSortMode) {
                                CommentSortMode.TOP -> "Hàng đầu"
                                CommentSortMode.NEWEST -> "Mới nhất"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray700,
                        )
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = SlateGray500,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Hàng đầu",
                                    fontWeight = if (commentSortMode == CommentSortMode.TOP) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                )
                            },
                            onClick = {
                                commentSortMode = CommentSortMode.TOP
                                sortMenuExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Mới nhất",
                                    fontWeight = if (commentSortMode == CommentSortMode.NEWEST) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                )
                            },
                            onClick = {
                                commentSortMode = CommentSortMode.NEWEST
                                sortMenuExpanded = false
                            },
                        )
                    }
                }
            }

            // ──────────────────────────────────────────────
            // 3. COMMENTS LIST — inline, no modal
            // ──────────────────────────────────────────────
            if (displayedComments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💬", fontSize = 36.sp)
                            Text(
                                "Hãy là người đầu tiên bình luận!",
                                color = SlateGray400, fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(displayedComments, key = { it.id }) { comment ->
                    ThreadCommentItem(
                        comment = comment,
                        isHighlighted = comment.id == highlightCommentId,
                        onReply = { replyingTo = comment },
                        onLike = {
                            if (comment.isLiked) viewModel.unlikeComment(comment.id)
                            else viewModel.likeComment(comment.id)
                        },
                        onAuthorClick = { openAuthorProfile(comment.userId) },
                        onReport = if (comment.userId != viewModel.currentUserId) {
                            { commentToReport = comment }
                        } else null,
                    )
                    // Nested replies (indented)
                    comment.replies.forEach { reply ->
                        ThreadCommentItem(
                            comment = reply,
                            isReply = true,
                            isHighlighted = reply.id == highlightCommentId,
                            onReply = { replyingTo = comment },
                            onLike = {
                                if (reply.isLiked) viewModel.unlikeComment(reply.id)
                                else viewModel.likeComment(reply.id)
                            },
                            onAuthorClick = { openAuthorProfile(reply.userId) },
                            onReport = if (reply.userId != viewModel.currentUserId) {
                                { commentToReport = reply }
                            } else null,
                        )
                    }
                }
            }
        }

        commentToReport?.let { comment ->
            ReportBottomSheet(
                target = ReportTarget.COMMENT,
                onDismiss = { commentToReport = null },
                onSubmit = { reason, description ->
                    viewModel.reportPostOrComment(
                        reason = reason,
                        reportedPostId = null,
                        reportedCommentId = comment.id,
                        description = description,
                    )
                    commentToReport = null
                    android.widget.Toast.makeText(reportContext, "Đã gửi báo cáo. Cảm ơn bạn!", android.widget.Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (postToReport) {
            ReportBottomSheet(
                target = ReportTarget.POST,
                onDismiss = { postToReport = false },
                onSubmit = { reason, description ->
                    viewModel.reportPostOrComment(
                        reason = reason,
                        reportedPostId = currentPost.id,
                        reportedCommentId = null,
                        description = description,
                    )
                    postToReport = false
                    android.widget.Toast.makeText(reportContext, "Đã gửi báo cáo. Cảm ơn bạn!", android.widget.Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showDeletePostDialog) {
            AlertDialog(
                onDismissRequest = { showDeletePostDialog = false },
                title = { Text("Xoá bài viết", fontWeight = FontWeight.Bold) },
                text = { Text("Bạn có chắc chắn muốn xoá bài viết này không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePost(currentPost.id)
                            showDeletePostDialog = false
                            onBack()
                        },
                    ) {
                        Text("Xoá", color = VNRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePostDialog = false }) {
                        Text("Huỷ", color = SlateGray500)
                    }
                },
            )
        }

        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = {
                    showShareDialog = false
                },
                title = {
                    Text(
                        text = "Chia sẻ bài viết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateGray900
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Bạn có muốn chia sẻ bài viết này lên bảng tin của mình?",
                            fontSize = 14.sp,
                            color = SlateGray600
                        )
                        OutlinedTextField(
                            value = quoteText,
                            onValueChange = { quoteText = it },
                            placeholder = { Text("Viết suy nghĩ của bạn... (Tùy chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VNRed,
                                cursorColor = VNRed
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.repostPost(
                                postId = currentPost.id,
                                quoteText = quoteText.takeIf { it.isNotBlank() }
                            )
                            showShareDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                    ) {
                        Text("Chia sẻ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showShareDialog = false
                        }
                    ) {
                        Text("Hủy", color = SlateGray500)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

// ── Fixed bottom input bar
@Composable
private fun CommentInputBar(
    text: String,
    replyingTo: Comment?,
    selectedImageUri: Uri? = null,
    currentUserAvatarColor: Long,
    currentUserAvatarInitials: String,
    currentUserAvatarUrl: String = "",
    onTextChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onImageClick: () -> Unit = {},
    onRemoveImage: () -> Unit = {},
    onSend: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            // Reply banner
            AnimatedVisibility(visible = replyingTo != null, enter = fadeIn(), exit = fadeOut()) {
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
                            "↩ Đang trả lời ${target.authorName}",
                            fontSize = 12.sp, fontWeight = FontWeight.Medium, color = VNRed
                        )
                        Text(
                            "✕", fontSize = 14.sp, color = SlateGray400,
                            modifier = Modifier.clickable(onClick = onCancelReply)
                        )
                    }
                }
            }
            HorizontalDivider(color = SlateGray100)
            if (selectedImageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Xóa ảnh",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .clickable(onClick = onRemoveImage)
                                .padding(2.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AuthorAvatar(
                    initials = currentUserAvatarInitials,
                    color = Color(currentUserAvatarColor),
                    avatarUrl = currentUserAvatarUrl,
                    size = 34,
                )
                // Input field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SlateGray50)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            if (replyingTo != null) "Viết trả lời..." else "Thêm bình luận...",
                            color = SlateGray400, fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        textStyle = TextStyle(color = SlateGray700, fontSize = 14.sp),
                        maxLines = 4
                    )
                }
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = "Thêm ảnh",
                    tint = SlateGray500,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onImageClick),
                )
                // Send button
                val canSend = text.isNotBlank() || selectedImageUri != null
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (canSend) VNRed else SlateGray200)
                        .clickable(enabled = canSend, onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Send, "Gửi",
                        tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Thread-style comment item (no bubble — giống Threads)
@Composable
fun ThreadCommentItem(
    comment: Comment,
    isReply: Boolean = false,
    isHighlighted: Boolean = false,
    onReply: () -> Unit,
    onLike: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onReport: (() -> Unit)? = null,
) {
    var liked by remember(comment.id) { mutableStateOf(comment.isLiked) }
    var likeCount by remember(comment.id) { mutableIntStateOf(comment.reactionCount) }
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHighlighted) VNRed.copy(alpha = 0.08f) else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isReply) 56.dp else 16.dp,
                    end = 16.dp,
                    top = if (isReply) 8.dp else 12.dp,
                    bottom = 4.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thread line + avatar stack
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthorAvatar(
                    initials = comment.authorAvatarInitials,
                    color = Color(comment.authorAvatarColor),
                    avatarUrl = comment.authorAvatarUrl,
                    size = if (isReply) 28 else 34,
                    modifier = Modifier.clickable(
                        enabled = comment.userId.isNotBlank(),
                        onClick = onAuthorClick,
                    ),
                )
                // Vertical thread line (only if has replies)
                if (!isReply && comment.replies.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(SlateGray200)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Name + time + more
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            enabled = comment.userId.isNotBlank(),
                            onClick = onAuthorClick,
                        ),
                    ) {
                        Text(
                            comment.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SlateGray900
                        )
                        Text(comment.timeAgo, fontSize = 11.sp, color = SlateGray400)
                    }
                    if (onReport != null) {
                        Box {
                            Icon(
                                Icons.Default.MoreHoriz, null,
                                tint = SlateGray400,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { showMenu = true },
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Báo cáo") },
                                    onClick = {
                                        showMenu = false
                                        onReport()
                                    },
                                )
                            }
                        }
                    } else {
                        Icon(
                            Icons.Default.MoreHoriz, null,
                            tint = SlateGray400, modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Content (no bubble — Threads style)
                if (comment.content.isNotBlank() && comment.content != "📷") {
                    Text(
                        comment.content,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = SlateGray800,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (comment.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = comment.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                }

                // Actions row
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like
                    Row(
                        modifier = Modifier.clickable {
                            liked = !liked
                            likeCount = if (liked) likeCount + 1 else likeCount - 1
                            onLike()
                        },
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            null,
                            tint = if (liked) VNRed else SlateGray400,
                            modifier = Modifier.size(14.dp)
                        )
                        if (likeCount > 0) {
                            Text(
                                likeCount.toString(), fontSize = 12.sp,
                                color = if (liked) VNRed else SlateGray400
                            )
                        }
                    }
                    // Reply (chỉ top-level)
                    if (!isReply) {
                        Text(
                            "Trả lời",
                            fontSize = 12.sp, color = SlateGray400, fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(onClick = onReply)
                        )
                        if (comment.replyCount > 0) {
                            Text(
                                "${comment.replyCount} trả lời",
                                fontSize = 12.sp, color = SlateGray400
                            )
                        }
                    }
                }
            }
        }

        if (!isReply) HorizontalDivider(
            modifier = Modifier.padding(start = 60.dp),
            color = SlateGray100
        )
    }
}

// ── Action button (Thích / Bình luận / Chia sẻ / Lưu)
@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 12.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

private enum class CommentSortMode {
    TOP,
    NEWEST,
}

private fun sortComments(comments: List<Comment>, mode: CommentSortMode): List<Comment> {
    val sortedTopLevel = when (mode) {
        CommentSortMode.TOP -> comments.sortedWith(
            compareByDescending<Comment> { it.reactionCount }
                .thenByDescending { it.createdAtEpochMillis() },
        )
        CommentSortMode.NEWEST -> comments.sortedByDescending { it.createdAtEpochMillis() }
    }
    return sortedTopLevel.map { comment ->
        comment.copy(replies = sortReplies(comment.replies, mode))
    }
}

private fun sortReplies(replies: List<Comment>, mode: CommentSortMode): List<Comment> {
    return when (mode) {
        CommentSortMode.TOP -> replies.sortedWith(
            compareByDescending<Comment> { it.reactionCount }
                .thenByDescending { it.createdAtEpochMillis() },
        )
        CommentSortMode.NEWEST -> replies.sortedByDescending { it.createdAtEpochMillis() }
    }
}

private fun Comment.createdAtEpochMillis(): Long {
    if (createdAt.isBlank()) return 0L
    return try {
        java.time.OffsetDateTime.parse(createdAt).toInstant().toEpochMilli()
    } catch (_: Exception) {
        try {
            java.time.Instant.parse(createdAt).toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }
}
