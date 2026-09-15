package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "banners",
        indexes = {
                @Index(name = "idx_banner_enabled", columnList = "enabled"),
                @Index(name = "idx_banner_display_order", columnList = "display_order"),
                @Index(name = "idx_banner_start_date", columnList = "start_date"),
                @Index(name = "idx_banner_end_date", columnList = "end_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // BANNER CONTENT
    // =========================================================

    @NotBlank(message = "Banner title is required")
    @Size(max = 150, message = "Banner title cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;


    @Size(max = 300, message = "Subtitle cannot exceed 300 characters")
    @Column(length = 300)
    private String subtitle;


    @Size(max = 100, message = "Button text cannot exceed 100 characters")
    @Column(name = "button_text", length = 100)
    private String buttonText;


    @Size(max = 500, message = "Button URL cannot exceed 500 characters")
    @Column(name = "button_url", length = 500)
    private String buttonUrl;


    // =========================================================
    // IMAGE
    // =========================================================

    @NotBlank(message = "Banner image is required")
    @Size(max = 1000, message = "Image URL cannot exceed 1000 characters")
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;


    // =========================================================
    // DISPLAY
    // =========================================================

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 0;


    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;


    // =========================================================
    // SCHEDULING
    // =========================================================

    @Column(name = "start_date")
    private LocalDateTime startDate;


    @Column(name = "end_date")
    private LocalDateTime endDate;


    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========================================================
    // LIFECYCLE
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (displayOrder == null) {
            displayOrder = 0;
        }

        if (enabled == null) {
            enabled = true;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }


    // =========================================================
    // ACTIVE CHECK
    // =========================================================

    public boolean isCurrentlyActive() {

        if (!Boolean.TRUE.equals(enabled)) {
            return false;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (startDate != null
                && now.isBefore(startDate)) {

            return false;
        }

        if (endDate != null
                && now.isAfter(endDate)) {

            return false;
        }

        return true;
    }
}