package com.example.vietnam_travel_itinerary_android.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.dto.NotificationDto
import com.example.vietnam_travel_itinerary_android.data.repository.CommunityRepository
import com.example.vietnam_travel_itinerary_android.data.repository.ItineraryRepository
import com.example.vietnam_travel_itinerary_android.data.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime

class NotificationViewModel(
    private val repository: CommunityRepository,
    private val profileRepository: ProfileRepository,
    private val itineraryRepository: ItineraryRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationUiModel>>(emptyList())
    val notifications: StateFlow<List<NotificationUiModel>> = _notifications.asStateFlow()

    private val _selectedTab = MutableStateFlow(NotifTab.ALL)
    val selectedTab: StateFlow<NotifTab> = _selectedTab.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _tabUnreadCounts = MutableStateFlow<Map<NotifTab, Int>>(emptyMap())
    val tabUnreadCounts: StateFlow<Map<NotifTab, Int>> = _tabUnreadCounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 30
    private var hasMore = true

    private val currentUserId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    init {
        refreshAll()
    }

    fun refreshAll() {
        if (currentUserId.isBlank()) return
        loadUnreadCount()
        refreshTabUnreadCounts()
        loadNotifications()
    }

    fun selectTab(tab: NotifTab) {
        _selectedTab.value = tab
        currentOffset = 0
        hasMore = true
        loadNotifications()
    }

    fun loadNotifications() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val raw = repository.getNotifications(
                    userId = currentUserId,
                    limit = pageSize,
                    offset = 0,
                    category = _selectedTab.value.category
                )
                currentOffset = raw.size
                hasMore = raw.size >= pageSize
                _notifications.value = groupNotifications(raw.map { it.toUiModel() })
                refreshTabUnreadCounts()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value || !hasMore) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val raw = repository.getNotifications(
                    userId = currentUserId,
                    limit = pageSize,
                    offset = currentOffset,
                    category = _selectedTab.value.category
                )
                currentOffset += raw.size
                hasMore = raw.size >= pageSize
                val merged = _notifications.value + groupNotifications(raw.map { it.toUiModel() })
                _notifications.value = dedupeGrouped(merged)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            if (currentUserId.isBlank()) return@launch
            _unreadCount.value = repository.getUnreadNotificationCount()
        }
    }

    fun refreshTabUnreadCounts() {
        viewModelScope.launch {
            if (currentUserId.isBlank()) return@launch
            try {
                val all = repository.getNotifications(
                    userId = currentUserId,
                    limit = 100,
                    offset = 0,
                    category = null,
                )
                val counts = NotifTab.entries.associateWith { tab ->
                    when (tab) {
                        NotifTab.ALL -> all.count { !it.is_read }
                        else -> all.count { !it.is_read && notifTypeStringToTab(it.notif_type) == tab }
                    }
                }
                _tabUnreadCounts.value = counts
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(notifId: String) {
        viewModelScope.launch {
            if (repository.markNotificationAsRead(notifId)) {
                _notifications.update { list ->
                    list.map { if (it.id == notifId) it.copy(isRead = true) else it }
                }
                _unreadCount.update { (it - 1).coerceAtLeast(0) }
                refreshTabUnreadCounts()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            if (repository.markNotificationsAsRead(currentUserId)) {
                _notifications.update { list -> list.map { it.copy(isRead = true) } }
                _unreadCount.value = 0
                _tabUnreadCounts.value = NotifTab.entries.associateWith { 0 }
            }
        }
    }

    fun followBack(userId: String) {
        viewModelScope.launch {
            profileRepository.followUser(userId)
        }
    }

    fun acceptItineraryInvite(notifId: String, itineraryId: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = itineraryRepository.acceptInvite(itineraryId)
            if (result.isSuccess) {
                markAsRead(notifId)
                loadNotifications()
                loadUnreadCount()
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    fun declineItineraryInvite(notifId: String, itineraryId: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = itineraryRepository.declineInvite(itineraryId)
            if (result.isSuccess) {
                markAsRead(notifId)
                loadNotifications()
                loadUnreadCount()
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    private fun groupNotifications(items: List<NotificationUiModel>): List<NotificationUiModel> {
        val grouped = mutableListOf<NotificationUiModel>()
        val seenKeys = mutableSetOf<String>()

        for (item in items) {
            val key = item.effectiveGroupKey()
            if (key != null && item.type == NotificationType.REACTION) {
                if (seenKeys.contains(key)) continue
                val sameGroup = items.filter { it.effectiveGroupKey() == key }
                if (sameGroup.size > 1) {
                    val primary = sameGroup.first()
                    grouped.add(
                        primary.copy(
                            groupedCount = sameGroup.size - 1,
                            groupedActors = sameGroup.drop(1).mapNotNull { n ->
                                n.actorId?.let {
                                    ActorInfo(it, n.actorName, n.actorUsername, n.actorAvatarUrl)
                                }
                            },
                            isRead = sameGroup.all { it.isRead },
                        )
                    )
                    seenKeys.add(key)
                    continue
                }
            }
            grouped.add(item)
        }
        return grouped
    }

    private fun dedupeGrouped(items: List<NotificationUiModel>): List<NotificationUiModel> {
        val seen = mutableSetOf<String>()
        return items.filter { seen.add(it.id) }
    }

    private fun NotificationUiModel.effectiveGroupKey(): String? {
        if (!groupKey.isNullOrBlank()) return groupKey
        if (postId == null) return null
        return when (type) {
            NotificationType.REACTION -> "reaction:$postId"
            NotificationType.COMMENT -> "comment:$postId"
            else -> null
        }
    }

    private fun NotificationDto.toUiModel(): NotificationUiModel {
        return NotificationUiModel(
            id = id,
            type = NotificationType.fromBackend(notif_type),
            actorName = actor?.name ?: actor_username ?: "Ai đó",
            actorUsername = actor_username,
            actorAvatarUrl = actor?.avatar_url,
            previewText = preview_text,
            timeAgo = formatTimeAgo(created_at),
            isRead = is_read,
            postId = post_id,
            commentId = comment_id,
            itineraryId = itinerary_id,
            itineraryTitle = itinerary_title,
            actorId = actor_id,
            groupKey = group_key,
        )
    }

    private fun formatTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val created = OffsetDateTime.parse(isoString)
            val minutes = Duration.between(created, OffsetDateTime.now()).toMinutes()
            when {
                minutes < 1 -> "Vừa xong"
                minutes < 60 -> "$minutes phút trước"
                minutes < 1440 -> "${minutes / 60} giờ trước"
                minutes < 2880 -> "Hôm qua"
                else -> "${minutes / 1440} ngày trước"
            }
        } catch (_: Exception) {
            ""
        }
    }
}
