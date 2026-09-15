package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Notification;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.NotificationType;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.NotificationRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Override
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String actionUrl,
            Long referenceId,
            String referenceKey
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Notification type is required."
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification title is required."
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message is required."
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );

        if (user.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Notifications can only be created for customers."
            );
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "Customer account is disabled."
            );
        }

        String cleanedTitle =
                title.trim();

        String cleanedMessage =
                message.trim();

        String cleanedActionUrl =
                actionUrl == null || actionUrl.isBlank()
                        ? null
                        : actionUrl.trim();

        String cleanedReferenceKey =
                referenceKey == null || referenceKey.isBlank()
                        ? null
                        : referenceKey.trim();

        Notification notification =
                Notification.builder()
                        .user(user)
                        .type(type)
                        .title(cleanedTitle)
                        .message(cleanedMessage)
                        .actionUrl(cleanedActionUrl)
                        .referenceId(referenceId)
                        .referenceKey(cleanedReferenceKey)
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // CREATE IF NOT EXISTS
    // =========================================================

    @Override
    public Notification createNotificationIfNotExists(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String actionUrl,
            Long referenceId,
            String referenceKey
    ) {

        /*
         * If there is no reference key, there is no reliable
         * way to identify this notification as a duplicate.
         *
         * In that case simply create it.
         */
        if (referenceKey == null
                || referenceKey.isBlank()) {

            return createNotification(
                    userId,
                    type,
                    title,
                    message,
                    actionUrl,
                    referenceId,
                    null
            );
        }

        String cleanedReferenceKey =
                referenceKey.trim();

        return notificationRepository
                .findByReferenceKey(
                        cleanedReferenceKey
                )
                .orElseGet(() ->
                        createNotification(
                                userId,
                                type,
                                title,
                                message,
                                actionUrl,
                                referenceId,
                                cleanedReferenceKey
                        )
                );
    }


    // =========================================================
    // CUSTOMER - ALL NOTIFICATIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getCustomerNotifications(
            String email,
            Pageable pageable
    ) {

        User user =
                getCustomer(email);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }


    // =========================================================
    // CUSTOMER - UNREAD NOTIFICATIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(
            String email,
            Pageable pageable
    ) {

        User user =
                getCustomer(email);

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }


    // =========================================================
    // CUSTOMER - SINGLE NOTIFICATION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Notification getCustomerNotification(
            String email,
            Long notificationId
    ) {

        User user =
                getCustomer(email);

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "Notification ID is required."
            );
        }

        return notificationRepository
                .findByIdAndUserId(
                        notificationId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found."
                        )
                );
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @Override
    public void markAsRead(
            String email,
            Long notificationId
    ) {

        Notification notification =
                getCustomerNotification(
                        email,
                        notificationId
                );

        if (!notification.isRead()) {

            notification.markAsRead();

            notificationRepository.save(
                    notification
            );
        }
    }


    // =========================================================
    // MARK AS UNREAD
    // =========================================================

    @Override
    public void markAsUnread(
            String email,
            Long notificationId
    ) {

        Notification notification =
                getCustomerNotification(
                        email,
                        notificationId
                );

        if (notification.isRead()) {

            notification.markAsUnread();

            notificationRepository.save(
                    notification
            );
        }
    }


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @Override
    public void markAllAsRead(
            String email
    ) {

        User user =
                getCustomer(email);

        Page<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                user.getId(),
                                Pageable.unpaged()
                        );

        if (notifications.isEmpty()) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        for (Notification notification
                : notifications.getContent()) {

            notification.setRead(true);
            notification.setReadAt(now);
        }

        notificationRepository.saveAll(
                notifications.getContent()
        );
    }


    // =========================================================
    // DELETE ONE
    // =========================================================

    @Override
    public void deleteNotification(
            String email,
            Long notificationId
    ) {

        Notification notification =
                getCustomerNotification(
                        email,
                        notificationId
                );

        notificationRepository.delete(
                notification
        );
    }


    // =========================================================
    // DELETE READ NOTIFICATIONS
    // =========================================================

    @Override
    public long deleteReadNotifications(
            String email
    ) {

        User user =
                getCustomer(email);

        return notificationRepository
                .deleteByUserIdAndReadTrue(
                        user.getId()
                );
    }


    // =========================================================
    // DELETE ALL NOTIFICATIONS
    // =========================================================

    @Override
    public void deleteAllNotifications(
            String email
    ) {

        User user =
                getCustomer(email);

        notificationRepository.deleteByUserId(
                user.getId()
        );
    }


    // =========================================================
    // UNREAD COUNT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(
            String email
    ) {

        User user =
                getCustomer(email);

        return notificationRepository
                .countByUserIdAndReadFalse(
                        user.getId()
                );
    }


    // =========================================================
    // CHECK DUPLICATE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public boolean notificationExists(
            String referenceKey
    ) {

        if (referenceKey == null
                || referenceKey.isBlank()) {

            return false;
        }

        return notificationRepository
                .existsByReferenceKey(
                        referenceKey.trim()
                );
    }


    // =========================================================
    // PRIVATE - CUSTOMER VALIDATION
    // =========================================================

    private User getCustomer(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "User email is required."
            );
        }

        User user =
                userRepository
                        .findByEmail(
                                email.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found."
                                )
                        );

        if (user.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customers can access notifications."
            );
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "Your account is disabled."
            );
        }

        return user;
    }
}