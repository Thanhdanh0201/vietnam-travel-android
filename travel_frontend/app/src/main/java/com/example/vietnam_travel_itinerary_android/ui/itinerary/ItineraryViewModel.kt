package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary

class ItineraryViewModel : ViewModel() {

    var itineraries = mutableStateListOf<Itinerary>()
        private set

    fun createItinerary(itinerary: Itinerary) {
        itineraries.add(itinerary)
    }
}