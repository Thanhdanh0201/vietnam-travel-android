package com.example.vietnam_travel_itinerary_android.ui.suggestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionResponse
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.DrawerPageTopBar
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray400
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlaceSuggestionsScreen(
    onMenuClick: () -> Unit,
    onAddNew: () -> Unit,
    viewModel: PlaceSuggestionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMySuggestions()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            DrawerPageTopBar(
                title = "Đề xuất của tôi",
                onMenuClick = onMenuClick,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNew,
                containerColor = VNRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Đề xuất mới") },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading && uiState.suggestions.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VNRed)
                    }
                }
                uiState.suggestions.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.suggestions, key = { it.id }) { item ->
                            SuggestionCard(item)
                        }
                        item { Spacer(Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.Place,
            contentDescription = null,
            tint = SlateGray400,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Bạn chưa có đề xuất nào",
            fontWeight = FontWeight.Bold,
            color = SlateGray900,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Hãy đề xuất một địa điểm thú vị để chia sẻ với cộng đồng.",
            color = SlateGray500,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SuggestionCard(item: PlaceSuggestionResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = SlateGray400)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = item.name ?: "",
                            fontWeight = FontWeight.Bold,
                            color = SlateGray900,
                            modifier = Modifier.weight(1f),
                        )
                        StatusBadge(item.status)
                    }
                    Spacer(Modifier.height(4.dp))
                    val subtitle = listOfNotNull(
                        item.provinceName?.takeIf { it.isNotBlank() },
                        item.type?.takeIf { it.isNotBlank() },
                    ).joinToString(" • ")
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, color = SlateGray500, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (item.status == "rejected" && !item.adminNote.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Lý do từ chối: ${item.adminNote}",
                        color = Color(0xFFB91C1C),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (label, bg, fg) = when (status) {
        "approved" -> Triple("Đã duyệt", Color(0xFFDCFCE7), Color(0xFF15803D))
        "rejected" -> Triple("Từ chối", Color(0xFFFEE2E2), Color(0xFFB91C1C))
        else -> Triple("Đang chờ", Color(0xFFFEF3C7), Color(0xFFB45309))
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}
