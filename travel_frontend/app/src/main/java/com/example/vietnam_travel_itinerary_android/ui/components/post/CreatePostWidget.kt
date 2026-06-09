package com.example.vietnam_travel_itinerary_android.ui.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.ui.theme.*

// ============================================================
// CREATE POST WIDGET — dùng chung ở Community + Profile
// ============================================================

@Composable
fun CreatePostWidget(
    avatarInitials: String = "U",
    avatarColor: Long = 0xFFC6102E,
    avatarUrl: String = "",
    text: String = "",
    onTextChange: (String) -> Unit = {},
    onPost: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VNRed.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                AuthorAvatar(
                    initials = avatarInitials,
                    color = Color(avatarColor),
                    avatarUrl = avatarUrl,
                    size = 40,
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    // ── Text input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        if (text.isEmpty()) {
                            Text("Có gì mới?", color = Color(0xFF6B7280), fontSize = 14.sp)
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 14.sp, color = SlateGray900),
                            cursorBrush = SolidColor(VNRed),
                            maxLines = 5
                        )
                    }

                    HorizontalDivider(color = SlateGray50)

                    // ── Bottom actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Outlined.Image, "Ảnh", tint = SlateGray400, modifier = Modifier.size(18.dp))
                            Icon(Icons.Outlined.Place, "Địa điểm", tint = SlateGray400, modifier = Modifier.size(18.dp))
                            Icon(Icons.Outlined.Map, "Lịch trình", tint = SlateGray400, modifier = Modifier.size(18.dp))
                        }
                        Button(
                            onClick = onPost,
                            shape = CircleShape,
                            enabled = text.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VNRed,
                                disabledContainerColor = SlateGray200
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                "ĐĂNG", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
