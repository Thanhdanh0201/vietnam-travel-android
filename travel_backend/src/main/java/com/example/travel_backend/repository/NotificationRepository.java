package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND (n.isDeleted = false OR n.isDeleted IS NULL) ORDER BY n.createdAt DESC")
    Page<Notification> findActiveByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.type IN :types AND (n.isDeleted = false OR n.isDeleted IS NULL) ORDER BY n.createdAt DESC")
    Page<Notification> findActiveByUserIdAndTypeInOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("types") List<String> types,
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND (n.isDeleted = false OR n.isDeleted IS NULL)")
    long countActiveUnreadByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false AND (n.isDeleted = false OR n.isDeleted IS NULL)")
    void markAllAsRead(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notifId AND n.user.id = :userId AND (n.isDeleted = false OR n.isDeleted IS NULL)")
    void markAsRead(@Param("notifId") UUID notifId, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.id = :notifId AND n.user.id = :userId AND (n.isDeleted = false OR n.isDeleted IS NULL)")
    void softDeleteByIdAndUserId(@Param("notifId") UUID notifId, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.id IN :ids AND n.user.id = :userId AND (n.isDeleted = false OR n.isDeleted IS NULL)")
    void softDeleteByIdInAndUserId(@Param("ids") List<UUID> ids, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.groupKey = :groupKey, n.isRead = true
            WHERE n.user.id = :userId
              AND n.itinerary.id = :itineraryId
              AND n.type = 'itinerary_invite'
              AND (n.isDeleted = false OR n.isDeleted IS NULL)
            """)
    void resolveItineraryInviteNotifications(
            @Param("userId") UUID userId,
            @Param("itineraryId") UUID itineraryId,
            @Param("groupKey") String groupKey);
}
