package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Notification;
import org.example.dreamzshop.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {


    /*
     * All notifications for a particular user.
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );


    /*
     * Unread notifications.
     */
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );


    /*
     * Count unread notifications.
     */
    long countByUserIdAndReadFalse(
            Long userId
    );


    /*
     * Find one notification belonging to a user.
     *
     * This prevents a customer from accessing
     * another customer's notification.
     */
    Optional<Notification> findByIdAndUserId(
            Long id,
            Long userId
    );


    /*
     * Find notification by reference key.
     *
     * Used to prevent duplicate event notifications.
     */
    Optional<Notification> findByReferenceKey(
            String referenceKey
    );


    /*
     * Check duplicate notification.
     */
    boolean existsByReferenceKey(
            String referenceKey
    );


    /*
     * Notifications of a specific type for a user.
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
            Long userId,
            NotificationType type,
            Pageable pageable
    );


    /*
     * Count notifications of a specific type.
     */
    long countByUserIdAndType(
            Long userId,
            NotificationType type
    );


    /*
     * Delete all notifications belonging to a user.
     */
    void deleteByUserId(
            Long userId
    );


    /*
     * Delete read notifications of a user.
     */
    long deleteByUserIdAndReadTrue(
            Long userId
    );


    /*
     * Latest unread notification count.
     *
     * Same as countByUserIdAndReadFalse(), but kept as a
     * query method for future extension.
     */
    @Query("""
            SELECT COUNT(n)
            FROM Notification n
            WHERE n.user.id = :userId
              AND n.read = false
            """)
    long countUnreadByUserId(
            @Param("userId") Long userId
    );
}