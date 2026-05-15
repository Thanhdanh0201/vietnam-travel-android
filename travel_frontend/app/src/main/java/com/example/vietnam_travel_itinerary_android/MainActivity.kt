package com.example.vietnam_travel_itinerary_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vietnam_travel_itinerary_android.ui.navigation.MainScreen
import com.example.vietnam_travel_itinerary_android.ui.theme.VietnamTravelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VietnamTravelTheme {
                MainScreen()
            }
        }
    }
}