package com.example.vietnam_travel_itinerary_android.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.data.dto.AdminReportResponse
import com.example.vietnam_travel_itinerary_android.ui.auth.AppViewModelProvider
import com.example.vietnam_travel_itinerary_android.ui.components.DrawerPageTopBar
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

private val reportTabs = listOf(
    "pending" to "Chờ xử lý",
    "resolved" to "Đã xử lý",
    "dismissed" to "Bỏ qua",
)

private fun reasonLabel(reason: String?): String = when (reason) {
    "spam" -> "Spam"
    "harassment" -> "Quấy rối"
    "inappropriate" -> "Nội dung không phù hợp"
    "misinformation" -> "Thông tin sai lệch"
    else -> "Khác"
}

private fun initials(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        else -> parts.first().take(1).uppercase()
    }
}

private fun avatarColor(name: String?): Color {
    val palette = listOf(
        0xFF64748B, 0xFF0EA5E9, 0xFF8B5CF6, 0xFFEC4899, 0xFFF97316, 0xFF22C55E,
    )
    val hash = (name ?: "?").sumOf { it.code }
    return Color(palette[hash % palette.size])
}

private data class ReportTargetInfo(
    val prefix: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val userId: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onMenuClick: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    onOpenPost: (postId: String, commentId: String?) -> Unit = { _, _ -> },
    viewModel: AdminReportsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.load("pending") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
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
                title = "Quản lý báo cáo",
                onMenuClick = onMenuClick,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val selectedTabIndex = reportTabs.indexOfFirst { it.first == uiState.status }.coerceAtLeast(0)
            TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White, contentColor = VNRed) {
                reportTabs.forEachIndexed { index, (status, label) ->
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
                when {
                    uiState.isLoading && uiState.reports.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VNRed)
                        }
                    }
                    uiState.reports.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không có báo cáo nào.", color = SlateGray500)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.reports, key = { it.id }) { report ->
                                ReportCard(
                                    report = report,
                                    isPending = uiState.status == "pending",
                                    isProcessing = uiState.actionInProgressId == report.id,
                                    viewModel = viewModel,
                                    onOpenProfile = onOpenProfile,
                                    onOpenPost = onOpenPost,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: AdminReportResponse,
    isPending: Boolean,
    isProcessing: Boolean,
    viewModel: AdminReportsViewModel,
    onOpenProfile: (String) -> Unit,
    onOpenPost: (postId: String, commentId: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBanDialog by remember { mutableStateOf(false) }

    val canOpenContent = report.reportedPostId != null ||
        report.reportedCommentPostId != null ||
        report.reportedUserId != null

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (canOpenContent) {
                            Modifier.clickable {
                                when {
                                    report.reportedPostId != null -> onOpenPost(report.reportedPostId, null)
                                    report.reportedCommentPostId != null -> onOpenPost(
                                        report.reportedCommentPostId,
                                        report.reportedCommentId,
                                    )
                                    report.reportedUserId != null -> onOpenProfile(report.reportedUserId)
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Surface(color = VNRed.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        reasonLabel(report.reason),
                        color = VNRed,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))

                val targetInfo = when {
                    report.reportedPostId != null -> ReportTargetInfo(
                        prefix = "Bài viết của",
                        name = report.reportedPostAuthorName,
                        avatarUrl = report.reportedPostAuthorAvatar,
                        userId = report.reportedPostAuthorId,
                    )
                    report.reportedCommentId != null -> ReportTargetInfo(
                        prefix = "Bình luận của",
                        name = report.reportedCommentAuthorName,
                        avatarUrl = report.reportedCommentAuthorAvatar,
                        userId = report.reportedCommentAuthorId,
                    )
                    report.reportedUserId != null -> ReportTargetInfo(
                        prefix = "Người dùng",
                        name = report.reportedUserName,
                        avatarUrl = report.reportedUserAvatar,
                        userId = report.reportedUserId,
                    )
                    else -> ReportTargetInfo(prefix = "Nội dung")
                }

                ReportUserRow(
                    prefix = targetInfo.prefix,
                    name = targetInfo.name,
                    avatarUrl = targetInfo.avatarUrl,
                    userId = targetInfo.userId,
                    onProfileClick = onOpenProfile,
                    nameStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )

                val content = report.reportedPostContent ?: report.reportedCommentContent
                if (!content.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        content,
                        modifier = Modifier.fillMaxWidth(),
                        color = SlateGray500,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                    )
                }
                if (!report.description.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Mô tả: ${report.description}",
                        modifier = Modifier.fillMaxWidth(),
                        color = SlateGray500,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(8.dp))
                ReportUserRow(
                    prefix = "Người báo cáo:",
                    name = report.reporterName,
                    avatarUrl = report.reporterAvatar,
                    userId = report.reporterId,
                    onProfileClick = onOpenProfile,
                )
            }

            if (isPending) {
                Spacer(Modifier.height(12.dp))
                if (isProcessing) {
                    LinearProgressIndicator(color = VNRed, modifier = Modifier.fillMaxWidth())
                } else {
                    FlowActions(
                        report = report,
                        onDeletePost = { viewModel.deletePost(report.id) },
                        onDeleteComment = { viewModel.deleteComment(report.id) },
                        onBanUser = { showBanDialog = true },
                        onDismiss = { viewModel.dismiss(report.id) },
                    )
                }
            }
        }
    }

    if (showBanDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = { Text("Cấm người dùng") },
            text = {
                Column {
                    ReportUserRow(
                        prefix = "Cấm",
                        name = report.reportedUserName,
                        avatarUrl = report.reportedUserAvatar,
                        userId = report.reportedUserId,
                        onProfileClick = onOpenProfile,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Lý do") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showBanDialog = false
                    report.reportedUserId?.let { viewModel.banUser(report.id, it, reason.takeIf { r -> r.isNotBlank() }) }
                }) { Text("Cấm", color = VNRed) }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = false }) { Text("Hủy") }
            },
        )
    }
}

@Composable
private fun ReportUserRow(
    prefix: String,
    name: String?,
    avatarUrl: String?,
    userId: String?,
    onProfileClick: (String) -> Unit,
    nameStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(prefix, color = SlateGray500, style = MaterialTheme.typography.bodySmall)
        if (userId != null) {
            Row(
                modifier = Modifier.clickable { onProfileClick(userId) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AuthorAvatar(
                    initials = initials(name),
                    color = avatarColor(name),
                    avatarUrl = avatarUrl.orEmpty(),
                    size = 28,
                )
                Text(
                    name ?: "?",
                    style = nameStyle,
                    color = SlateGray900,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Text(name ?: "?", style = nameStyle, color = SlateGray900)
        }
    }
}

@Composable
private fun FlowActions(
    report: AdminReportResponse,
    onDeletePost: () -> Unit,
    onDeleteComment: () -> Unit,
    onBanUser: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (report.reportedPostId != null) {
                OutlinedButton(
                    onClick = onDeletePost,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VNRed),
                ) { Text("Xóa bài") }
            }
            if (report.reportedCommentId != null) {
                OutlinedButton(
                    onClick = onDeleteComment,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VNRed),
                ) { Text("Xóa bình luận") }
            }
            if (report.reportedUserId != null) {
                Button(
                    onClick = onBanUser,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                ) { Text("Cấm user") }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Bỏ qua") }
        }
    }
}
