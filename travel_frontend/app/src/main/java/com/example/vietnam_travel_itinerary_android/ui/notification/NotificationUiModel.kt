package com.example.vietnam_travel_itinerary_android.ui.notification

enum class FollowBackStatus {
    SHOW_BUTTON, FOLLOWED;

    companion object {
        fun fromGroupKey(groupKey: String?): FollowBackStatus = when (groupKey?.lowercase()) {
            "follow:back" -> FOLLOWED
            else -> SHOW_BUTTON
        }
    }
}

enum class ItineraryInviteStatus {
    PENDING, ACCEPTED, DECLINED;

    companion object {
        fun fromGroupKey(groupKey: String?): ItineraryInviteStatus = when (groupKey?.lowercase()) {
            "invite:accepted" -> ACCEPTED
            "invite:declined" -> DECLINED
            else -> PENDING
        }
    }
}

data class ActorInfo(
    val id: String,
    val name: String,
    val username: String?,
    val avatarUrl: String?,
)

data class NotificationUiModel(
    val id: String,
    val type: NotificationType,
    val actorName: String,
    val actorUsername: String?,
    val actorAvatarUrl: String?,
    val previewText: String?,
    val timeAgo: String,
    val isRead: Boolean,
    val groupedCount: Int = 0,
    val groupedActors: List<ActorInfo> = emptyList(),
    val postId: String? = null,
    val commentId: String? = null,
    val itineraryId: String? = null,
    val itineraryTitle: String? = null,
    val actorId: String? = null,
    val groupKey: String? = null,
    val inviteStatus: ItineraryInviteStatus = ItineraryInviteStatus.PENDING,
    val followBackStatus: FollowBackStatus = FollowBackStatus.SHOW_BUTTON,
)

enum class NotificationType {
    FOLLOW, REACTION, COMMENT, COMMENT_REACTION,
    REPOST, MENTION, ACHIEVEMENT,
    ITINERARY_INVITE, ITINERARY_UPDATED,
    PLACE_SUGGESTION_APPROVED;

    companion object {
        fun fromBackend(type: String?): NotificationType = when (type?.lowercase()) {
            "follow" -> FOLLOW
            "reaction" -> REACTION
            "comment" -> COMMENT
            "comment_reaction" -> COMMENT_REACTION
            "repost" -> REPOST
            "mention" -> MENTION
            "achievement" -> ACHIEVEMENT
            "itinerary_invite" -> ITINERARY_INVITE
            "itinerary_updated" -> ITINERARY_UPDATED
            "place_suggestion_approved" -> PLACE_SUGGESTION_APPROVED
            else -> REACTION
        }
    }
}

enum class NotifTab(val label: String, val category: String?) {
    ALL("Tất cả", null),
    INTERACTION("Tương tác", "interaction"),
    TRAVEL("Du lịch", "travel"),
    SYSTEM("Hệ thống", "system"),
}

fun NotificationType.toNotifTab(): NotifTab? = when (this) {
    NotificationType.FOLLOW,
    NotificationType.REACTION,
    NotificationType.COMMENT,
    NotificationType.COMMENT_REACTION,
    NotificationType.REPOST,
    NotificationType.MENTION -> NotifTab.INTERACTION
    NotificationType.ITINERARY_INVITE,
    NotificationType.ITINERARY_UPDATED -> NotifTab.TRAVEL
    NotificationType.ACHIEVEMENT,
    NotificationType.PLACE_SUGGESTION_APPROVED -> NotifTab.SYSTEM
}

fun notifTypeStringToTab(type: String?): NotifTab? =
    NotificationType.fromBackend(type).toNotifTab()
