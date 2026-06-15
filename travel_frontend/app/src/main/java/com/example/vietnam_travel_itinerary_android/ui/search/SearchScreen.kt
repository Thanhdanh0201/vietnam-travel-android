package com.example.vietnam_travel_itinerary_android.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.example.vietnam_travel_itinerary_android.ui.components.post.PostCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.ItineraryCard
import com.example.vietnam_travel_itinerary_android.ui.components.PlaceCard
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar

@Composable
fun SearchScreen(
    state: SearchUiState,
    currentUserId: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onNavigate: (String) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,

) {


    Column(modifier = Modifier.fillMaxSize()) {

        AppBackTopBar(
            onBackClick = onBackClick,
            showActions = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = { onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            placeholder = { Text("Search places, itineraries...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = {
                        onFilterChange(filter)
                    },
                    label = {
                        Text(filter.title)
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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
                    PlaceCard(
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