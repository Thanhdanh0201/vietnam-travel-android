package com.example.vietnam_travel_itinerary_android.ui.components.introduction

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Festival
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.example.vietnam_travel_itinerary_android.data.model.Event
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.model.PlaceDetail
import com.example.vietnam_travel_itinerary_android.data.model.PlaceReview
import com.example.vietnam_travel_itinerary_android.data.model.displayLocation
import com.example.vietnam_travel_itinerary_android.data.model.toPlace
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import com.example.vietnam_travel_itinerary_android.ui.theme.BackgroundLight
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray300
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray600
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray800
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PlaceDescriptionMinHeight = 160.dp
private val PlaceHeroHeight = 288.dp

/** Fade ảnh hero xuống nền sheet — không phủ trắng lên đầu ảnh. */
private val HeroBottomFade = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color.Transparent,
        BackgroundLight,
    ),
)

@Composable
private fun IntroductionHeroImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val request: ImageRequest? = remember(imageUrl, context) {
        val url = imageUrl?.trim()?.takeIf { it.isNotBlank() } ?: return@remember null
        ImageRequest.Builder(context)
            .data(url)
            .build()
    }
    if (request == null) {
        Box(
            modifier = modifier.background(SlateGray300),
        )
    } else {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SlateGray300),
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SlateGray300),
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceIntroductionOverlay(
    place: Place,
    onDismiss: () -> Unit,
    onExplore: (Place) -> Unit,
) {
    val placeRepo = remember { PlaceRepository() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var detail by remember(place.id) { mutableStateOf<PlaceDetail?>(null) }
    var reviewsLoading by remember(place.id) { mutableStateOf(false) }
    var detailLoadFailed by remember(place.id) { mutableStateOf(false) }
    var selectedTab by remember(place.id) { mutableIntStateOf(0) }
    var showWriteReview by remember { mutableStateOf(false) }
    var reviewRefreshTick by remember(place.id) { mutableIntStateOf(0) }

    // Tải mô tả khi mở overlay (tab Mô tả)
    LaunchedEffect(place.id) {
        placeRepo.getPlaceDetail(place.id)
            .onSuccess { detail = it; detailLoadFailed = false }
    }

    // Mỗi lần chuyển sang tab Bình luận → loading + fetch mới
    LaunchedEffect(place.id, selectedTab, reviewRefreshTick) {
        if (selectedTab != 1) return@LaunchedEffect
        reviewsLoading = true
        detailLoadFailed = false
        placeRepo.getPlaceDetail(place.id)
            .onSuccess { detail = it; detailLoadFailed = false }
            .onFailure { detailLoadFailed = true }
        reviewsLoading = false
    }

    // Tab Bình luận: tải lại mỗi 40s (nền, không loading)
    LaunchedEffect(place.id, selectedTab, showWriteReview) {
        if (selectedTab != 1 || showWriteReview) return@LaunchedEffect
        while (true) {
            delay(40_000L)
            placeRepo.getPlaceDetail(place.id)
                .onSuccess { detail = it; detailLoadFailed = false }
        }
    }

    val resolved = detail?.toPlace() ?: place
    val reviews = detail?.reviews.orEmpty()
    val province = resolved.provinces?.name ?: resolved.cities?.name.orEmpty()
    val locationLine = if (province.isNotBlank()) "$province, Việt Nam" else "Việt Nam"
    val typeLabel = placeTypeLabel(resolved.type)
    val typeIcon = placeTypeIcon(resolved.type)
    val description = resolved.description?.takeIf { it.isNotBlank() }
        ?: "Khám phá vẻ đẹp và văn hóa đặc trưng của điểm đến này — thông tin chi tiết sẽ được cập nhật từ hệ thống."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PlaceHeroHeight),
                ) {
                    IntroductionHeroImage(
                        imageUrl = resolved.imageUrl,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HeroBottomFade),
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Đóng",
                            tint = SlateGray900,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(VNRed.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = VNRed,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = typeLabel.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = VNRed,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = resolved.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray900,
                            lineHeight = 36.sp,
                            modifier = Modifier.weight(1f),
                        )
                        resolved.rating?.takeIf { it > 0 }?.let { rating ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VNRed.copy(alpha = 0.08f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = VNRed,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f", rating),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = VNRed,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = VNRed,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = locationLine,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = VNRed,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PlaceOverlayTabBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTab == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = PlaceDescriptionMinHeight)
                                .clip(RoundedCornerShape(8.dp)),
                        ) {
                            PlaceDescriptionTab(text = description)
                        }
                    }
                }

                if (selectedTab == 1) {
                    PlaceReviewFeedSection(
                        reviews = reviews,
                        loading = reviewsLoading,
                        loadFailed = detailLoadFailed,
                        onWriteReviewClick = { showWriteReview = true },
                    )
                }
            }

            if (selectedTab == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundLight)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Button(
                        onClick = { showWriteReview = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        Text(
                            text = "Viết đánh giá",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(VNRed, VNRed.copy(alpha = 0.6f), VNRed),
                        ),
                    ),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp),
        )

        PlaceWriteReviewSheet(
            placeName = resolved.name,
            placeId = place.id,
            visible = showWriteReview,
            onDismiss = { showWriteReview = false },
            onSubmitted = { reviewRefreshTick++ },
            onNeedLogin = {
                scope.launch {
                    snackbarHostState.showSnackbar("Đăng nhập để gửi đánh giá")
                }
            },
            onError = { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            },
        )
    }
}

@Composable
private fun PlaceOverlayTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf("Mô tả", "Bình luận")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onTabSelected(index) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTab == index) VNRed else SlateGray500,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (selectedTab == index) VNRed else Color.Transparent,
                            ),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(VNRed.copy(alpha = 0.15f)),
        )
    }
}

@Composable
private fun PlaceDescriptionTab(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = SlateGray600,
        lineHeight = 26.sp,
    )
}

private fun placeTypeLabel(type: String?): String {
    val key = type?.lowercase(Locale.getDefault())?.trim().orEmpty()
    return when {
        key.contains("historical") || key.contains("lịch sử") || key.contains("di tích") ->
            "Di tích lịch sử"
        key.contains("natural") || key.contains("thiên nhiên") -> "Thiên nhiên"
        key.contains("cultural") || key.contains("văn hóa") -> "Văn hóa"
        key.contains("entertainment") || key.contains("giải trí") -> "Giải trí"
        key.contains("shopping") || key.contains("mua sắm") -> "Mua sắm"
        key.contains("food") || key.contains("ẩm thực") -> "Ẩm thực"
        key.contains("resort") || key.contains("nghỉ dưỡng") -> "Nghỉ dưỡng"
        key.contains("heritage") || key.contains("di sản") -> "Di sản thế giới"
        type.isNullOrBlank() -> "Điểm đến"
        else -> type.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
}

private fun placeTypeIcon(type: String?): androidx.compose.ui.graphics.vector.ImageVector {
    val key = type?.lowercase(Locale.getDefault()).orEmpty()
    return when {
        key.contains("historical") || key.contains("lịch sử") || key.contains("di tích") ->
            Icons.Outlined.AccountBalance
        key.contains("food") || key.contains("ẩm thực") -> Icons.Outlined.Restaurant
        key.contains("shopping") || key.contains("mua sắm") -> Icons.Outlined.ShoppingBag
        key.contains("entertainment") || key.contains("giải trí") -> Icons.Outlined.Explore
        key.contains("resort") || key.contains("nghỉ dưỡng") -> Icons.Outlined.WbSunny
        key.contains("cultural") || key.contains("văn hóa") -> Icons.Outlined.Festival
        else -> Icons.Outlined.Landscape
    }
}

private fun formatReviewDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return raw.take(10).replace('-', '/')
}

@Composable
fun FestivalIntroductionOverlay(
    event: Event,
    onDismiss: () -> Unit,
    onSchedule: (Event) -> Unit,
) {
    val locationName = event.displayLocation()
    val dateRangeText = remember(event.startDate, event.endDate) {
        formatEventDateRange(event.startDate, event.endDate)
    }
    val subtitle =
        event.description?.lines()?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: "Sự kiện văn hóa đặc sắc tại địa phương."
    val bodyPrimary = event.description?.takeIf { it.isNotBlank() }
        ?: "Lễ hội là dịp để tôn vinh nét văn hóa bản địa, quảng bá du lịch và kết nối cộng đồng."
    val bodySecondary =
        event.description?.lines()?.drop(1)?.joinToString("\n")?.takeIf { it.isNotBlank() }
            ?: "Du khách được trải nghiệm không gian nghệ thuật, ẩm thực và các hoạt động giao lưu đặc sắc."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(VNRed.copy(alpha = 0.04f), Color.Transparent),
                        radius = 800f,
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                ) {
                    IntroductionHeroImage(
                        imageUrl = event.imageUrl,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HeroBottomFade),
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Đóng",
                            tint = SlateGray900,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 20.dp),
                ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(VNRed)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Festival,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                "Lễ hội văn hóa",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = Color.White,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray900,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VNRed,
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        InfoBlock(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Thời gian diễn ra",
                            value = dateRangeText,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoBlock(
                            icon = Icons.Outlined.Place,
                            label = "Địa điểm",
                            value = locationName.ifBlank { "Cập nhật sau" },
                            modifier = Modifier.fillMaxWidth(),
                            minHeightValue = 48.dp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, null, tint = VNRed, modifier = Modifier.size(22.dp))
                            Text(
                                "Ý nghĩa văn hóa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray900,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(VNRed.copy(alpha = 0.3f)),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = bodyPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SlateGray600,
                                lineHeight = 26.sp,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = bodySecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SlateGray600,
                            lineHeight = 26.sp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Button(
                    onClick = { onSchedule(event) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                ) {
                    Text(
                        "LÊN LỊCH",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(VNRed, VNRed.copy(alpha = 0.6f), VNRed),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun InfoBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    minHeightValue: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VNRed.copy(alpha = 0.05f))
            .border(1.dp, VNRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VNRed),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = SlateGray500,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = SlateGray900,
                lineHeight = 24.sp,
                modifier = Modifier.heightIn(min = minHeightValue),
            )
        }
    }
}

private fun formatEventDateRange(start: String, end: String): String {
    return try {
        val fmtIn = DateTimeFormatter.ISO_LOCAL_DATE
        val d1 = LocalDate.parse(start.take(10), fmtIn)
        val d2 = LocalDate.parse(end.take(10), fmtIn)
        val fmtOut = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi"))
        "${d1.format(fmtOut)} – ${d2.format(fmtOut)}"
    } catch (_: Exception) {
        "$start – $end"
    }
}
