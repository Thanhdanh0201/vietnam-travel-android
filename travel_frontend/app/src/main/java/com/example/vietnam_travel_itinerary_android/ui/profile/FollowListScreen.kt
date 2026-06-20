package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.data.model.FollowListType
import com.example.vietnam_travel_itinerary_android.data.model.FollowListUser
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    listType: FollowListType,
    uiState: FollowListUiState,
    currentUserId: String?,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val title = when (listType) {
        FollowListType.FOLLOWERS -> "Người theo dõi"
        FollowListType.FOLLOWING -> "Đang theo dõi"
    }

    Scaffold(
        containerColor = Color(0xFFF8F6F6),
        topBar = {
            AppBackTopBar(
                onBackClick = onBack,
                title = title,
                showActions = false,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = VNRed,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = uiState.error,
                            color = SlateGray500,
                            fontSize = 14.sp,
                        )
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
                uiState.users.isEmpty() -> {
                    Text(
                        text = when (listType) {
                            FollowListType.FOLLOWERS -> "Chưa có người theo dõi."
                            FollowListType.FOLLOWING -> "Chưa theo dõi ai."
                        },
                        color = SlateGray500,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(uiState.users, key = { it.id }) { user ->
                            FollowListUserRow(
                                user = user,
                                onClick = { onUserClick(user.id) },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = Color(0xFFE2E8F0),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowListUserRow(
    user: FollowListUser,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileAvatar(
            avatarUrl = user.avatarUrl,
            initials = user.avatarInitials,
            color = Color(user.avatarColor),
            size = 48,
            textSize = 18.sp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (user.isVerified) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = null,
                        tint = VNRed,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = user.explorerLevel.label,
                fontSize = 12.sp,
                color = SlateGray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
