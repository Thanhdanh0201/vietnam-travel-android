package com.example.vietnam_travel_itinerary_android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray500

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Trang chủ", Icons.Filled.Home, Icons.Outlined.Home, "home"),
    BottomNavItem("Cộng đồng", Icons.Filled.Groups, Icons.Outlined.Groups, "community"),
    BottomNavItem("", Icons.Filled.Star, Icons.Filled.Star, "explore"),  // Center FAB - Logo
    BottomNavItem("Lịch trình", Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote, "itinerary"),
    BottomNavItem("Cá nhân", Icons.Filled.Person, Icons.Outlined.Person, "profile"),
)

@Composable
fun BottomNavBar(
    currentRoute: String = "home",
    onItemClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Main navigation bar with glassmorphism
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter),
            color = Color.White.copy(alpha = 0.85f),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top gradient accent line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    VNRed.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Nav items row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        if (index == 2) {
                            // Center spacer for FAB
                            Spacer(modifier = Modifier.width(64.dp))
                        } else {
                            // Regular nav item
                            val isSelected = currentRoute == item.route
                            val animatedColor by animateColorAsState(
                                targetValue = if (isSelected) VNRed else SlateGray500,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "navColor"
                            )
                            val animatedScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "navScale"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(animatedScale)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onItemClick(item.route) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp),
                                    tint = animatedColor
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = animatedColor,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Elevated Center FAB (Logo button)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = { onItemClick("explore") },
                containerColor = VNRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .shadow(12.dp, CircleShape)
                    .border(3.dp, Color.White, CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Khám phá",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
