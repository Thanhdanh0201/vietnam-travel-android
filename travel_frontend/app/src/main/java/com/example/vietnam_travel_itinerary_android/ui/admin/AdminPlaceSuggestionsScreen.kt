package com.example.vietnam_travel_itinerary_android.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

private val statusTabs = listOf(
    "pending" to "Chờ duyệt",
    "approved" to "Đã duyệt",
    "rejected" to "Đã từ chối",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlaceSuggestionsScreen(
    onMenuClick: () -> Unit,
    viewModel: AdminPlaceSuggestionsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var detailItem by remember { mutableStateOf<PlaceSuggestionResponse?>(null) }

    LaunchedEffect(Unit) { viewModel.load("pending") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
            detailItem = null
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            DrawerPageTopBar(
                title = "Quản lý đề xuất",
                onMenuClick = onMenuClick,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Tìm theo tên, tỉnh, người đề xuất") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val selectedTabIndex = statusTabs.indexOfFirst { it.first == uiState.status }.coerceAtLeast(0)
            TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White, contentColor = VNRed) {
                statusTabs.forEachIndexed { index, (status, label) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.load(status) },
                        text = { Text(label, fontWeight = FontWeight.SemiBold) },
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                val visible = viewModel.visibleSuggestions
                when {
                    uiState.isLoading && visible.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VNRed)
                        }
                    }
                    visible.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không có đề xuất nào.", color = SlateGray500)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(visible, key = { it.id }) { item ->
                                AdminSuggestionCard(item, onClick = { detailItem = item })
                            }
                        }
                    }
                }
            }
        }
    }

    detailItem?.let { item ->
        SuggestionDetailSheet(
            item = item,
            isProcessing = uiState.actionInProgressId == item.id,
            onApprove = { viewModel.approve(item.id) },
            onReject = { note -> viewModel.reject(item.id, note) },
            onDismiss = { detailItem = null },
        )
    }
}

@Composable
private fun AdminSuggestionCard(item: PlaceSuggestionResponse, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
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
            Column(Modifier.weight(1f)) {
                Text(item.name ?: "", fontWeight = FontWeight.Bold, color = SlateGray900)
                Spacer(Modifier.height(2.dp))
                val sub = listOfNotNull(
                    item.provinceName?.takeIf { it.isNotBlank() },
                    item.type?.takeIf { it.isNotBlank() },
                ).joinToString(" • ")
                if (sub.isNotBlank()) Text(sub, color = SlateGray500, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Người đề xuất: ${item.userName ?: "?"}",
                    color = SlateGray500,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionDetailSheet(
    item: PlaceSuggestionResponse,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
            Text(item.name ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val sub = listOfNotNull(
                item.provinceName?.takeIf { it.isNotBlank() },
                item.type?.takeIf { it.isNotBlank() },
            ).joinToString(" • ")
            if (sub.isNotBlank()) Text(sub, color = SlateGray500)
            if (!item.description.isNullOrBlank()) Text(item.description!!, color = SlateGray900)
            Text("Người đề xuất: ${item.userName ?: "?"}", color = SlateGray500, style = MaterialTheme.typography.bodySmall)

            if (item.status == "pending") {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VNRed),
                    ) { Text("Từ chối") }
                    Button(
                        onClick = onApprove,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Duyệt")
                        }
                    }
                }
            } else if (item.status == "rejected" && !item.adminNote.isNullOrBlank()) {
                Text("Lý do từ chối: ${item.adminNote}", color = Color(0xFFB91C1C))
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showRejectDialog) {
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Từ chối đề xuất") },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Lý do (gửi cho người dùng)") },
                    minLines = 2,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    onReject(note.takeIf { it.isNotBlank() })
                }) { Text("Từ chối", color = VNRed) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Hủy") }
            },
        )
    }
}
