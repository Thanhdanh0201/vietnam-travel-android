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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.components.ItineraryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(itineraries: List<Itinerary>) {

    //FILTER STATE
    var filter by remember { mutableStateOf("all") }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Create new itinerary */ },
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

            //TITLE
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

            //FILTER BUTTONS
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { filter = "all" }
                ) {
                    Text("All")
                }

                Button(
                    onClick = { filter = "upcoming" }
                ) {
                    Text("Upcoming")
                }

                Button(
                    onClick = { filter = "past" }
                ) {
                    Text("Past")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //APPLY FILTER LOGIC
            val filteredList = when (filter) {
                "upcoming" -> itineraries.filter { it.isUpcoming }
                "past" -> itineraries.filter { !it.isUpcoming }
                else -> itineraries
            }

            //LIST
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
}