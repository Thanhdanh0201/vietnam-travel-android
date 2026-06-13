package com.example.vietnam_travel_itinerary_android.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.PlaceCard

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onPlaceClick: (Place) -> Unit, // keep it but optional now
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // PLACES FIRST
            if (state.places.isNotEmpty()) {
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
            if (state.itineraries.isNotEmpty()) {
                item {
                    Text(
                        text = "Itineraries",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(state.itineraries) { itinerary ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(itinerary.title)
                        }
                    }
                }
            }

            if (!state.isLoading &&
                state.places.isEmpty() &&
                state.itineraries.isEmpty() &&
                state.query.isNotBlank()
            ) {
                item {
                    Text("No results found")
                }
            }
        }
    }
}