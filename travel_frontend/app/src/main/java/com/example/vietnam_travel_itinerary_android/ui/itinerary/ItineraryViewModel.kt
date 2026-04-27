package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.R
/*
* ViewModel for managing Itinerary data (CRUD - READ + CREATE for now)
*
* - Acts like a controller + in-memory data store (similar to a Java List)
* - "itineraries" holds the current list of data for the UI
* - Uses mutableStateListOf so UI auto-updates when data changes
*
* READ:
* - loadItineraries() initializes sample data (temporary before API integration)
* - Called in init block (runs when ViewModel is created)
*
* CREATE:
* - createItinerary() adds a new item to the list
* - UI updates automatically after adding
*
* NOTE:
* - This is currently using mock data
* - Will be replaced with API calls (Retrofit) later
*/

/*
 * ViewModel stores in-memory UI state (itineraries list).
 * Acts as the single source of truth while the app is running.
 *
 * - Data is temporary (not persisted yet)
 * - Will later be loaded from backend API
 */

/*
 * ViewModel quản lý dữ liệu lịch trình cho UI.
 *
 * - Lưu danh sách itineraries (dữ liệu tạm trong lúc app chạy)
 * - UI sẽ đọc dữ liệu từ đây
 * - CREATE: thêm lịch trình vào danh sách
 * - READ: load dữ liệu ban đầu (hiện đang dùng mock data)
 *
 * Sau này sẽ kết nối API để lấy dữ liệu từ backend
 */
class ItineraryViewModel : ViewModel() {

    var itineraries = mutableStateListOf<Itinerary>()
        private set

    init {
        loadItineraries()
    }

    fun loadItineraries() {
        // TEMPORARY MOCK DATA (before backend)
        itineraries.addAll(
            listOf(
                Itinerary(
                    id = "1",
                    title = "Đà Nẵng Trip",
                    location = "Đà Nẵng",
                    dateRange = "1-3 May",
                    isUpcoming = true,
                    imageResId = R.drawable.ic_launcher_background,
                    statusText = "Sắp diễn ra",
                    statusSubText = null,
                    participantImages = emptyList()
                ),
                Itinerary(
                    id = "2",
                    title = "Hà Nội Trip",
                    location = "Hà Nội",
                    dateRange = "10-12 April",
                    isUpcoming = false,
                    imageResId = R.drawable.ic_launcher_background,
                    statusText = "Đã kết thúc",
                    statusSubText = null,
                    participantImages = emptyList()
                )
            )
        )
    }

    fun createItinerary(itinerary: Itinerary) {
        itineraries.add(itinerary)
    }

    fun updateItinerary(updated: Itinerary) {
        val index = itineraries.indexOfFirst { it.id == updated.id }

        if (index != -1) {
            itineraries[index] = updated
        }
    }
    fun deleteItinerary(itinerary: Itinerary) {
        itineraries.remove(itinerary)
    }
}