package com.example.vietnam_travel_itinerary_android.ui.components.introduction

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.vietnam_travel_itinerary_android.data.auth.AuthSessionHelper
import com.example.vietnam_travel_itinerary_android.data.model.PlaceReview
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import com.example.vietnam_travel_itinerary_android.ui.theme.BackgroundLight
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray600
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray800
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import kotlinx.coroutines.launch
import java.util.Locale

private val ReviewCardBorder = VNRed.copy(alpha = 0.05f)
private val ReviewBodyColor = Color(0xFF475569)
private val ReviewMetaColor = Color(0xFF94A3B8)

@Composable
fun PlaceReviewFeedSection(
    reviews: List<PlaceReview>,
    loading: Boolean,
    loadFailed: Boolean,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(VNRed),
                )
                Text(
                    text = "Đánh giá du lịch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    lineHeight = 28.sp,
                    color = SlateGray900,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { },
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Mới nhất",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = VNRed,
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SlateGray500,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = VNRed, modifier = Modifier.size(32.dp))
                }
            }
            loadFailed -> {
                ReviewEmptyState(
                    message = "Không tải được đánh giá từ máy chủ.",
                    onWriteReviewClick = onWriteReviewClick,
                )
            }
            reviews.isEmpty() -> {
                ReviewEmptyState(
                    message = "Chưa có đánh giá nào.",
                    onWriteReviewClick = onWriteReviewClick,
                )
            }
            else -> {
                reviews.forEach { review ->
                    PlaceReviewCard(review = review)
                }
            }
        }
    }
}

@Composable
private fun ReviewEmptyState(
    message: String,
    onWriteReviewClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGray500,
        )
        Text(
            text = "Viết đánh giá ngay",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = VNRed,
            modifier = Modifier.clickable(onClick = onWriteReviewClick),
        )
    }
}

@Composable
fun PlaceReviewCard(review: PlaceReview) {
    val name = review.userName?.takeIf { it.isNotBlank() } ?: "Khách du lịch"
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifBlank { "K" }
    val avatarColor = remember(name) {
        val palette = listOf(0xFF64748B, 0xFF0EA5E9, 0xFF8B5CF6, 0xFFF59E0B, 0xFF10B981)
        Color(palette[name.hashCode().mod(palette.size)])
    }
    val starCount = review.rating?.coerceIn(0, 5) ?: 0
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, ReviewCardBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReviewAvatar(
                    avatarUrl = review.userAvatarUrl,
                    initials = initials,
                    fallbackColor = avatarColor,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    formatReviewDateUpper(review.createdAt)?.let { timeLabel ->
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.55.sp,
                            color = ReviewMetaColor,
                        )
                    }
                }
                StarRatingRow(count = starCount, starSize = 14.dp)
            }

            review.review?.takeIf { it.isNotBlank() }?.let { body ->
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    color = ReviewBodyColor,
                )
            }

            if (review.photoUrls.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(review.photoUrls.take(3), key = { it }) { url ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(url).build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewAvatar(
    avatarUrl: String?,
    initials: String,
    fallbackColor: Color,
) {
    val context = LocalContext.current
    val url = avatarUrl?.trim()?.takeIf { it.isNotBlank() }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(2.dp, VNRed.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(url).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fallbackColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun StarRatingRow(
    count: Int,
    max: Int = 5,
    starSize: androidx.compose.ui.unit.Dp = 14.dp,
    filledColor: Color = VNRed,
    emptyColor: Color = Color(0xFFE2E8F0),
) {
    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        repeat(max) { index ->
            Icon(
                imageVector = if (index < count) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < count) filledColor else emptyColor,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceWriteReviewSheet(
    placeName: String,
    placeId: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
    onNeedLogin: () -> Unit,
    onError: (String) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val placeRepo = remember { PlaceRepository() }
    var selectedStars by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    val pickedPhotos = remember { mutableStateListOf<Uri>() }
    var submitting by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3),
    ) { uris ->
        pickedPhotos.clear()
        pickedPhotos.addAll(uris.take(3))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFD1D5DB)),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Trải nghiệm của bạn tại $placeName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "BẠN CẢM THẤY THẾ NÀO?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.35.sp,
                    color = Color(0xFF6B7280),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        Icon(
                            imageVector = if (starIndex <= selectedStars) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.Star
                            },
                            contentDescription = "$starIndex sao",
                            tint = if (starIndex <= selectedStars) VNRed else Color(0xFFD1D5DB),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { selectedStars = starIndex },
                                ),
                        )
                    }
                }
            }

            androidx.compose.material3.OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp),
                placeholder = {
                    Text(
                        "Hãy chia sẻ cảm nhận của bạn về chuyến đi này nhé...",
                        color = Color(0xFF9CA3AF),
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                ),
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .border(2.dp, VNRed, RoundedCornerShape(12.dp))
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0x4DFEF2F2),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = VNRed,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (pickedPhotos.isEmpty()) {
                            "Thêm ảnh trải nghiệm"
                        } else {
                            "Đã chọn ${pickedPhotos.size} ảnh (lưu sau khi có upload)"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VNRed,
                    )
                }
            }

            if (pickedPhotos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pickedPhotos, key = { it.toString() }) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedStars < 1) {
                        onError("Vui lòng chọn số sao đánh giá")
                        return@Button
                    }
                    scope.launch {
                        submitting = true
                        val tokenResult = AuthSessionHelper.ensureBackendUserSynced()
                        val token = tokenResult.getOrElse { error ->
                            submitting = false
                            if (error is AuthSessionHelper.NotLoggedInException) {
                                onNeedLogin()
                            } else {
                                onError(AuthSessionHelper.humanReadableError(error))
                            }
                            return@launch
                        }
                        placeRepo.submitPlaceReview(
                            placeId = placeId,
                            token = token,
                            rating = selectedStars,
                            review = reviewText,
                            photoUrls = emptyList(),
                        ).fold(
                            onSuccess = {
                                submitting = false
                                onSubmitted()
                                onDismiss()
                            },
                            onFailure = { error ->
                                submitting = false
                                onError(AuthSessionHelper.humanReadableError(error))
                            },
                        )
                    }
                },
                enabled = !submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Gửi đánh giá",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Hủy",
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun formatReviewDateUpper(createdAt: String?): String? {
    val instant = parseReviewInstant(createdAt) ?: return null
    val zone = java.time.ZoneId.of("Asia/Ho_Chi_Minh")
    val then = instant.atZone(zone)
    val now = java.time.Instant.now().atZone(zone)
    val minutes = java.time.Duration.between(then, now).toMinutes().coerceAtLeast(0)
    val hours = java.time.Duration.between(then, now).toHours().coerceAtLeast(0)
    val days = java.time.Duration.between(then, now).toDays().coerceAtLeast(0)
    val text = when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days == 1L -> "Hôm qua"
        days < 7 -> "$days ngày trước"
        days < 30 -> "${days / 7} tuần trước"
        days < 365 -> "${days / 30} tháng trước"
        else -> "${days / 365} năm trước"
    }
    return text.uppercase(Locale.forLanguageTag("vi"))
}

private fun parseReviewInstant(raw: String?): java.time.Instant? {
    if (raw.isNullOrBlank()) return null
    val trimmed = raw.trim()
    return runCatching { java.time.Instant.parse(trimmed) }.getOrNull()
        ?: runCatching { java.time.OffsetDateTime.parse(trimmed).toInstant() }.getOrNull()
        ?: runCatching {
            java.time.LocalDateTime.parse(
                trimmed.take(19),
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            ).atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        }.getOrNull()
}
