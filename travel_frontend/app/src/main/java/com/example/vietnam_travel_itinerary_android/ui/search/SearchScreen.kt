package com.example.vietnam_travel_itinerary_android.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar

@Composable
fun SearchScreen(
    onBackClick: () -> Unit
) {
    Column {
        AppBackTopBar(
            onBackClick = onBackClick
        )

        Text("Search coming soon")
    }
}