package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import kotlinx.coroutines.delay

data class FeaturedBannerItem(
    val title: String,
    val subtitle: String,
    val badge: String,
    val imageUrl: String,
)

object FeaturedBannerDefaults {
    private const val IMG_DA_QUY =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDZZxPFedvoaOhbzXxMv9k6jB3FOdDdf2g4pShyyV30ssGdEZaG3xu3Lpt5bhxy8xfqHvfJOnAEL9RWMWib8LIzWxdgq0BLk6P692h69F3ykZyNs7cVNxmS737g0kARtNd0_Ds1opyN0SjZd5xMOKjP8Qks_LQvJ5PREcLnZgp5KwH_ZVOcLvpNSvyGAHRV4BETRZfFA6MSeJm017zpr2XEX5RBmjsxRQy0OJoe_SASaSESVVEGoBN37S7fO58usQlDmHON0lU5Gw"
    private const val IMG_BEACH =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBuYPhU7veBhvyka3NMHanAx-Y6N4yOw_4B95lEq0nJ-OzAHD7tl9ytpqQG0-CAx_Q6zykZG51Ici2sRDg1BOKlPdCkEEWbxqoii178ChGB4MSA57L9I1fyLRHRc2CO5ej5XVIquw65kGR8BTpMbFc90ND0tCjPbbO5_ze9udApBzZXI-PH0JOJve86j2OTgV8_FS_T7oV5j8u16EE_Cfhyzl_iyaFlF_GtL9_kgPmSIk3SNnItk09Ej8RhC4sydSfovuwW0CLlXg"
    private const val IMG_MOC_CHAU =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCqQIFXQo_FEd8pAnt_27xOwgp06ex_ELbEr4Y8QnoVzQAKBuUTAuJJOj9VjhJnj0jIbKGWzRDzE8ZyKgjz7dEEXgLyAsFpuMeCesn_MagOCqPa0ZEXLMUnvjZ08_vlXHYoM6UyFoA6xjUbpnR2u3NzuOKCnHZ9JUT0DmOD8ITJXOoXQ8YRrqdh9YcW3bbtpKYB50UZ9QKY3Jf2-KAbmHaJAqRQDJojZvhyk12WYij-LVtnrBheMpfTITSjqONlYRD05CDCDU0IdQ"
    private const val IMG_HA_GIANG =
        "https://www.vietnambooking.com/wp-content/uploads/2019/10/kinh-nghiem-phuot-ha-giang-mua-lua-chin-24-10-2019-2.jpg"

    val slides: List<FeaturedBannerItem> = listOf(
        FeaturedBannerItem(
            title = "Mùa hoa dã quỳ",
            subtitle = "Khám phá vẻ đẹp vàng rực vùng cao nguyên",
            badge = "Mùa cao điểm",
            imageUrl = IMG_DA_QUY,
        ),
        FeaturedBannerItem(
            title = "Biển đẹp tháng 5",
            subtitle = "Phú Quốc, Nha Trang, Quy Nhơn — nắng vàng, biển xanh",
            badge = "Gợi ý hè",
            imageUrl = IMG_BEACH,
        ),
        FeaturedBannerItem(
            title = "Check-in Mộc Châu",
            subtitle = "Thung lũng hoa, đồi chè và homestay view núi",
            badge = "Điểm check-in",
            imageUrl = IMG_MOC_CHAU,
        ),
        FeaturedBannerItem(
            title = "Mùa lúa chín Hà Giang",
            subtitle = "Ruộng bậc thang vàng óng — tháng 9 đến tháng 11",
            badge = "Tây Bắc",
            imageUrl = IMG_HA_GIANG,
        ),
    )
}

private const val AUTO_SCROLL_MS = 4_500L
private val BannerHeight = 260.dp
private val BannerShape = RoundedCornerShape(12.dp)
private val BannerBadgeHeight = 28.dp
private val BannerTitleHeight = 72.dp
private val BannerSubtitleHeight = 44.dp

@Composable
fun FeaturedBanner(
    items: List<FeaturedBannerItem> = FeaturedBannerDefaults.slides,
    onExploreClick: (FeaturedBannerItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(AUTO_SCROLL_MS)
            if (pagerState.isScrollInProgress) continue
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(BannerHeight),
        ) { page ->
            FeaturedBannerSlide(
                item = items[page],
                onExploreClick = { onExploreClick(items[page]) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, _ ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) VNRed else VNRed.copy(alpha = 0.28f),
                        ),
                )
            }
        }
    }
}

@Composable
private fun FeaturedBannerSlide(
    item: FeaturedBannerItem,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BannerHeight)
            .clip(BannerShape),
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.8f),
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Box(modifier = Modifier.height(BannerBadgeHeight)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = VNRed,
                ) {
                    Text(
                        text = item.badge.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BannerTitleHeight),
                contentAlignment = Alignment.TopStart,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BannerSubtitleHeight),
                contentAlignment = Alignment.TopStart,
            ) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onExploreClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = VNRed,
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Khám phá ngay",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
