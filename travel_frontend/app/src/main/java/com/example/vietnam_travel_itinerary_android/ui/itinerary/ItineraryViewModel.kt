package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ParticipantRole {
    EDIT, VIEW_ONLY
}

data class Participant(
    val name: String,
    val email: String,
    val initials: String,
    val avatarColor: Long, // Màu nền của avatar (dạng Hex Long)
    val role: ParticipantRole
)

class ItineraryViewModel(
    private val placeRepo: PlaceRepository = PlaceRepository()
) : ViewModel() {

    data class ItineraryUiState(
        val itineraries: List<Itinerary> = emptyList(),
        val timelineMap: Map<String, List<TimelineItemData>> = emptyMap(), // Key: "itineraryId-day"
        val participantsMap: Map<String, List<Participant>> = emptyMap(), // Key: "itineraryId"
        val allPlaces: List<Place> = emptyList(),
        val isLoadingPlaces: Boolean = false,
        val placesError: String? = null
    )

    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        fetchAllPlaces()
    }

    private fun loadInitialData() {
        val initialItineraries = listOf(
            Itinerary(
                id = "1",
                title = "Kỳ nghỉ Vịnh Hạ Long",
                location = "Hạ Long, Quảng Ninh",
                dateRange = "15/10 - 18/10/2024",
                statusText = "SẮP DIỄN RA",
                statusSubText = "🕒 Còn 5 ngày nữa",
                isUpcoming = true,
                imageResId = android.R.drawable.ic_menu_gallery,
                participantImages = listOf(android.R.drawable.ic_menu_report_image)
            ),
            Itinerary(
                id = "2",
                title = "Khám phá Hội An",
                location = "Hội An, Quảng Nam",
                dateRange = "01/09 - 04/09/2024",
                statusText = "ĐÃ KẾT THÚC",
                statusSubText = null,
                isUpcoming = false,
                imageResId = android.R.drawable.ic_menu_gallery,
                participantImages = listOf(android.R.drawable.ic_menu_report_image)
            ),
            Itinerary(
                id = "3",
                title = "Hành trình Sapa",
                location = "Sa Pa, Lào Cai",
                dateRange = "10/11 - 14/11/2024",
                statusText = "SẮP DIỄN RA",
                statusSubText = "🕒 Còn 24 ngày",
                isUpcoming = true,
                imageResId = android.R.drawable.ic_menu_gallery,
                participantImages = listOf(android.R.drawable.ic_menu_report_image)
            )
        )

        val initialTimelineMap = mapOf(
            // Hạ Long - Ngày 12
            "1-12" to listOf(
                TimelineItemData("08:00 AM", "Vịnh Hạ Long", "Hạ Long, Quảng Ninh", "Du thuyền", "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b"),
                TimelineItemData("12:00 PM", "Đảo Ti Tốp", "Hạ Long, Quảng Ninh", "Tắm biển", "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b")
            ),
            // Hạ Long - Ngày 13
            "1-13" to listOf(
                TimelineItemData("09:00 AM", "Hang Sửng Sốt", "Hạ Long, Quảng Ninh", "Thám hiểm", "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b"),
                TimelineItemData("03:00 PM", "Bãi Cháy", "Hạ Long, Quảng Ninh", "Vui chơi", "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b")
            ),
            
            // Hội An - Ngày 12
            "2-12" to listOf(
                TimelineItemData("08:00 AM", "Phố cổ Hội An", "Hội An, Quảng Nam", "Tham quan", "https://images.unsplash.com/photo-1555921015-5532091f6026"),
                TimelineItemData("07:00 PM", "Chợ đêm Hội An", "Hội An, Quảng Nam", "Ẩm thực", "https://images.unsplash.com/photo-1555921015-5532091f6026")
            ),
            // Hội An - Ngày 13
            "2-13" to listOf(
                TimelineItemData("09:00 AM", "Thánh địa Mỹ Sơn", "Duy Xuyên, Quảng Nam", "Di sản", "https://images.unsplash.com/photo-1555921015-5532091f6026")
            ),
            
            // Sapa - Ngày 12
            "3-12" to listOf(
                TimelineItemData("09:00 AM", "Bản Cát Cát", "Sa Pa, Lào Cai", "Dã ngoại", "https://images.unsplash.com/photo-1583417657208-cb86acb8b209"),
                TimelineItemData("02:00 PM", "Đỉnh Fansipan", "Sa Pa, Lào Cai", "Cáp treo", "https://images.unsplash.com/photo-1583417657208-cb86acb8b209")
            ),
            // Sapa - Ngày 13
            "3-13" to listOf(
                TimelineItemData("10:00 AM", "Thung lũng Mường Hoa", "Sa Pa, Lào Cai", "Ngắm cảnh", "https://images.unsplash.com/photo-1583417657208-cb86acb8b209")
            )
        )

        // Seed danh sách người tham gia ban đầu
        val initialParticipantsMap = mapOf(
            "1" to listOf(
                Participant("Lâm", "lam@gmail.com", "L", 0xFF10B981, ParticipantRole.EDIT),
                Participant("Mai", "mai@gmail.com", "M", 0xFF3B82F6, ParticipantRole.VIEW_ONLY),
                Participant("Hải", "hai@gmail.com", "H", 0xFFF59E0B, ParticipantRole.VIEW_ONLY)
            ),
            "2" to listOf(
                Participant("Cường", "cuong@gmail.com", "C", 0xFF10B981, ParticipantRole.EDIT),
                Participant("Vy", "vy@gmail.com", "V", 0xFF3B82F6, ParticipantRole.EDIT)
            ),
            "3" to listOf(
                Participant("Nam", "nam@gmail.com", "N", 0xFF10B981, ParticipantRole.EDIT),
                Participant("Hoa", "hoa@gmail.com", "H", 0xFFF59E0B, ParticipantRole.VIEW_ONLY)
            )
        )

        _uiState.update {
            it.copy(
                itineraries = initialItineraries,
                timelineMap = initialTimelineMap,
                participantsMap = initialParticipantsMap
            )
        }
    }

    private fun fetchAllPlaces() {
        _uiState.update { it.copy(isLoadingPlaces = true) }
        viewModelScope.launch {
            try {
                placeRepo.getPlaces(limit = 100).onSuccess { places ->
                    _uiState.update {
                        it.copy(
                            allPlaces = places,
                            isLoadingPlaces = false,
                            placesError = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingPlaces = false,
                            placesError = error.message ?: "Lỗi tải địa điểm",
                            allPlaces = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingPlaces = false,
                        placesError = e.message ?: "Lỗi tải địa điểm"
                    )
                }
            }
        }
    }

    fun addItinerary(itinerary: Itinerary) {
        _uiState.update {
            val newList = it.itineraries + itinerary
            it.copy(itineraries = newList)
        }
    }

    fun addPlaceToItinerary(itineraryId: String, day: String, time: String, place: Place, tag: String) {
        _uiState.update { state ->
            val key = "$itineraryId-$day"
            val currentTimeline = state.timelineMap[key] ?: emptyList()
            val newLocation = "${place.cities?.name ?: ""}, ${place.provinces?.name ?: ""}"
            val newItem = TimelineItemData(
                time = time.ifBlank { "08:00 AM" },
                title = place.name,
                location = newLocation.trim().removePrefix(",").removeSuffix(",").trim(),
                tag = tag.ifBlank { place.type ?: "Địa điểm" },
                imageUrl = place.imageUrl ?: "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b"
            )
            
            val updatedTimeline = (currentTimeline + newItem).sortedBy { it.time }
            val newMap = state.timelineMap.toMutableMap()
            newMap[key] = updatedTimeline
            
            state.copy(timelineMap = newMap)
        }
    }

    fun removePlaceFromItinerary(itineraryId: String, day: String, item: TimelineItemData) {
        _uiState.update { state ->
            val key = "$itineraryId-$day"
            val currentTimeline = state.timelineMap[key] ?: emptyList()
            val updatedTimeline = currentTimeline - item
            val newMap = state.timelineMap.toMutableMap()
            newMap[key] = updatedTimeline
            state.copy(timelineMap = newMap)
        }
    }

    // ---- Các chức năng quản lý Người Tham Gia & Phân Quyền ----
    fun addParticipant(itineraryId: String, name: String, email: String, role: ParticipantRole) {
        _uiState.update { state ->
            val currentList = state.participantsMap[itineraryId] ?: emptyList()
            if (currentList.any { it.email.equals(email, ignoreCase = true) }) return
            
            val colors = listOf(0xFF10B981, 0xFF3B82F6, 0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899)
            val randomColor = colors.random()
            val initials = name.trim().take(1).uppercase()
            
            val newParticipant = Participant(
                name = name.ifBlank { "Thành viên" },
                email = email.ifBlank { "member@gmail.com" },
                initials = initials.ifBlank { "M" },
                avatarColor = randomColor,
                role = role
            )
            
            val newMap = state.participantsMap.toMutableMap()
            newMap[itineraryId] = currentList + newParticipant
            state.copy(participantsMap = newMap)
        }
    }

    fun updateParticipantRole(itineraryId: String, email: String, newRole: ParticipantRole) {
        _uiState.update { state ->
            val currentList = state.participantsMap[itineraryId] ?: emptyList()
            val updatedList = currentList.map {
                if (it.email.equals(email, ignoreCase = true)) it.copy(role = newRole) else it
            }
            val newMap = state.participantsMap.toMutableMap()
            newMap[itineraryId] = updatedList
            state.copy(participantsMap = newMap)
        }
    }

    fun removeParticipant(itineraryId: String, email: String) {
        _uiState.update { state ->
            val currentList = state.participantsMap[itineraryId] ?: emptyList()
            val updatedList = currentList.filterNot { it.email.equals(email, ignoreCase = true) }
            val newMap = state.participantsMap.toMutableMap()
            newMap[itineraryId] = updatedList
            state.copy(participantsMap = newMap)
        }
    }

    fun getFallbackPlacesFor(province: String, district: String): List<Place> {
        val fallbackList = mutableListOf<Place>()
        val p = province.trim()
        val d = district.trim()

        if (p.contains("Huế", ignoreCase = true) || p.contains("Thừa Thiên", ignoreCase = true)) {
            if (d.contains("Huế", ignoreCase = true)) {
                fallbackList.add(Place("fb_hue_1", "Đại Nội Huế", "Di sản", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.8, 100, "Đại Nội Huế cổ kính", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("TP. Huế")))
                fallbackList.add(Place("fb_hue_2", "Chùa Thiên Mụ", "Văn hóa", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.7, 100, "Chùa Thiên Mụ bên sông Hương", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("TP. Huế")))
            } else if (d.contains("Phú Lộc", ignoreCase = true)) {
                fallbackList.add(Place("fb_hue_pl1", "Đầm Lập An", "Thiên nhiên", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.6, 100, "Đầm Lập An thơ mộng", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Phú Lộc")))
                fallbackList.add(Place("fb_hue_pl2", "Vườn quốc gia Bạch Mã", "Thiên nhiên", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.8, 100, "Bạch Mã hùng vĩ", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Phú Lộc")))
            } else {
                fallbackList.add(Place("fb_hue_gen", "Lăng Khải Định", "Di sản", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.8, 100, "Lăng tẩm cổ kính", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            }
        } else if (p.contains("Đà Nẵng", ignoreCase = true)) {
            if (d.contains("Hòa Vang", ignoreCase = true)) {
                fallbackList.add(Place("fb_dn_1", "Bà Nà Hills", "Cáp treo", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.8, 200, "Bà Nà Hills - Đường lên tiên cảnh", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hòa Vang")))
                fallbackList.add(Place("fb_dn_2", "Cầu Vàng", "Cảnh quan", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.9, 300, "Cầu Vàng Bà Nà Hills", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hòa Vang")))
            } else if (d.contains("Sơn Trà", ignoreCase = true)) {
                fallbackList.add(Place("fb_dn_st1", "Bán đảo Sơn Trà", "Thiên nhiên", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.7, 100, "Bán đảo Sơn Trà xanh mát", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Sơn Trà")))
                fallbackList.add(Place("fb_dn_st2", "Chùa Linh Ứng", "Tâm linh", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.8, 120, "Tượng Phật Bà cao nhất Việt Nam", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Sơn Trà")))
            } else {
                fallbackList.add(Place("fb_dn_gen", "Cầu Rồng Đà Nẵng", "Cầu", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.8, 150, "Cầu Rồng phun lửa", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            }
        } else if (p.contains("Quảng Nam", ignoreCase = true)) {
            if (d.contains("Hội An", ignoreCase = true)) {
                fallbackList.add(Place("fb_qn_1", "Chùa Cầu", "Di sản", 0.0, 0.0, "https://images.unsplash.com/photo-1555921015-5532091f6026", 4.7, 150, "Biểu tượng phố cổ Hội An", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Nam", "49"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hội An")))
                fallbackList.add(Place("fb_qn_2", "Chợ đêm Hội An", "Ẩm thực", 0.0, 0.0, "https://images.unsplash.com/photo-1555921015-5532091f6026", 4.6, 120, "Chợ đêm đầy sắc màu", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Nam", "49"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hội An")))
            } else {
                fallbackList.add(Place("fb_qnam_gen", "Thánh địa Mỹ Sơn", "Di sản", 0.0, 0.0, "https://images.unsplash.com/photo-1555921015-5532091f6026", 4.8, 100, "Thành cổ Chăm Pa cổ kính", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Nam", "49"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            }
        } else if (p.contains("Lào Cai", ignoreCase = true) || p.contains("Sa Pa", ignoreCase = true) || p.contains("Sapa", ignoreCase = true)) {
            fallbackList.add(Place("fb_lc_1", "Bản Cát Cát", "Dân tộc", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.5, 100, "Bản Cát Cát của người H'Mông", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Lào Cai", "10"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Sa Pa")))
            fallbackList.add(Place("fb_lc_2", "Đỉnh Fansipan", "Leo núi", 0.0, 0.0, "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", 4.9, 300, "Nóc nhà Đông Dương", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Lào Cai", "10"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Sa Pa")))
        } else if (p.contains("Quảng Ninh", ignoreCase = true) || p.contains("Hạ Long", ignoreCase = true) || p.contains("Halong", ignoreCase = true)) {
            fallbackList.add(Place("fb_qn_hl1", "Vịnh Hạ Long", "Vịnh", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.9, 500, "Kỳ quan thiên nhiên thế giới", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Ninh", "22"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Hạ Long")))
            fallbackList.add(Place("fb_qn_hl2", "Đảo Ti Tốp", "Bãi biển", 0.0, 0.0, "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", 4.7, 120, "Hòn đảo Ti Tốp thơ mộng", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Ninh", "22"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Hạ Long")))
        } else {
            fallbackList.add(Place("fb_gen_1", "Khu du lịch sinh thái $d", "Giải trí", 0.0, 0.0, null, 4.5, 50, "Điểm du lịch địa phương", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary(p, "00"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            fallbackList.add(Place("fb_gen_2", "Hồ sinh thái $d", "Thiên nhiên", 0.0, 0.0, null, 4.6, 40, "Điểm ngắm cảnh địa phương", com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary(p, "00"), com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
        }
        return fallbackList
    }
    fun createItinerary(itinerary: Itinerary) {
        _uiState.update { state ->
            state.copy(
                itineraries = state.itineraries + itinerary
            )
        }
    }
    fun deleteItinerary(itinerary: Itinerary) {
        _uiState.update { state ->
            state.copy(
                itineraries = state.itineraries.filterNot { it.id == itinerary.id }
            )
        }
    }
}
