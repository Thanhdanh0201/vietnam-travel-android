package com.example.vietnam_travel_itinerary_android.ui.notification

import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onItineraryInviteHandled: () -> Unit = {},
    viewModel: NotificationViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabUnreadCounts by viewModel.tabUnreadCounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    // Trạng thái pull-to-refresh
    var isRefreshing by remember { mutableStateOf(false) }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds = emptySet()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6))) {
        AppBackTopBar(
            onBackClick = {
                if (isSelectionMode) exitSelectionMode() else onBack()
            },
            showLogo = !isSelectionMode,
            title = if (isSelectionMode) "Đã chọn ${selectedIds.size}" else null,
            trailingContent = {
                if (isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                selectedIds = if (selectedIds.size == notifications.size) {
                                    emptySet()
                                } else {
                                    notifications.map { it.id }.toSet()
                                }
                            },
                        ) {
                            Text(
                                if (selectedIds.size == notifications.size && notifications.isNotEmpty()) {
                                    "Bỏ chọn"
                                } else {
                                    "Chọn tất cả"
                                },
                                color = VNRed,
                                fontSize = 13.sp,
                            )
                        }
                        IconButton(
                            onClick = {
                                if (selectedIds.isEmpty()) return@IconButton
                                viewModel.deleteNotifications(selectedIds) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Đã xóa ${selectedIds.size} thông báo", Toast.LENGTH_SHORT).show()
                                        exitSelectionMode()
                                    } else {
                                        Toast.makeText(context, "Không thể xóa thông báo", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = selectedIds.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Xóa đã chọn",
                                tint = if (selectedIds.isNotEmpty()) VNRed else Color(0xFFCBD5E1),
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(
                            imageVector = Icons.Outlined.DoneAll,
                            contentDescription = "Đọc tất cả",
                            tint = VNRed,
                        )
                    }
                }
            },
        )

        if (!isSelectionMode) {
            Text(
                text = "Thông báo",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
            )
        }

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
            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshAll()
                    isRefreshing = false
                },
                modifier = Modifier.fillMaxSize()
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val isSelected = selectedIds.contains(notif.id)
                    NotificationCard(
                        notif = notif,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (isSelected) {
                                    selectedIds - notif.id
                                } else {
                                    selectedIds + notif.id
                                }
                            } else {
                                if (!notif.isRead) viewModel.markAsRead(notif.id)
                                navigateFromNotification(notif, onNavigate)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedIds = setOf(notif.id)
                            }
                        },
                        onFollowBack = {
                            val actorId = notif.actorId ?: return@NotificationCard
                            viewModel.followBack(notif.id, actorId) { success ->
                                if (!success) {
                                    Toast.makeText(context, "Theo dõi thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onAcceptInvite = {
                            val itineraryId = notif.itineraryId ?: return@NotificationCard
                            viewModel.acceptItineraryInvite(notif.id, itineraryId) { success ->
                                if (success) {
                                    onItineraryInviteHandled()
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
            } // end PullToRefreshBox
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationCard(
    notif: NotificationUiModel,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
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

    val borderModifier = if (isInvite && !isSelectionMode && notif.inviteStatus == ItineraryInviteStatus.PENDING) {
        Modifier.border(
            width = 1.5.dp,
            brush = Brush.linearGradient(listOf(VNRed, VNRedContainer)),
            shape = RoundedCornerShape(16.dp),
        )
    } else Modifier

    val interactionSource = remember { MutableInteractionSource() }
    val cardClickModifier = if (isSelectionMode) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) VNRed.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.92f),
        shadowElevation = if (isSelectionMode) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    enabled = false,
                    colors = CheckboxDefaults.colors(checkedColor = VNRed),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    ),
                )
            }
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
                Column(modifier = cardClickModifier) {
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
                }

                when (notif.type) {
                    NotificationType.FOLLOW -> {
                        if (!isSelectionMode) {
                            Spacer(Modifier.height(8.dp))
                            when (notif.followBackStatus) {
                                FollowBackStatus.SHOW_BUTTON -> {
                                    OutlinedButton(
                                        onClick = onFollowBack,
                                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(VNRed, VNRed))),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    ) {
                                        Text("THEO DÕI LẠI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VNRed)
                                    }
                                }
                                FollowBackStatus.FOLLOWED -> {
                                    Text(
                                        "✓ Đã theo dõi lại",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                            }
                        }
                    }
                    NotificationType.ITINERARY_INVITE -> {
                        if (!isSelectionMode) {
                            Spacer(Modifier.height(8.dp))
                            when (notif.inviteStatus) {
                                ItineraryInviteStatus.PENDING -> {
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
                                ItineraryInviteStatus.ACCEPTED -> {
                                    Text(
                                        "✓ Đã chấp nhận lời mời",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                                ItineraryInviteStatus.DECLINED -> {
                                    Text(
                                        "Đã từ chối lời mời",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF94A3B8),
                                    )
                                }
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
        NotificationType.FOLLOW -> when (notif.followBackStatus) {
            FollowBackStatus.FOLLOWED -> "Bạn đã theo dõi lại $actor"
            FollowBackStatus.SHOW_BUTTON -> "$actor đã bắt đầu theo dõi bạn"
        }
        NotificationType.REACTION -> "$actor$extra đã thả tim bài viết của bạn"
        NotificationType.COMMENT -> "$actor đã bình luận bài viết của bạn"
        NotificationType.COMMENT_REACTION -> "$actor đã thích bình luận của bạn"
        NotificationType.REPOST -> "$actor đã đăng lại bài viết của bạn"
        NotificationType.MENTION -> "$actor đã nhắc đến bạn"
        NotificationType.ACHIEVEMENT -> "Bạn đã đạt thành tựu mới!"
        NotificationType.ITINERARY_INVITE -> when (notif.inviteStatus) {
            ItineraryInviteStatus.ACCEPTED ->
                "Bạn đã chấp nhận lời mời cộng tác lịch trình${notif.itineraryTitle?.let { " \"$it\"" } ?: ""}"
            ItineraryInviteStatus.DECLINED ->
                "Bạn đã từ chối lời mời cộng tác lịch trình${notif.itineraryTitle?.let { " \"$it\"" } ?: ""}"
            ItineraryInviteStatus.PENDING ->
                "$actor mời bạn cộng tác lịch trình${notif.itineraryTitle?.let { " \"$it\"" } ?: ""}"
        }
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
        else -> notif.postId?.let { postId ->
            if (notif.commentId != null) {
                onNavigate("post_detail/$postId/comment/${notif.commentId}")
            } else {
                onNavigate("post_detail/$postId")
            }
        }
    }
}
