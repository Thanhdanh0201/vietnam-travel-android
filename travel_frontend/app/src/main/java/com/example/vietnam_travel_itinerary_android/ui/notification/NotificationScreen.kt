package com.example.vietnam_travel_itinerary_android.ui.notification

import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.AppBackTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRedContainer

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: NotificationViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabUnreadCounts by viewModel.tabUnreadCounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        AppBackTopBar(
            onBackClick = onBack,
            trailingContent = {
                IconButton(onClick = { viewModel.markAllAsRead() }) {
                    Icon(
                        imageVector = Icons.Outlined.DoneAll,
                        contentDescription = "Đọc tất cả",
                        tint = VNRed,
                    )
                }
            },
        )

        Text(
            text = "Thông báo",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
        )

        NotificationFilterTabs(
            selectedTab = selectedTab,
            tabUnreadCounts = tabUnreadCounts,
            onTabSelected = { viewModel.selectTab(it) },
        )

        if (isLoading && notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VNRed)
            }
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có thông báo", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notif = notif,
                        onClick = {
                            if (!notif.isRead) viewModel.markAsRead(notif.id)
                            navigateFromNotification(notif, onNavigate)
                        },
                        onFollowBack = { notif.actorId?.let { viewModel.followBack(it) } },
                        onAcceptInvite = {
                            val itineraryId = notif.itineraryId ?: return@NotificationCard
                            viewModel.acceptItineraryInvite(notif.id, itineraryId) { success ->
                                if (success) {
                                    onNavigate("itinerary_detail/$itineraryId")
                                } else {
                                    Toast.makeText(context, "Chấp nhận lời mời thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDeclineInvite = {
                            val itineraryId = notif.itineraryId ?: return@NotificationCard
                            viewModel.declineItineraryInvite(notif.id, itineraryId) { success ->
                                if (!success) {
                                    Toast.makeText(context, "Từ chối lời mời thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterTabs(
    selectedTab: NotifTab,
    tabUnreadCounts: Map<NotifTab, Int>,
    onTabSelected: (NotifTab) -> Unit,
) {
    val tabs = NotifTab.entries
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = Color.Transparent,
        contentColor = VNRed,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            val index = tabs.indexOf(selectedTab)
            if (index in tabPositions.indices) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[index])
                        .clip(RoundedCornerShape(50)),
                    color = VNRed,
                    height = 3.dp,
                )
            }
        },
        divider = {},
    ) {
        tabs.forEach { tab ->
            val unread = tabUnreadCounts[tab] ?: 0
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            tab.label,
                            fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                        if (unread > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VNRed),
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notif: NotificationUiModel,
    onClick: () -> Unit,
    onFollowBack: () -> Unit,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
) {
    val isInvite = notif.type == NotificationType.ITINERARY_INVITE
    val isSystem = notif.type in listOf(
        NotificationType.ITINERARY_UPDATED,
        NotificationType.PLACE_SUGGESTION_APPROVED,
        NotificationType.ACHIEVEMENT,
    )

    val borderModifier = if (isInvite) {
        Modifier.border(
            width = 1.5.dp,
            brush = Brush.linearGradient(listOf(VNRed, VNRedContainer)),
            shape = RoundedCornerShape(16.dp),
        )
    } else Modifier

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box {
                if (isSystem) {
                    SystemNotifIcon(type = notif.type)
                } else {
                    AuthorAvatar(
                        initials = notif.actorName.take(2).uppercase(),
                        color = VNRed,
                        size = 44,
                        avatarUrl = notif.actorAvatarUrl ?: "",
                    )
                    if (notif.groupedCount > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp),
                            shape = CircleShape,
                            color = VNRed,
                        ) {
                            Text(
                                "+${notif.groupedCount}",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                if (!notif.isRead) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(VNRed),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildNotifMessage(notif),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1E293B),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                notif.previewText?.takeIf { it.isNotBlank() }?.let { preview ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "\"$preview\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(notif.timeAgo, fontSize = 12.sp, color = Color(0xFF94A3B8))

                when (notif.type) {
                    NotificationType.FOLLOW -> {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onFollowBack,
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(VNRed, VNRed))),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text("THEO DÕI LẠI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VNRed)
                        }
                    }
                    NotificationType.ITINERARY_INVITE -> {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onAcceptInvite,
                                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text("CHẤP NHẬN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onDeclineInvite,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text("TỪ CHỐI", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                    else -> Unit
                }
            }

            if (notif.type == NotificationType.REACTION) {
                Icon(
                    Icons.Outlined.Favorite,
                    contentDescription = null,
                    tint = VNRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SystemNotifIcon(type: NotificationType) {
    val (icon, bg) = when (type) {
        NotificationType.PLACE_SUGGESTION_APPROVED -> Icons.Filled.CheckCircle to Color(0xFF22C55E)
        NotificationType.ITINERARY_UPDATED -> Icons.Filled.Map to VNRed
        else -> Icons.Filled.CheckCircle to VNRed
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = bg, modifier = Modifier.size(24.dp))
    }
}

private fun buildNotifMessage(notif: NotificationUiModel): String {
    val actor = notif.actorUsername?.let { "@$it" } ?: notif.actorName
    val extra = if (notif.groupedCount > 0) " và ${notif.groupedCount} người khác" else ""
    return when (notif.type) {
        NotificationType.FOLLOW -> "$actor đã bắt đầu theo dõi bạn"
        NotificationType.REACTION -> "$actor$extra đã thả tim bài viết của bạn"
        NotificationType.COMMENT -> "$actor đã bình luận bài viết của bạn"
        NotificationType.COMMENT_REACTION -> "$actor đã thích bình luận của bạn"
        NotificationType.REPOST -> "$actor đã đăng lại bài viết của bạn"
        NotificationType.MENTION -> "$actor đã nhắc đến bạn"
        NotificationType.ACHIEVEMENT -> "Bạn đã đạt thành tựu mới!"
        NotificationType.ITINERARY_INVITE -> "$actor mời bạn cộng tác lịch trình${notif.itineraryTitle?.let { " \"$it\"" } ?: ""}"
        NotificationType.ITINERARY_UPDATED -> "Lịch trình${notif.itineraryTitle?.let { " \"$it\"" } ?: ""} đã được cập nhật"
        NotificationType.PLACE_SUGGESTION_APPROVED -> "Đề xuất địa điểm của bạn đã được phê duyệt"
    }
}

private fun navigateFromNotification(notif: NotificationUiModel, onNavigate: (String) -> Unit) {
    when (notif.type) {
        NotificationType.FOLLOW -> notif.actorId?.let { onNavigate("profile/$it") }
        NotificationType.ITINERARY_INVITE, NotificationType.ITINERARY_UPDATED ->
            notif.itineraryId?.let { onNavigate("itinerary_detail/$it") }
        NotificationType.PLACE_SUGGESTION_APPROVED -> onNavigate("my_place_suggestions")
        else -> notif.postId?.let { /* post detail handled in community */ onNavigate("community") }
    }
}
