package com.example.vietnam_travel_itinerary_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    Scaffold(
        containerColor = Color(0xFFF5F5F5), // Màu nền xám nhạt
        // Nút Tạo lịch trình mới nổi lên
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Xử lý sự kiện tạo mới */ },
                containerColor = Color(0xFFC21833), // Màu đỏ chủ đạo
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo lịch trình mới")
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

            // Tiêu đề
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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

            // Danh sách lịch trình
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Tránh bị che bởi Bottom Nav và FAB
            ) {
                items(itineraries) { itinerary ->
                    ItineraryCard(itinerary = itinerary)
                }
            }
        }
    }
}
