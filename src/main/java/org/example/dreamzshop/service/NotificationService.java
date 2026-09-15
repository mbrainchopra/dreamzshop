package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Notification;
import org.example.dreamzshop.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /*
     * Create a notification for a user.
     */
    Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String actionUrl,
            Long referenceId,
            String referenceKey
    );


    /*
     * Get all notifications of the logged-in customer.
     */
    Page<Notification> getCustomerNotifications(
            String email,
            Pageable pageable
    );


    /*
     * Get only unread notifications.
     */
    Page<Notification> getUnreadNotifications(
            String email,
            Pageable pageable
    );


    /*
     * Get one notification belonging to the customer.
     */
    Notification getCustomerNotification(
            String email,
            Long notificationId
    );


    /*
     * Mark one notification as read.
     */
    void markAsRead(
            String email,
            Long notificationId
    );


    /*
     * Mark one notification as unread.
     */
    void markAsUnread(
            String email,
            Long notificationId
    );


    /*
     * Mark all customer notifications as read.
     */
    void markAllAsRead(
            String email
    );


    /*
     * Delete one customer notification.
     */
    void deleteNotification(
            String email,
            Long notificationId
    );


    /*
     * Delete all read notifications.
     */
    long deleteReadNotifications(
            String email
    );


    /*
     * Delete all customer notifications.
     */
    void deleteAllNotifications(
            String email
    );


    /*
     * Unread notification count.
     *
     * This will be used in the navbar notification bell.
     */
    long getUnreadCount(
            String email
    );


    /*
     * Check whether a notification already exists
     * for a particular event.
     */
    boolean notificationExists(
            String referenceKey
    );


    /*
     * Create notification only when the event has not
     * already been processed.
     */
    Notification createNotificationIfNotExists(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String actionUrl,
            Long referenceId,
            String referenceKey
    );
}