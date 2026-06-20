package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.ui.components.post.AuthorAvatar
import com.example.vietnam_travel_itinerary_android.ui.theme.*

data class InviteMemberCandidate(
    val id: String,
    val name: String,
    val username: String = "",
    val avatarUrl: String = "",
    val avatarInitials: String = "",
    val avatarColor: Long = 0xFF64748B,
)

@Composable
fun MemberSearchPicker(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    friends: List<InviteMemberCandidate>,
    community: List<InviteMemberCandidate>,
    selectedMember: InviteMemberCandidate?,
    onSelectMember: (InviteMemberCandidate) -> Unit,
    onClearSelection: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selectedMember != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = VNRed.copy(alpha = 0.08f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AuthorAvatar(
                        initials = selectedMember.avatarInitials,
                        color = Color(selectedMember.avatarColor),
                        avatarUrl = selectedMember.avatarUrl,
                        size = 32,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedMember.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SlateGray900)
                        if (selectedMember.username.isNotBlank()) {
                            Text(selectedMember.username, fontSize = 11.sp, color = SlateGray500)
                        }
                    }
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Bỏ chọn",
                        tint = SlateGray400,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onClearSelection),
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateGray200),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Search, null, tint = SlateGray400, modifier = Modifier.size(18.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("Tìm theo tên người dùng...", color = SlateGray400, fontSize = 13.sp)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp, color = SlateGray900),
                            cursorBrush = SolidColor(VNRed),
                            singleLine = true,
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Xóa",
                            tint = SlateGray400,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onSearchQueryChange("") },
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = VNRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
                searchQuery.length < 2 -> {
                    Text(
                        "Nhập ít nhất 2 ký tự để tìm",
                        fontSize = 12.sp,
                        color = SlateGray400,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                friends.isEmpty() && community.isEmpty() -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.SearchOff, null, tint = SlateGray300, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Không tìm thấy người dùng", fontSize = 12.sp, color = SlateGray400)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        if (friends.isNotEmpty()) {
                            item {
                                Text(
                                    "Bạn bè",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SlateGray500,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                                )
                            }
                            items(friends, key = { "friend-${it.id}" }) { user ->
                                MemberSearchResultRow(user = user, onClick = { onSelectMember(user) })
                            }
                        }
                        if (community.isNotEmpty()) {
                            item {
                                Text(
                                    "Cộng đồng",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SlateGray500,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                                )
                            }
                            items(community, key = { "community-${it.id}" }) { user ->
                                MemberSearchResultRow(user = user, onClick = { onSelectMember(user) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberSearchResultRow(
    user: InviteMemberCandidate,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AuthorAvatar(
            initials = user.avatarInitials,
            color = Color(user.avatarColor),
            avatarUrl = user.avatarUrl,
            size = 36,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                user.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = SlateGray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (user.username.isNotBlank()) {
                Text(
                    user.username,
                    fontSize = 11.sp,
                    color = SlateGray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(Icons.Outlined.Person, null, tint = SlateGray300, modifier = Modifier.size(16.dp))
    }
    HorizontalDivider(color = SlateGray100)
}
