package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.vietnam_travel_itinerary_android.data.model.WeatherCityPresets
import com.example.vietnam_travel_itinerary_android.data.model.WeatherCitySlide
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@Composable
fun WeatherWidgetCarousel(
    slides: List<WeatherCitySlide>,
    favoriteCityId: String,
    favoriteScrollTick: Long,
    onFavoriteCityChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (slides.isEmpty()) return

    val initialIndex = WeatherCityPresets.pageIndexForCity(favoriteCityId)
        .coerceIn(0, slides.lastIndex)

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { slides.size },
    )

    LaunchedEffect(favoriteCityId, favoriteScrollTick, slides.size) {
        val target = WeatherCityPresets.pageIndexForCity(favoriteCityId).coerceIn(0, slides.lastIndex)
        pagerState.animateScrollToPage(target)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(WeatherWidgetHeight),
            beyondViewportPageCount = 1,
        ) { page ->
            val slide = slides[page]
            val isFavorite = slide.id == favoriteCityId
            WeatherWidget(
                weather = slide.weather,
                locationName = slide.displayName,
                locationSubtitle = slide.subtitle,
                isLoading = slide.isLoading,
                loadFailed = slide.loadFailed,
                isFavorite = isFavorite,
                onFavoriteClick = { onFavoriteCityChange(slide.id) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slides.forEachIndexed { index, slide ->
                val selected = pagerState.currentPage == index
                val isFav = slide.id == favoriteCityId
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                selected && isFav -> VNRed
                                selected -> VNRed.copy(alpha = 0.85f)
                                isFav -> VNRed.copy(alpha = 0.45f)
                                else -> VNRed.copy(alpha = 0.25f)
                            },
                        ),
                )
            }
        }
    }
}
