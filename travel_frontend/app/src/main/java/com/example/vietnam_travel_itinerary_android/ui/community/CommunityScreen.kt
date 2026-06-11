package com.example.vietnam_travel_itinerary_android.ui.community

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

// ============================================================
// COMMUNITY SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigate: (String) -> Unit = {},
    itineraryViewModel: com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel? = null,
    viewModel: CommunityViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
    val selectedImages by viewModel.selectedImages.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val searchPlaceResults by viewModel.searchPlaceResults.collectAsState()
    val isSearchingPlaces by viewModel.isSearchingPlaces.collectAsState()
    val isCreatingPost by viewModel.isCreatingPost.collectAsState()
    val context = LocalContext.current
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

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

    // ── Place Picker Bottom Sheet
    if (showPlacePicker) {
        PlacePickerBottomSheet(
            searchResults = searchPlaceResults,
            isSearching = isSearchingPlaces,
            onSearch = { query -> viewModel.searchPlaces(query) },
            onSelect = { place ->
                viewModel.selectPlace(place)
                viewModel.clearSearchResults()
                showPlacePicker = false
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
            onBack = { openedPost = null },
            onItineraryClick = { itineraryId -> onNavigate("itinerary_detail/$itineraryId") }
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
                            selectedPlace = selectedPlace,
                            onPlaceClick = { showPlacePicker = true },
                            onRemovePlace = { viewModel.clearPlace() },
                            onMapClick = {
                                val place = selectedPlace
                                if (place != null && place.lat != 0.0) {
                                    openGoogleMaps(place.lat, place.lng, place.name)
                                } else {
                                    Toast.makeText(context, "Vui lòng chọn địa điểm trước", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPost = {
                                if (postText.isNotBlank() || activeShareItineraryId != null || selectedImages.isNotEmpty()) {
                                    val currentText = postText
                                    val currentItineraryId = activeShareItineraryId
                                    val currentPlaceId = selectedPlace?.id
                                    val currentImages = selectedImages.toList()
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
                        )
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
