package com.example.vietnam_travel_itinerary_android.ui.itinerary
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.ui.components.AppTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.ItineraryCard
import com.example.vietnam_travel_itinerary_android.ui.theme.*
import com.example.vietnam_travel_itinerary_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    viewModel: ItineraryViewModel = viewModel(),
    unreadCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {

    // State cho việc lọc danh sách
    var filter by remember { mutableStateOf("all") }
    val uiState by viewModel.uiState.collectAsState()
    val itineraries = uiState.itineraries

    // State cho việc xóa lịch trình
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }

    // Fetch itineraries when screen is shown
    LaunchedEffect(Unit) {
        viewModel.fetchItineraries()
    }

    // Fetch collaborators mỗi khi danh sách itinerary thay đổi (đảm bảo hiển thị participants)
    LaunchedEffect(itineraries) {
        itineraries.forEach { itinerary ->
            viewModel.fetchCollaborators(itinerary.id)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F6F6),
        topBar = {
            Column {
                AppTopBar(
                    onSearchClick = onSearchClick,
                    onNotificationClick = onNotificationClick,
                    unreadCount = unreadCount,
                    onMenuClick = onMenuClick,
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateClick,
                containerColor = VNRed,
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
            Spacer(modifier = Modifier.height(24.dp))

            // Tiêu đề
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(VNRed)
                )
                Text(
                    text = "Lịch trình của tôi",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp,
                    color = SlateGray900
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thanh Filter (đã làm đẹp lại so với bản cũ)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = filter == "all",
                    onClick = { filter = "all" },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = filter == "upcoming",
                    onClick = { filter = "upcoming" },
                    label = { Text("Sắp diễn ra") }
                )
                FilterChip(
                    selected = filter == "past",
                    onClick = { filter = "past" },
                    label = { Text("Đã đi") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logic lọc danh sách
            val filteredList = when (filter) {
                "upcoming" -> itineraries.filter { it.isUpcoming }
                "past" -> itineraries.filter { !it.isUpcoming }
                else -> itineraries
            }

            // Danh sách lịch trình
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { itinerary ->
                    val participants = uiState.participantsMap[itinerary.id] ?: emptyList()
                    ItineraryCard(
                        itinerary = itinerary,
                        participants = participants,
                        canDelete = itinerary.myRole == "OWNER",
                        onClick = onEditClick,
                        onShareClick = onShareClick,
                        onDelete = {
                            itineraryToDelete = itinerary
                        }
                    )
                }
            }
            }
        }
    }

    // Hộp thoại xác nhận xóa lịch trình
    itineraryToDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { itineraryToDelete = null },
            title = {
                Text(
                    text = "Xóa lịch trình",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateGray900
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa lịch trình \"${toDelete.title}\" không? Hành động này không thể hoàn tác.",
                    fontSize = 14.sp,
                    color = SlateGray600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItinerary(toDelete.id)
                        itineraryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itineraryToDelete = null }) {
                    Text("Hủy", color = SlateGray500)
                }
            },
            containerColor = Color.White
        )
    }
}