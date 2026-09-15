package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sub_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subcategory_category_name",
                        columnNames = {"category_id", "name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_subcategory_category",
                        columnList = "category_id"
                ),
                @Index(
                        name = "idx_subcategory_status",
                        columnList = "enabled"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subcategory name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Subcategory name must be between 2 and 100 characters"
    )
    @Column(nullable = false, length = 100)
    private String name;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String image;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_subcategory_category"
            )
    )
    private Category category;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}