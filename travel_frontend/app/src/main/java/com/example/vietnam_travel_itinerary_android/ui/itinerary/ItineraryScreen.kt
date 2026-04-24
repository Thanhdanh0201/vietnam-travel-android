package com.example.vietnam_travel_itinerary_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.components.ItineraryCard
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel
import com.example.vietnam_travel_itinerary_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(viewModel: ItineraryViewModel = viewModel()) {

    val itineraries = viewModel.itineraries

    // NEW: dialog state
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // FILTER STATE
    var filter by remember { mutableStateOf("all") }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),

        floatingActionButton = {
            ExtendedFloatingActionButton(
                // CHANGED: open dialog instead of creating instantly
                onClick = { showDialog = true },
                containerColor = Color(0xFFC21833),
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo lịch trình mới", fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // TITLE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(Color(0xFFC21833), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lịch trình của tôi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FILTER BUTTONS
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { filter = "all" }) {
                    Text("All")
                }

                Button(onClick = { filter = "upcoming" }) {
                    Text("Upcoming")
                }

                Button(onClick = { filter = "past" }) {
                    Text("Past")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // APPLY FILTER LOGIC
            val filteredList = when (filter) {
                "upcoming" -> itineraries.filter { it.isUpcoming }
                "past" -> itineraries.filter { !it.isUpcoming }
                else -> itineraries
            }

            // LIST
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList) { itinerary ->
                    ItineraryCard(itinerary = itinerary)
                }
            }
        }
    }

    // NEW: Dialog (PUT THIS OUTSIDE Scaffold but inside function)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },

            confirmButton = {
                Button(onClick = {

                    val newItinerary = Itinerary(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        location = location,
                        dateRange = "Custom",
                        isUpcoming = true,
                        imageResId = R.drawable.ic_launcher_background,
                        statusText = "Sắp diễn ra",
                        statusSubText = null,
                        participantImages = emptyList()
                    )

                    viewModel.createItinerary(newItinerary)

                    // reset + close
                    title = ""
                    location = ""
                    showDialog = false
                }) {
                    Text("Save")
                }
            },

            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },

            title = { Text("Tạo lịch trình") },

            text = {
                Column {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") }
                    )
                }
            }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun ItineraryScreenPreview() {
    ItineraryScreen()
}