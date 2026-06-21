package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.vietnam_travel_itinerary_android.data.model.UserProfile
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

private data class DrawerEntry(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun AppNavigationDrawer(
    profile: UserProfile?,
    currentRoute: String,
    onDestination: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val isAdmin = profile?.role == "admin"

    val entries = if (isAdmin) {
        listOf(
            DrawerEntry("home", "Trang chủ", Icons.Filled.Home),
            DrawerEntry("my_place_suggestions", "Đề xuất địa điểm", Icons.Filled.AddLocationAlt),
            DrawerEntry("admin_place_suggestions", "Quản lý đề xuất", Icons.AutoMirrored.Filled.ListAlt),
            DrawerEntry("admin_reports", "Quản lý báo cáo", Icons.Filled.Flag),
        )
    } else {
        listOf(
            DrawerEntry("home", "Trang chủ", Icons.Filled.Home),
            DrawerEntry("my_place_suggestions", "Đề xuất địa điểm", Icons.Filled.AddLocationAlt),
        )
    }

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Spacer(Modifier.height(8.dp))
            DrawerHeader(profile = profile, isAdmin = isAdmin)
            HorizontalDivider(color = VNRed.copy(alpha = 0.08f))
            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                entries.forEach { entry ->
                    NavigationDrawerItem(
                        label = { Text(entry.label, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(entry.icon, contentDescription = entry.label) },
                        selected = currentRoute == entry.route,
                        onClick = { onDestination(entry.route) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = VNRed.copy(alpha = 0.1f),
                            selectedIconColor = VNRed,
                            selectedTextColor = VNRed,
                            unselectedIconColor = SlateGray500,
                            unselectedTextColor = SlateGray900,
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }

            HorizontalDivider(color = VNRed.copy(alpha = 0.08f))
            NavigationDrawerItem(
                label = { Text("Đăng xuất", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Đăng xuất") },
                selected = false,
                onClick = onLogout,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = VNRed,
                    unselectedTextColor = VNRed,
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun DrawerHeader(profile: UserProfile?, isAdmin: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(profile?.avatarColor ?: 0xFF64748B)),
            contentAlignment = Alignment.Center,
        ) {
            if (!profile?.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profile?.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(
                    text = profile?.avatarInitials ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile?.name ?: "Khách",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateGray900,
                maxLines = 1,
            )
            if (isAdmin) {
                Spacer(Modifier.height(2.dp))
                Surface(
                    color = VNRed.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "Quản trị viên",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VNRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            } else if (!profile?.username.isNullOrBlank()) {
                Text(
                    text = profile?.username ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray500,
                    maxLines = 1,
                )
            }
        }
    }
}
