package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import lombok.*;

import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.PaymentMethod;
import org.example.dreamzshop.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "order_status"),
                @Index(name = "idx_order_payment_status", columnList = "payment_status"),
                @Index(name = "idx_order_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 30
    )
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_user")
    )
    private User user;

    /*
     * Address snapshot.
     *
     * We intentionally store the delivery address inside
     * the order instead of depending only on the Address table.
     *
     * This means if the customer later edits their address,
     * the old order still contains the original delivery details.
     */

    @Column(nullable = false, length = 100)
    private String shippingFullName;

    @Column(nullable = false, length = 15)
    private String shippingPhone;

    @Column(nullable = false, length = 200)
    private String shippingAddressLine1;

    @Column(length = 200)
    private String shippingAddressLine2;

    @Column(nullable = false, length = 100)
    private String shippingCity;

    @Column(nullable = false, length = 100)
    private String shippingState;

    @Column(nullable = false, length = 10)
    private String shippingPincode;

    @Column(length = 100)
    private String shippingLandmark;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "order_status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.RAZORPAY;

    @Column(length = 100)
    private String razorpayOrderId;

    @Column(length = 100)
    private String razorpayPaymentId;

    @Column(length = 200)
    private String razorpaySignature;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(length = 500)
    private String customerNotes;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public void addItem(OrderItem item) {

        items.add(item);

        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {

        items.remove(item);

        item.setOrder(null);
    }

    public int getTotalItems() {

        return items.stream()
                .filter(item -> item.getQuantity() != null)
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (couponDiscount == null) {
            couponDiscount = BigDecimal.ZERO;
        }

        if (taxAmount == null) {
            taxAmount = BigDecimal.ZERO;
        }

        if (deliveryCharge == null) {
            deliveryCharge = BigDecimal.ZERO;
        }

        if (grandTotal == null) {
            grandTotal = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}