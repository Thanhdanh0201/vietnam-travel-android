package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

@Composable
fun ProfileAvatar(
    avatarUrl: String,
    initials: String,
    color: Color,
    size: Int,
    textSize: TextUnit = (size * 0.35f).sp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val url = avatarUrl.trim().takeIf { it.isNotBlank() }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(url).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = textSize,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
fun EditableProfileAvatar(
    avatarUrl: String,
    initials: String,
    color: Color,
    size: Int = 96,
    isUploading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = VNRed.copy(alpha = 0.2f),
                    spotColor = VNRed.copy(alpha = 0.2f),
                )
                .clip(CircleShape)
                .border(3.dp, VNRed, CircleShape)
                .clickable(enabled = !isUploading, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            ProfileAvatar(
                avatarUrl = avatarUrl,
                initials = initials,
                color = color,
                size = size - 10,
                textSize = 28.sp,
            )

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(VNRed)
                .border(2.dp, Color.White, CircleShape)
                .clickable(enabled = !isUploading, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Đổi ảnh đại diện",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
