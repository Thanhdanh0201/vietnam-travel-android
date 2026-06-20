package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

// ============================================================
// APP TOP BAR — dùng chung cho Home, Community, Profile, ...
// ============================================================

@Composable
fun AppTopBarLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = VNRed,
            modifier = Modifier.size(36.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = "VIETNAM TRAVEL",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = VNRed,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
fun AppTopBar(
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    unreadCount: Int = 0,
    onMenuClick: (() -> Unit)? = null,
) {
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = VNRed,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    AppTopBarLogo()
                }
                AppTopBarActions(
                    onSearchClick = onSearchClick,
                    onNotificationClick = onNotificationClick,
                    unreadCount = unreadCount,
                )
            }
        }
        HorizontalDivider(color = VNRed.copy(alpha = 0.05f))
    }
}

@Composable
fun AppBackTopBar(
    onBackClick: () -> Unit,
    title: String? = null,
    showLogo: Boolean = true,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    unreadCount: Int = 0,
    showActions: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = VNRed,
                        )
                    }
                    if (title != null) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SlateGray900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else if (showLogo) {
                        AppTopBarLogo()
                    }
                }

                when {
                    showActions -> AppTopBarActions(
                        onSearchClick = onSearchClick,
                        onNotificationClick = onNotificationClick,
                        unreadCount = unreadCount,
                    )
                    trailingContent != null -> trailingContent()
                }
            }
        }
        HorizontalDivider(color = VNRed.copy(alpha = 0.05f))
    }
}

@Composable
private fun AppTopBarActions(
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int = 0,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = VNRed,
                        contentColor = Color.White,
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
        ) {
            FilledIconButton(
                onClick = onNotificationClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = VNRed.copy(alpha = 0.1f),
                    contentColor = VNRed,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        FilledIconButton(
            onClick = onSearchClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = VNRed.copy(alpha = 0.1f),
                contentColor = VNRed,
            ),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Tìm kiếm",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
