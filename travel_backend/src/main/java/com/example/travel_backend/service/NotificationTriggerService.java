package com.example.travel_backend.service;

import com.example.travel_backend.entity.*;
import com.example.travel_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationTriggerService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private ItineraryRepository itineraryRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private UserSettingsRepository userSettingsRepository;
    @Autowired private ItineraryCollaboratorRepository collaboratorRepository;

    public void notifyFollow(UUID actorId, UUID targetUserId) {
        if (!shouldNotify(actorId, targetUserId, "follow")) return;
        saveNotification(targetUserId, actorId, "follow", null, null, null, null, null, null, null);
    }

    public void notifyReaction(UUID actorId, UUID postOwnerId, UUID postId, String reactionType) {
        if (!shouldNotify(actorId, postOwnerId, "reaction")) return;
        saveNotification(postOwnerId, actorId, "reaction", postId, null, null, null,
                reactionType, "reaction:" + postId, null);
    }

    public void notifyComment(UUID actorId, UUID postOwnerId, UUID postId, UUID commentId, String preview) {
        if (shouldNotify(actorId, postOwnerId, "comment")) {
            saveNotification(postOwnerId, actorId, "comment", postId, commentId, null, null,
                    truncate(preview), "comment:" + postId, null);
        }
    }

    public void notifyCommentReaction(UUID actorId, UUID commentOwnerId, UUID commentId, UUID postId, String reactionType) {
        if (!shouldNotify(actorId, commentOwnerId, "comment_reaction")) return;
        saveNotification(commentOwnerId, actorId, "comment_reaction", postId, commentId, null, null,
                reactionType, postId != null ? "comment_reaction:" + postId : null, null);
    }

    public void notifyRepost(UUID actorId, UUID postOwnerId, UUID postId) {
        if (!shouldNotify(actorId, postOwnerId, "repost")) return;
        saveNotification(postOwnerId, actorId, "repost", postId, null, null, null,
                null, "repost:" + postId, null);
    }

    public void notifyMention(UUID actorId, UUID mentionedUserId, UUID postId, UUID commentId) {
        if (!shouldNotify(actorId, mentionedUserId, "mention")) return;
        saveNotification(mentionedUserId, actorId, "mention", postId, commentId, null, null,
                null, "mention:" + postId, null);
    }

    public void notifyItineraryInvite(UUID actorId, UUID invitedUserId, UUID itineraryId) {
        if (!shouldNotify(actorId, invitedUserId, "itinerary_invite")) return;
        saveNotification(invitedUserId, actorId, "itinerary_invite", null, null, itineraryId, null,
                null, null, null);
    }

    public void notifyItineraryUpdated(UUID actorId, UUID itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId).orElse(null);
        if (itinerary == null) return;

        UUID ownerId = itinerary.getUser().getId();
        if (!actorId.equals(ownerId) && shouldNotify(actorId, ownerId, "itinerary_updated")) {
            saveNotification(ownerId, actorId, "itinerary_updated", null, null, itineraryId, null,
                    null, null, null);
        }

        List<ItineraryCollaborator> collaborators = collaboratorRepository.findByItinerary_IdAndStatusIgnoreCase(itineraryId, "accepted");
        for (ItineraryCollaborator collab : collaborators) {
            userRepository.findByEmail(collab.getEmail().trim().toLowerCase()).ifPresent(user -> {
                if (shouldNotify(actorId, user.getId(), "itinerary_updated")) {
                    saveNotification(user.getId(), actorId, "itinerary_updated", null, null, itineraryId, null,
                            null, null, null);
                }
            });
        }
    }

    public void notifyPlaceSuggestionApproved(UUID adminId, UUID suggesterId, UUID suggestionId) {
        if (!shouldNotify(adminId, suggesterId, "place_suggestion_approved")) return;
        saveNotification(suggesterId, adminId, "place_suggestion_approved", null, null, null, suggestionId,
                null, null, null);
    }

    /**
     * Thông báo cho tất cả thành viên của lịch trình khi có người thêm ghi chú nhóm mới.
     * actorId = người vừa thêm ghi chú.
     */
    public void notifyItineraryNote(UUID actorId, UUID itineraryId, String notePreview) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId).orElse(null);
        if (itinerary == null) return;

        // Thông báo cho owner (nếu không phải người ghi chú)
        UUID ownerId = itinerary.getUser().getId();
        if (!actorId.equals(ownerId) && shouldNotify(actorId, ownerId, "itinerary_updated")) {
            saveNotification(ownerId, actorId, "itinerary_updated", null, null, itineraryId, null,
                    truncate(notePreview), "note:" + itineraryId, null);
        }

        // Thông báo cho tất cả collaborator đã accepted
        List<ItineraryCollaborator> collaborators = collaboratorRepository
                .findByItinerary_IdAndStatusIgnoreCase(itineraryId, "accepted");
        for (ItineraryCollaborator collab : collaborators) {
            userRepository.findByEmail(collab.getEmail().trim().toLowerCase()).ifPresent(user -> {
                if (!actorId.equals(user.getId()) && shouldNotify(actorId, user.getId(), "itinerary_updated")) {
                    saveNotification(user.getId(), actorId, "itinerary_updated", null, null, itineraryId, null,
                            truncate(notePreview), "note:" + itineraryId, null);
                }
            });
        }
    }

    private boolean shouldNotify(UUID actorId, UUID targetUserId, String notificationType) {
        if (actorId == null || targetUserId == null || actorId.equals(targetUserId)) return false;
        if (userBlockRepository.existsByBlockerIdAndBlockedId(targetUserId, actorId)) return false;
        if (userBlockRepository.existsByBlockerIdAndBlockedId(actorId, targetUserId)) return false;

        Optional<UserSetting> settingsOpt = userSettingsRepository.findByUserId(targetUserId);
        if (settingsOpt.isEmpty()) return true;

        UserSetting settings = settingsOpt.get();
        return switch (notificationType) {
            case "follow" -> Boolean.TRUE.equals(settings.getPushFollows());
            case "reaction" -> Boolean.TRUE.equals(settings.getPushReactions());
            case "comment", "comment_reaction" -> Boolean.TRUE.equals(settings.getPushComments());
            case "repost" -> Boolean.TRUE.equals(settings.getPushReposts());
            case "mention" -> Boolean.TRUE.equals(settings.getPushMentions());
            case "achievement" -> Boolean.TRUE.equals(settings.getPushAchievements());
            default -> true;
        };
    }

    private void saveNotification(
            UUID targetUserId, UUID actorId, String type,
            UUID postId, UUID commentId, UUID itineraryId, UUID placeSuggestionId,
            String previewText, String groupKey, String reactionType) {

        Notification notification = new Notification();
        notification.setUser(userRepository.getReferenceById(targetUserId));
        notification.setActor(userRepository.getReferenceById(actorId));
        notification.setType(type);
        if (postId != null) notification.setPost(postRepository.getReferenceById(postId));
        if (commentId != null) notification.setComment(commentRepository.getReferenceById(commentId));
        if (itineraryId != null) notification.setItinerary(itineraryRepository.getReferenceById(itineraryId));
        notification.setPlaceSuggestionId(placeSuggestionId);
        notification.setPreviewText(previewText);
        notification.setGroupKey(groupKey);
        notification.setReactionType(reactionType);
        notification.setIsRead(false);
        notification.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }
}
