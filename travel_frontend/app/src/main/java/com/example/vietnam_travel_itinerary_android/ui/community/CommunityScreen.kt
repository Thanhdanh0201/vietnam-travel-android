package com.example.vietnam_travel_itinerary_android.ui.community

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale

// ============================================================
// COMMUNITY SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigate: (String) -> Unit = {},
    itineraryViewModel: com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel? = null,
    viewModel: CommunityViewModel = viewModel(factory = AppViewModelProvider.Factory),
    unreadCount: Int = 0,
    onMenuClick: (() -> Unit)? = null,
) {
    var postText by remember { mutableStateOf("") }
    val shareItineraryId by viewModel.shareItineraryId.collectAsState()
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
                stopCount = it.itemCount,
                location = it.location,
                isPublic = it.isPublic,
                coverImageKey = it.coverUrl ?: ""
            )
        }
    }
    val posts by viewModel.posts.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            if (!hasMore || isLoadingMore || isLoading || isRefreshing) return@derivedStateOf false
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }
    val errorMsg by viewModel.error.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val searchPlaceResults by viewModel.searchPlaceResults.collectAsState()
    val searchProvinceResults by viewModel.searchProvinceResults.collectAsState()
    val provincePlacesResults by viewModel.provincePlacesResults.collectAsState()
    val selectedProvinceFilter by viewModel.selectedProvinceFilter.collectAsState()
    val isSearchingPlaces by viewModel.isSearchingPlaces.collectAsState()
    val isCreatingPost by viewModel.isCreatingPost.collectAsState()
    val context = LocalContext.current
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }
    var postToShare by remember { mutableStateOf<CommunityPost?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var postToReport by remember { mutableStateOf<CommunityPost?>(null) }
    var quoteText by remember { mutableStateOf("") }

    // ── Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = (4 - selectedImages.size).coerceAtLeast(1)
        )
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    // ── Place Picker state
    var showPlacePicker by remember { mutableStateOf(false) }
    var showItineraryPicker by remember { mutableStateOf(false) }
    var initialShareItineraryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showItineraryPicker) {
        if (showItineraryPicker) {
            initialShareItineraryId = activeShareItineraryId
        }
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // ── Helper to open Google Maps
    fun openGoogleMaps(lat: Double, lng: Double, label: String) {
        try {
            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback: open in browser
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
        }
    }

    // null = feed, non-null = PostDetailScreen
    var openedPost by remember { mutableStateOf<CommunityPost?>(null) }

    val targetOpenedPostId by viewModel.openedPostId.collectAsState()
    val highlightCommentId by viewModel.highlightCommentId.collectAsState()

    LaunchedEffect(targetOpenedPostId) {
        targetOpenedPostId?.let { postId ->
            if (openedPost?.id != postId) {
                viewModel.fetchPostDetails(postId) { post ->
                    openedPost = post
                }
            }
        } ?: run {
            openedPost = null
        }
    }

    // ── Place Picker Bottom Sheet
    if (showPlacePicker) {
        PlacePickerBottomSheet(
            searchResults = searchPlaceResults,
            provinceResults = searchProvinceResults,
            provincePlaces = provincePlacesResults,
            selectedProvince = selectedProvinceFilter,
            isSearching = isSearchingPlaces,
            onSearch = { query -> viewModel.searchPlaces(query) },
            onSelect = { place ->
                viewModel.selectPlace(place)
                viewModel.clearSearchResults()
                showPlacePicker = false
            },
            onSelectProvince = { province ->
                viewModel.loadPlacesForProvince(province)
            },
            onClearProvinceFilter = {
                viewModel.clearProvinceFilter()
            },
            onDismiss = {
                viewModel.clearSearchResults()
                showPlacePicker = false
            }
        )
    }

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
            highlightCommentId = highlightCommentId,
            onBack = {
                openedPost = null
                viewModel.setOpenedPostId(null)
            },
            onItineraryClick = { itineraryId -> onNavigate("itinerary_detail/$itineraryId") },
            onAuthorClick = { userId ->
                if (userId == viewModel.currentUserId) {
                    onNavigate("profile")
                } else {
                    onNavigate("profile/$userId")
                }
            },
        )
        return@CommunityScreen
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                onSearchClick = { onNavigate("search") },
                onNotificationClick = { onNavigate("notifications") },
                unreadCount = unreadCount,
                onMenuClick = onMenuClick,
            )
            HorizontalDivider(color = Color(0xFFF1F5F9))

            if (isLoading && posts.isEmpty() && !isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VNRed)
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                LazyColumn(
                    state = listState,
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
                            onUnlinkClick = {
                                activeShareItineraryId = null
                                viewModel.setShareItineraryId(null)
                            },
                            selectedImages = selectedImages,
                            onImageClick = {
                                val remaining = 4 - selectedImages.size
                                if (remaining > 0) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else {
                                    Toast.makeText(context, "Tối đa 4 ảnh", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRemoveImage = { index -> viewModel.removeImage(index) },
                            onItineraryClick = {
                                showItineraryPicker = true
                            },
                            selectedPlace = selectedPlace,
                            onPlaceClick = { showPlacePicker = true },
                            onUnlinkPlaceClick = { viewModel.clearPlace() },
                            onPost = {
                                if (postText.isNotBlank() || activeShareItineraryId != null || selectedImages.isNotEmpty() || selectedPlace != null) {
                                    val currentText = postText
                                    val currentItineraryId = activeShareItineraryId
                                    val currentImages = selectedImages.toList()
                                    val currentPlaceId = selectedPlace?.id
                                    // Clear UI immediately for responsive feel
                                    postText = ""
                                    activeShareItineraryId = null
                                    viewModel.setShareItineraryId(null)
                                    // Upload + create in background (ViewModel clears images/place on success)
                                    viewModel.createPost(
                                        content = currentText,
                                        mediaUris = currentImages,
                                        itineraryId = currentItineraryId,
                                        placeId = currentPlaceId,
                                        contentResolver = context.contentResolver
                                    )
                                }
                            }
                        )

                        // Loading indicator while creating post
                        if (isCreatingPost) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = VNRed,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Đang đăng bài...",
                                    fontSize = 12.sp,
                                    color = SlateGray500
                                )
                            }
                        }
                    }
                    item(contentType = "header") { ScreenHeader(title = "Bảng tin cộng đồng") }
                    items(
                        items = posts,
                        key = { it.id },
                        contentType = { "post" }
                    ) { post ->
                        PostCard(
                            post = post,
                            currentUserId = viewModel.currentUserId,
                            onLikeClick = {
                                if (post.isLiked) viewModel.unlikePost(post.id) else viewModel.likePost(post.id)
                            },
                            onCommentClick = { openedPost = post },
                            onSaveClick = {
                                if (post.isSaved) viewModel.unsavePost(post.id) else viewModel.savePost(post.id)
                            },
                            onDeleteClick = {
                                postToDelete = post
                            },
                            onReportClick = {
                                postToReport = post
                            },
                            onItineraryClick = { itineraryId -> onNavigate("itinerary_detail/$itineraryId") },
                            onPlaceClick = { lat, lng, name ->
                                openGoogleMaps(lat, lng, name)
                            },
                            onAuthorClick = {
                                val authorId = post.userId.takeIf { it.isNotBlank() } ?: return@PostCard
                                if (authorId == viewModel.currentUserId) {
                                    onNavigate("profile")
                                } else {
                                    onNavigate("profile/$authorId")
                                }
                            },
                            onShareClick = {
                                postToShare = post
                                quoteText = ""
                                showShareDialog = true
                            },
                            onNavigateToPost = { originalPostId ->
                                viewModel.fetchPostDetails(originalPostId) { originalPost ->
                                    openedPost = originalPost
                                    viewModel.setOpenedPostId(originalPostId)
                                }
                            }
                        )
                    }
                    if (isLoadingMore) {
                        item(contentType = "loading_more") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    color = VNRed,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Đang tải thêm...",
                                    fontSize = 13.sp,
                                    color = SlateGray500,
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

        postToReport?.let { post ->
            ReportBottomSheet(
                target = ReportTarget.POST,
                onDismiss = { postToReport = null },
                onSubmit = { reason, description ->
                    viewModel.reportPostOrComment(
                        reason = reason,
                        reportedPostId = post.id,
                        reportedCommentId = null,
                        description = description,
                    )
                    postToReport = null
                    android.widget.Toast.makeText(context, "Đã gửi báo cáo. Cảm ơn bạn!", android.widget.Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showShareDialog && postToShare != null) {
            AlertDialog(
                onDismissRequest = {
                    showShareDialog = false
                    postToShare = null
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
                            val targetPost = postToShare
                            if (targetPost != null) {
                                viewModel.repostPost(
                                    postId = targetPost.id,
                                    quoteText = quoteText.takeIf { it.isNotBlank() }
                                )
                            }
                            showShareDialog = false
                            postToShare = null
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
                            postToShare = null
                        }
                    ) {
                        Text("Hủy", color = SlateGray500)
                    }
                },
                containerColor = Color.White
            )
        }

        if (showItineraryPicker && itineraryViewModel != null) {
            val uiStateVal by itineraryViewModel.uiState.collectAsState()
            val userItineraries = uiStateVal.itineraries

            // Fetch initially
            LaunchedEffect(Unit) {
                itineraryViewModel.fetchItineraries()
            }

            AlertDialog(
                onDismissRequest = {
                    activeShareItineraryId = initialShareItineraryId
                    viewModel.setShareItineraryId(initialShareItineraryId)
                    showItineraryPicker = false
                },
                title = {
                    Text(
                        text = "Chọn lịch trình đính kèm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateGray900
                    )
                },
                text = {
                    if (userItineraries.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Bạn chưa có lịch trình nào.", color = SlateGray500, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(userItineraries) { itinerary ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeShareItineraryId = itinerary.id
                                            viewModel.setShareItineraryId(itinerary.id)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // ── Left: small image
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SlateGray100)
                                    ) {
                                        if (!itinerary.coverUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = itinerary.coverUrl,
                                                contentDescription = itinerary.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            androidx.compose.foundation.Image(
                                                painter = painterResource(id = itinerary.imageResId),
                                                contentDescription = itinerary.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    // ── Middle: Title & Info
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = itinerary.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SlateGray800,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = itinerary.location.ifBlank { "Chưa có địa điểm" },
                                            fontSize = 11.sp,
                                            color = SlateGray400,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // ── Right: RadioButton
                                    RadioButton(
                                        selected = activeShareItineraryId == itinerary.id,
                                        onClick = {
                                            activeShareItineraryId = itinerary.id
                                            viewModel.setShareItineraryId(itinerary.id)
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = VNRed,
                                            unselectedColor = SlateGray300
                                        )
                                    )
                                }
                                HorizontalDivider(color = SlateGray50)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showItineraryPicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                    ) {
                        Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            activeShareItineraryId = initialShareItineraryId
                            viewModel.setShareItineraryId(initialShareItineraryId)
                            showItineraryPicker = false
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
