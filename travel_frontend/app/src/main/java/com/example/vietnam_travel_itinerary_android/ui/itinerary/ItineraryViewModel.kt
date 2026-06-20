package com.example.vietnam_travel_itinerary_android.ui.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.vietnam_travel_itinerary_android.data.dto.ItineraryNoteDto
import com.example.vietnam_travel_itinerary_android.data.model.Itinerary
import com.example.vietnam_travel_itinerary_android.data.model.Place
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.vietnam_travel_itinerary_android.data.repository.ItineraryRepository
import com.example.vietnam_travel_itinerary_android.SupabaseObject
import com.example.vietnam_travel_itinerary_android.data.dto.CollaboratorDto

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
    private val placeRepo: PlaceRepository = PlaceRepository(),
    private val itineraryRepo: ItineraryRepository = ItineraryRepository(SupabaseObject.client)
) : ViewModel() {

    data class ItineraryUiState(
        val itineraries: List<Itinerary> = emptyList(),
        val timelineMap: Map<String, List<TimelineItemData>> = emptyMap(), // Key: "itineraryId-day"
        val participantsMap: Map<String, List<Participant>> = emptyMap(), // Key: "itineraryId"
        val allPlaces: List<Place> = emptyList(),
        val isLoadingPlaces: Boolean = false,
        val placesError: String? = null,
        val provinces: List<com.example.vietnam_travel_itinerary_android.data.model.Province> = emptyList(),
        val cities: List<String> = emptyList(),
        val isLoadingProvinces: Boolean = false,
        val isRefreshing: Boolean = false,
        /** Key: "itineraryId-itemId" — ghi chú nhóm gắn với từng địa điểm */
        val notesMap: Map<String, List<ItineraryNoteDto>> = emptyMap(),
        /** Key: "itineraryId" — ghi chú chung hiển thị ở cuối timeline */
        val generalNotesMap: Map<String, List<ItineraryNoteDto>> = emptyMap(),
    )

    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    init {
        fetchItineraries() // overwrite with API if success
        fetchAllPlaces()
        fetchProvinces()
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

    private fun parseDateRange(dateRange: String): Pair<String?, String?> {
        try {
            val parts = dateRange.split("-")
            if (parts.size == 2) {
                val startPart = parts[0].trim() // "16/12"
                val endPart = parts[1].trim() // "20/12/2024"
                val endSubParts = endPart.split("/") // ["20", "12", "2024"]
                if (endSubParts.size == 3) {
                    val year = endSubParts[2]
                    val endMonth = endSubParts[1].padStart(2, '0')
                    val endDay = endSubParts[0].padStart(2, '0')

                    val startSubParts = startPart.split("/")
                    val startDay = startSubParts[0].padStart(2, '0')
                    val startMonth = startSubParts[1].padStart(2, '0')

                    return Pair("$year-$startMonth-$startDay", "$year-$endMonth-$endDay")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(null, null)
    }

    fun fetchItineraries() {
        viewModelScope.launch {
            itineraryRepo.getItineraries()
                .onSuccess { itineraries ->
                    _uiState.update {
                        it.copy(itineraries = itineraries)
                    }
                    itineraries.forEach {
                        fetchItineraryItems(it.id)
                    }
                }
                .onFailure {
                    // fallback to mock data already loaded
                    println(it.message)
                }
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                itineraryRepo.getItineraries()
                    .onSuccess { itineraries ->
                        _uiState.update { it.copy(itineraries = itineraries) }
                        itineraries.forEach { fetchCollaborators(it.id) }
                    }
                    .onFailure {
                        it.printStackTrace()
                    }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun fetchItineraryDetail(itineraryId: String) {
        viewModelScope.launch {
            itineraryRepo.getItineraryById(itineraryId)
                .onSuccess { itinerary ->
                    _uiState.update { state ->
                        val currentList = state.itineraries
                        val updatedList = if (currentList.any { it.id == itinerary.id }) {
                            currentList.map { if (it.id == itinerary.id) itinerary else it }
                        } else {
                            currentList + itinerary
                        }
                        state.copy(itineraries = updatedList)
                    }
                    fetchItineraryItems(itineraryId)
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun fetchItineraryItems(itineraryId: String) {
        viewModelScope.launch {
            fetchCollaborators(itineraryId)
            itineraryRepo.getItineraryItems(itineraryId)
                .onSuccess { items ->
                    _uiState.update { state ->
                        val newMap = state.timelineMap.toMutableMap()
                        // Clear old keys for this itinerary first
                        val keysToRemove = newMap.keys.filter { it.startsWith("$itineraryId-") }
                        keysToRemove.forEach { newMap.remove(it) }

                        val itemsByDay = items.groupBy { it.day ?: "12" }
                        itemsByDay.forEach { (day, list) ->
                            newMap["$itineraryId-$day"] = list.sortedBy { it.time }
                        }
                        state.copy(timelineMap = newMap)
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    /**
     * Trả về true nếu user hiện tại có quyền chỉnh sửa itinerary (OWNER hoặc EDIT).
     * VIEW-only collaborators không thể thêm/xóa item.
     */
    fun canModifyItinerary(itineraryId: String): Boolean {
        val itinerary = _uiState.value.itineraries.find { it.id == itineraryId } ?: return false
        return itinerary.myRole == "OWNER" || itinerary.myRole == "EDIT"
    }

    private fun getProvinceCode(provinceName: String): String? {
        val p = provinceName.trim()
        return when {
            p.contains("Hà Nội", ignoreCase = true) -> "01"
            p.contains("Lào Cai", ignoreCase = true) -> "10"
            p.contains("Quảng Ninh", ignoreCase = true) -> "22"
            p.contains("Huế", ignoreCase = true) || p.contains("Thừa Thiên", ignoreCase = true) -> "46"
            p.contains("Đà Nẵng", ignoreCase = true) -> "48"
            p.contains("Quảng Nam", ignoreCase = true) -> "49"
            p.contains("Hồ Chí Minh", ignoreCase = true) || p.contains("Sài Gòn", ignoreCase = true) -> "79"
            else -> null
        }
    }

    fun fetchPlacesForProvince(provinceName: String) {
        val provinceCode = _uiState.value.provinces.find { it.name.contains(provinceName.trim(), ignoreCase = true) }?.code
            ?: getProvinceCode(provinceName)
        _uiState.update { it.copy(isLoadingPlaces = true) }
        viewModelScope.launch {
            placeRepo.getPlaces(provinceCode = provinceCode, limit = 100).onSuccess { places ->
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
        }
    }

    fun fetchProvinces() {
        viewModelScope.launch {
            itineraryRepo.getProvinces()
                .onSuccess { list ->
                    _uiState.update { state ->
                        state.copy(provinces = list)
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun fetchCitiesForProvince(provinceCode: String) {
        viewModelScope.launch {
            itineraryRepo.getCitiesByProvince(provinceCode)
                .onSuccess { list ->
                    _uiState.update { state ->
                        state.copy(cities = list.map { it.name })
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
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

    fun uploadCover(byteArray: ByteArray, fileName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            itineraryRepo.uploadCover(byteArray, fileName)
                .onSuccess { url ->
                    onSuccess(url)
                }
                .onFailure {
                    onFailure(it.message ?: "Lỗi tải ảnh bìa")
                }
        }
    }

    fun addItinerary(
        itinerary: Itinerary, 
        coverUrl: String? = null,
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val (startDate, endDate) = parseDateRange(itinerary.dateRange)
            itineraryRepo.createItinerary(
                title = itinerary.title,
                location = itinerary.location,
                startDate = startDate,
                endDate = endDate,
                description = "",
                coverUrl = coverUrl,
                isPublic = false
            ).onSuccess { saved ->
                _uiState.update { state ->
                    state.copy(itineraries = state.itineraries + saved)
                }
                onSuccess(saved.id)
            }.onFailure {
                it.printStackTrace()
                onFailure(it.message ?: "Lỗi tạo lịch trình")
            }
        }
    }

    fun deleteItinerary(itineraryId: String) {
        viewModelScope.launch {
            itineraryRepo.deleteItinerary(itineraryId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            itineraries = state.itineraries.filterNot { it.id == itineraryId }
                        )
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun addPlaceToItinerary(itineraryId: String, day: String, time: String, place: Place, note: String?) {
        viewModelScope.launch {
            val scheduledTime = try {
                val cleanTime = time.ifBlank { "08:00 AM" }.trim()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
                val localTime = java.time.LocalTime.parse(cleanTime, formatter)
                localTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)
            } catch (e: Exception) {
                "08:00:00"
            }

            val key = "$itineraryId-$day"
            val currentTimeline = _uiState.value.timelineMap[key] ?: emptyList()
            val orderIndex = currentTimeline.size

            itineraryRepo.addItineraryItem(
                itineraryId = itineraryId,
                placeId = place.id,
                scheduledTime = scheduledTime,
                day = day,
                note = note,
                orderIndex = orderIndex
            ).onSuccess { newItem ->
                _uiState.update { state ->
                    val list = (state.timelineMap[key] ?: emptyList()) + newItem
                    val newMap = state.timelineMap.toMutableMap()
                    newMap[key] = list.sortedBy { it.time }
                    state.copy(timelineMap = newMap)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun removePlaceFromItinerary(itineraryId: String, day: String, item: TimelineItemData) {
        viewModelScope.launch {
            if (item.id.isBlank()) {
                _uiState.update { state ->
                    val key = "$itineraryId-$day"
                    val list = (state.timelineMap[key] ?: emptyList()) - item
                    val newMap = state.timelineMap.toMutableMap()
                    newMap[key] = list
                    state.copy(timelineMap = newMap)
                }
                return@launch
            }

            itineraryRepo.deleteItineraryItem(itineraryId, item.id)
                .onSuccess {
                    _uiState.update { state ->
                        val key = "$itineraryId-$day"
                        val list = (state.timelineMap[key] ?: emptyList()).filterNot { it.id == item.id }
                        val newMap = state.timelineMap.toMutableMap()
                        newMap[key] = list
                        state.copy(timelineMap = newMap)
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun fetchCollaborators(itineraryId: String) {
        viewModelScope.launch {
            itineraryRepo.getCollaborators(itineraryId)
                .onSuccess { list ->
                    _uiState.update { state ->
                        val currentList = list.map { c ->
                            val colors = listOf(0xFF10B981, 0xFF3B82F6, 0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899)
                            val colorIndex = (c.email.hashCode().and(0x7FFFFFFF)) % colors.size
                            val initials = c.name.trim().take(1).uppercase()
                            Participant(
                                name = c.name,
                                email = c.email,
                                initials = if (initials.isBlank()) "M" else initials,
                                avatarColor = colors[colorIndex],
                                role = when (c.role) {
                                    "EDIT" -> ParticipantRole.EDIT
                                    else -> ParticipantRole.VIEW_ONLY
                                }
                            )
                        }
                        val newMap = state.participantsMap.toMutableMap()
                        newMap[itineraryId] = currentList
                        state.copy(participantsMap = newMap)
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    // ---- Các chức năng quản lý Người Tham Gia & Phân Quyền ----
    fun addParticipant(itineraryId: String, name: String, email: String, role: ParticipantRole) {
        viewModelScope.launch {
            val roleStr = if (role == ParticipantRole.EDIT) "EDIT" else "VIEW"
            itineraryRepo.addCollaborator(itineraryId, email, name, roleStr)
                .onSuccess {
                    fetchCollaborators(itineraryId)
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun updateParticipantRole(itineraryId: String, email: String, newRole: ParticipantRole) {
        viewModelScope.launch {
            val roleStr = if (newRole == ParticipantRole.EDIT) "EDIT" else "VIEW"
            val currentName = _uiState.value.participantsMap[itineraryId]?.find { it.email.equals(email, ignoreCase = true) }?.name ?: "Thành viên"
            itineraryRepo.addCollaborator(itineraryId, email, currentName, roleStr)
                .onSuccess {
                    fetchCollaborators(itineraryId)
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun removeParticipant(itineraryId: String, email: String) {
        viewModelScope.launch {
            itineraryRepo.removeCollaborator(itineraryId, email)
                .onSuccess {
                    fetchCollaborators(itineraryId)
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun getFallbackPlacesFor(province: String, district: String): List<Place> {
        val fallbackList = mutableListOf<Place>()
        val p = province.trim()
        val d = district.trim()

        if (p.contains("Huế", ignoreCase = true) || p.contains("Thừa Thiên", ignoreCase = true)) {
            if (d.contains("Huế", ignoreCase = true)) {
                fallbackList.add(Place(id = "fb_hue_1", name = "Đại Nội Huế", type = "Di sản", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.8, reviewCount = 100, description = "Đại Nội Huế cổ kính", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("TP. Huế")))
                fallbackList.add(Place(id = "fb_hue_2", name = "Chùa Thiên Mụ", type = "Văn hóa", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.7, reviewCount = 100, description = "Chùa Thiên Mụ bên sông Hương", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("TP. Huế")))
            } else if (d.contains("Phú Lộc", ignoreCase = true)) {
                fallbackList.add(Place(id = "fb_hue_pl1", name = "Đầm Lập An", type = "Thiên nhiên", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.6, reviewCount = 100, description = "Đầm Lập An thơ mộng", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Phú Lộc")))
                fallbackList.add(Place(id = "fb_hue_pl2", name = "Vườn quốc gia Bạch Mã", type = "Thiên nhiên", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.8, reviewCount = 100, description = "Bạch Mã hùng vĩ", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Phú Lộc")))
            } else {
                fallbackList.add(Place(id = "fb_hue_gen", name = "Lăng Khải Định", type = "Di sản", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.8, reviewCount = 100, description = "Lăng tẩm cổ kính", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Thừa Thiên Huế", "46"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            }
        } else if (p.contains("Đà Nẵng", ignoreCase = true)) {
            if (d.contains("Hòa Vang", ignoreCase = true)) {
                fallbackList.add(Place(id = "fb_dn_1", name = "Bà Nà Hills", type = "Cáp treo", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.8, reviewCount = 200, description = "Bà Nà Hills - Đường lên tiên cảnh", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hòa Vang")))
                fallbackList.add(Place(id = "fb_dn_2", name = "Cầu Vàng", type = "Cảnh quan", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.9, reviewCount = 300, description = "Cầu Vàng Bà Nà Hills", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hòa Vang")))
            } else if (d.contains("Sơn Trà", ignoreCase = true)) {
                fallbackList.add(Place(id = "fb_dn_st1", name = "Bán đảo Sơn Trà", type = "Thiên nhiên", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.7, reviewCount = 100, description = "Bán đảo Sơn Trà xanh mát", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Sơn Trà")))
                fallbackList.add(Place(id = "fb_dn_st2", name = "Chùa Linh Ứng", type = "Tâm linh", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.8, reviewCount = 120, description = "Tượng Phật Bà cao nhất Việt Nam", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Sơn Trà")))
            } else {
                fallbackList.add(Place(id = "fb_dn_gen", name = "Cầu Rồng Đà Nẵng", type = "Cầu", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.8, reviewCount = 150, description = "Cầu Rồng phun lửa", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Đà Nẵng", "48"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            }
        } else if (p.contains("Quảng Nam", ignoreCase = true)) {
            if (d.contains("Hội An", ignoreCase = true)) {
                fallbackList.add(Place(id = "fb_qn_1", name = "Chùa Cầu", type = "Di sản", imageUrl = "https://images.unsplash.com/photo-1555921015-5532091f6026", rating = 4.7, reviewCount = 150, description = "Biểu tượng phố cổ Hội An", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Nam", "49"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hội An")))
                fallbackList.add(Place(id = "fb_qn_2", name = "Chợ đêm Hội An", type = "Ẩm thực", imageUrl = "https://images.unsplash.com/photo-1555921015-5532091f6026", rating = 4.6, reviewCount = 120, description = "Chợ đêm đầy sắc màu", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Nam", "49"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary("Hội An")))
            } else {
                fallbackList.add(Place(id = "fb_qnam_gen", name = "Thánh địa Mỹ Sơn", type = "Di sản", imageUrl = "https://images.unsplash.com/photo-1555921015-5532091f6026", rating = 4.8, reviewCount = 100, description = "Thành cổ Chăm Pa cổ kính", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Nam", "49"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            }
        } else if (p.contains("Lào Cai", ignoreCase = true) || p.contains("Sa Pa", ignoreCase = true) || p.contains("Sapa", ignoreCase = true)) {
            fallbackList.add(Place(id = "fb_lc_1", name = "Bản Cát Cát", type = "Dân tộc", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.5, reviewCount = 100, description = "Bản Cát Cát của người H'Mông", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Lào Cai", "10"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Sa Pa")))
            fallbackList.add(Place(id = "fb_lc_2", name = "Đỉnh Fansipan", type = "Leo núi", imageUrl = "https://images.unsplash.com/photo-1583417657208-cb86acb8b209", rating = 4.9, reviewCount = 300, description = "Nóc nhà Đông Dương", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Lào Cai", "10"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Sa Pa")))
        } else if (p.contains("Quảng Ninh", ignoreCase = true) || p.contains("Hạ Long", ignoreCase = true) || p.contains("Halong", ignoreCase = true)) {
            fallbackList.add(Place(id = "fb_qn_hl1", name = "Vịnh Hạ Long", type = "Vịnh", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.9, reviewCount = 500, description = "Kỳ quan thiên nhiên thế giới", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Ninh", "22"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Hạ Long")))
            fallbackList.add(Place(id = "fb_qn_hl2", name = "Đảo Ti Tốp", type = "Bãi biển", imageUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b", rating = 4.7, reviewCount = 120, description = "Hòn đảo Ti Tốp thơ mộng", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary("Quảng Ninh", "22"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(if (d.isNotBlank()) d else "Hạ Long")))
        } else {
            fallbackList.add(Place(id = "fb_gen_1", name = "Khu du lịch sinh thái $d", type = "Giải trí", imageUrl = null, rating = 4.5, reviewCount = 50, description = "Điểm du lịch địa phương", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary(p, "00"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
            fallbackList.add(Place(id = "fb_gen_2", name = "Hồ sinh thái $d", type = "Thiên nhiên", imageUrl = null, rating = 4.6, reviewCount = 40, description = "Điểm ngắm cảnh địa phương", provinces = com.example.vietnam_travel_itinerary_android.data.model.ProvinceSummary(p, "00"), cities = com.example.vietnam_travel_itinerary_android.data.model.CitySummary(d)))
        }
        return fallbackList
    }
    fun updateItinerary(
        itineraryId: String,
        title: String?,
        description: String?,
        isPublic: Boolean?,
        status: String?,
        coverUrl: String?,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val request = UpdateItineraryRequest(
                title = title,
                description = description,
                isPublic = isPublic,
                status = status,
                coverUrl = coverUrl
            )

            itineraryRepo.updateItinerary(itineraryId, request)
                .onSuccess {
                    fetchItineraries()
                    onSuccess()
                }
                .onFailure {
                    it.printStackTrace()
                    onFailure(it.message ?: "Lỗi cập nhật lịch trình")
                }
        }
    }

    // =================== ITINERARY NOTES ===================

    /** Load ghi chú nhóm của một địa điểm cụ thể. */
    fun fetchNotesForItem(itineraryId: String, itemId: String) {
        viewModelScope.launch {
            itineraryRepo.getNotesForItem(itineraryId, itemId)
                .onSuccess { notes ->
                    _uiState.update { state ->
                        val newMap = state.notesMap.toMutableMap()
                        newMap["$itineraryId-$itemId"] = notes
                        state.copy(notesMap = newMap)
                    }
                }
                .onFailure { it.printStackTrace() }
        }
    }

    /** Load ghi chú chung (hiển thị ở cuối timeline). */
    fun fetchGeneralNotes(itineraryId: String) {
        viewModelScope.launch {
            itineraryRepo.getGeneralNotes(itineraryId)
                .onSuccess { notes ->
                    _uiState.update { state ->
                        val newMap = state.generalNotesMap.toMutableMap()
                        newMap[itineraryId] = notes
                        state.copy(generalNotesMap = newMap)
                    }
                }
                .onFailure { it.printStackTrace() }
        }
    }

    /**
     * Thêm ghi chú nhóm.
     * - itemId != null → ghi chú gắn với địa điểm cụ thể
     * - itemId == null → ghi chú chung ở cuối timeline
     */
    fun addNote(
        itineraryId: String,
        content: String,
        imageUrl: String? = null,
        imageUri: Uri? = null,
        contentResolver: android.content.ContentResolver? = null,
        itemId: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            var uploadedImageUrl = imageUrl
            if (imageUri != null && contentResolver != null) {
                try {
                    val bytes = contentResolver.openInputStream(imageUri)?.readBytes()
                    if (bytes != null) {
                        itineraryRepo.uploadNoteImage(bytes, "note_${System.currentTimeMillis()}.jpg")
                            .onSuccess { uploadedImageUrl = it }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val trimmed = content.trim()
            if (trimmed.isBlank() && uploadedImageUrl.isNullOrBlank()) return@launch
            val finalContent = trimmed.ifBlank { "📷" }

            itineraryRepo.addNote(itineraryId, finalContent, uploadedImageUrl, itemId)
                .onSuccess { newNote ->
                    _uiState.update { state ->
                        if (itemId != null) {
                            val key = "$itineraryId-$itemId"
                            val newMap = state.notesMap.toMutableMap()
                            newMap[key] = (newMap[key] ?: emptyList()) + newNote
                            state.copy(notesMap = newMap)
                        } else {
                            val newMap = state.generalNotesMap.toMutableMap()
                            newMap[itineraryId] = (newMap[itineraryId] ?: emptyList()) + newNote
                            state.copy(generalNotesMap = newMap)
                        }
                    }
                    onSuccess()
                }
                .onFailure {
                    it.printStackTrace()
                    onFailure(it.message ?: "Lỗi thêm ghi chú")
                }
        }
    }

    /** Xóa ghi chú nhóm. */
    fun deleteNote(
        itineraryId: String,
        noteId: String,
        itemId: String? = null
    ) {
        viewModelScope.launch {
            itineraryRepo.deleteNote(itineraryId, noteId)
                .onSuccess {
                    _uiState.update { state ->
                        if (itemId != null) {
                            val key = "$itineraryId-$itemId"
                            val newMap = state.notesMap.toMutableMap()
                            newMap[key] = (newMap[key] ?: emptyList()).filterNot { it.id == noteId }
                            state.copy(notesMap = newMap)
                        } else {
                            val newMap = state.generalNotesMap.toMutableMap()
                            newMap[itineraryId] = (newMap[itineraryId] ?: emptyList()).filterNot { it.id == noteId }
                            state.copy(generalNotesMap = newMap)
                        }
                    }
                }
                .onFailure { it.printStackTrace() }
        }
    }

    /** Cập nhật ghi chú riêng của địa điểm (itinerary_items.note). */
    fun updateItemNote(
        itineraryId: String,
        day: String,
        itemId: String,
        note: String?,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            itineraryRepo.updateItemNote(itineraryId, itemId, note)
                .onSuccess { updatedItem ->
                    _uiState.update { state ->
                        val key = "$itineraryId-$day"
                        val newMap = state.timelineMap.toMutableMap()
                        newMap[key] = (newMap[key] ?: emptyList()).map {
                            if (it.id == itemId) updatedItem else it
                        }
                        state.copy(timelineMap = newMap)
                    }
                    onSuccess()
                }
                .onFailure {
                    it.printStackTrace()
                    onFailure(it.message ?: "Lỗi cập nhật ghi chú")
                }
        }
    }
}
