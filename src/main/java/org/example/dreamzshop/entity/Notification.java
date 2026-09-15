package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.dreamzshop.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notification_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_notification_read",
                        columnList = "is_read"
                ),
                @Index(
                        name = "idx_notification_created",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_notification_type",
                        columnList = "type"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Customer who receives this notification.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    /*
     * Notification type.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 50
    )
    private NotificationType type;


    /*
     * Short notification title.
     */
    @NotBlank
    @Size(max = 150)
    @Column(
            name = "title",
            nullable = false,
            length = 150
    )
    private String title;


    /*
     * Complete notification message.
     */
    @NotBlank
    @Size(max = 1000)
    @Column(
            name = "message",
            nullable = false,
            length = 1000
    )
    private String message;


    /*
     * Optional URL/action target.
     *
     * Example:
     * /customer/orders/15
     */
    @Size(max = 500)
    @Column(
            name = "action_url",
            length = 500
    )
    private String actionUrl;


    /*
     * Related entity ID.
     *
     * Example:
     * Order ID, Return ID, Refund ID, Review ID.
     */
    @Column(name = "reference_id")
    private Long referenceId;


    /*
     * Prevent duplicate notifications when
     * the same event is processed more than once.
     */
    @Size(max = 100)
    @Column(
            name = "reference_key",
            length = 100
    )
    private String referenceKey;


    /*
     * Read / unread state.
     */
    @Column(
            name = "is_read",
            nullable = false
    )
    @Builder.Default
    private boolean read = false;


    /*
     * Date and time when notification was read.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;


    /*
     * Notification creation time.
     */
    @Column(
            name = "created_at",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();


    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (type == null) {
            type = NotificationType.SYSTEM;
        }

        if (read) {
            if (readAt == null) {
                readAt = LocalDateTime.now();
            }
        } else {
            readAt = null;
        }
    }


    /*
     * Mark notification as read.
     */
    public void markAsRead() {

        this.read = true;

        this.readAt =
                LocalDateTime.now();
    }


    /*
     * Mark notification as unread.
     */
    public void markAsUnread() {

        this.read = false;

        this.readAt = null;
    }
}