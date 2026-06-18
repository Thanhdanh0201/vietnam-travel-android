package com.example.vietnam_travel_itinerary_android.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import com.example.vietnam_travel_itinerary_android.ui.components.post.PostCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.ItineraryCard
import com.example.vietnam_travel_itinerary_android.ui.components.PlaceCard
import com.example.vietnam_travel_itinerary_android.ui.components.PlaceSearchCard
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    currentUserId: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onNavigate: (String) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onTrendingClick: (String) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppBackTopBar(
            onBackClick = onBackClick,
            showActions = false
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = { onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Tìm kiếm địa điểm, lịch trình...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        val selectedTabIndex = SearchFilter.entries.indexOfFirst { it == state.selectedFilter }.coerceAtLeast(0)
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = VNRed,
            edgePadding = 16.dp,
        ) {
            SearchFilter.entries.forEachIndexed { index, filter ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onFilterChange(filter) },
                    text = { Text(filter.title, fontWeight = FontWeight.SemiBold) },
                )
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // TRENDING KEYWORDS — hiện khi chưa nhập từ khoá
            if (state.query.isBlank()) {
                if (state.isTrendingLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VNRed, modifier = Modifier.size(24.dp))
                        }
                    }
                } else if (state.trendingKeywords.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Whatshot,
                                    contentDescription = null,
                                    tint = VNRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Tìm kiếm phổ biến",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.trendingKeywords.forEach { keyword ->
                                    SuggestionChip(
                                        onClick = { onTrendingClick(keyword) },
                                        label = { Text(keyword, fontSize = 13.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
                return@LazyColumn
            }

            // PLACES FIRST
            if ((state.selectedFilter == SearchFilter.ALL ||
                        state.selectedFilter == SearchFilter.PLACES)
                && state.places.isNotEmpty()) {
                item {
                    Text(
                        text = "Places",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(state.places) { place ->
                    PlaceSearchCard(
                        place = place,
                        onPlaceClick = onPlaceClick
                    )
                }
            }

            // ITINERARIES
            if ((state.selectedFilter == SearchFilter.ALL ||
                        state.selectedFilter == SearchFilter.ITINERARIES)
                && state.itineraries.isNotEmpty()) {
                item {
                    Text(
                        text = "Itineraries",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(state.itineraries) { itinerary ->
                    ItineraryCard(
                        itinerary = itinerary,
                        participants = emptyList(), // or real data if you have it
                        canDelete = false, // search results usually shouldn't delete
                        onClick = { id ->
                            // navigate to itinerary detail
                        },
                        onShareClick = { id ->
                            // share logic
                        },
                        onDelete = {
                            // maybe no-op in search
                        }
                    )
                }
            }
            //post
            if ((state.selectedFilter == SearchFilter.ALL ||
                        state.selectedFilter == SearchFilter.POSTS)
                && state.posts.isNotEmpty()) {
                item {
                    Text(
                        text = "Posts",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(state.posts) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        onLikeClick = {},
                        onCommentClick = {},
                        onSaveClick = {},
                        onDeleteClick = {},
                        onItineraryClick = { itineraryId ->
                            onNavigate("itinerary_detail/$itineraryId")
                        },

                        onAuthorClick = {
                            val authorId = post.userId.takeIf { it.isNotBlank() } ?: return@PostCard

                            if (authorId == currentUserId) {
                                onNavigate("profile")
                            } else {
                                onNavigate("profile/$authorId")
                            }
                        }
                    )
                }
            }
            //users
            if ((state.selectedFilter == SearchFilter.ALL ||
                        state.selectedFilter == SearchFilter.USERS)
                && state.users.isNotEmpty()) {
                item {
                    Text(
                        text = "Users",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(state.users) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (user.id == currentUserId) {
                                    onNavigate("profile")
                                } else {
                                    onNavigate("profile/${user.id}")
                                }
                            }
                    ) {
                        AuthorAvatar(
                            initials = user.avatarInitials,
                            color = Color(user.avatarColor),
                            avatarUrl = user.avatarUrl,
                            size = 40
                        )

                        Text(user.name)
                    }
                }
            }

            if (!state.isLoading &&
                state.places.isEmpty() &&
                state.posts.isEmpty() &&
                state.itineraries.isEmpty() &&
                state.users.isEmpty() &&
                state.query.isNotBlank()
            ) {
                item {
                    Text("No results found")
                }
            }

        }
    }
}