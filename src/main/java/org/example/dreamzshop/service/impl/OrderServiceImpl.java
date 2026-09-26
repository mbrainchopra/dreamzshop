package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.dto.PricingResult;
import org.example.dreamzshop.entity.Address;
import org.example.dreamzshop.entity.Cart;
import org.example.dreamzshop.entity.CartItem;
import org.example.dreamzshop.entity.Coupon;
import org.example.dreamzshop.entity.InventoryTransaction;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.OrderItem;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.InventoryTransactionType;
import org.example.dreamzshop.enums.NotificationType;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.PaymentMethod;
import org.example.dreamzshop.enums.PaymentStatus;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.repository.AddressRepository;
import org.example.dreamzshop.repository.CartRepository;
import org.example.dreamzshop.repository.CouponUsageRepository;
import org.example.dreamzshop.repository.InventoryTransactionRepository;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.CouponService;
import org.example.dreamzshop.service.NotificationService;
import org.example.dreamzshop.service.OrderService;
import org.example.dreamzshop.service.PricingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final AddressRepository addressRepository;

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final InventoryTransactionRepository
            inventoryTransactionRepository;

    private final CouponService couponService;

    private final CouponUsageRepository couponUsageRepository;

    private final PricingService pricingService;

    private final NotificationService notificationService;


    // =========================================================
    // CREATE COD ORDER
    // =========================================================

    @Override
    public Order createCodOrder(
            String email,
            Long addressId,
            String couponCode
    ) {

        User user = getCustomer(email);


        Address address =
                addressRepository
                        .findByIdAndUserId(
                                addressId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Selected address was not found"
                                )
                        );


        Cart cart =
                cartRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Your cart is empty"
                                )
                        );


        if (cart.getItems() == null
                || cart.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Your cart is empty"
            );
        }


        // =====================================================
        // SERVER-SIDE PRICING
        // =====================================================

        PricingResult pricing =
                pricingService.calculateCartPricing(
                        cart,
                        couponCode
                );


        if (pricing == null) {

            throw new IllegalArgumentException(
                    "Unable to calculate order pricing"
            );
        }


        // =====================================================
        // STOCK VALIDATION
        // =====================================================

        for (CartItem cartItem : cart.getItems()) {

            Product product =
                    cartItem.getProduct();


            if (product == null) {

                throw new IllegalArgumentException(
                        "A product in your cart is no longer available"
                );
            }


            if (product.getStatus()
                    != ProductStatus.ACTIVE) {

                throw new IllegalArgumentException(
                        product.getName()
                                + " is currently unavailable"
                );
            }


            int stock =
                    product.getStockQuantity() == null
                            ? 0
                            : product.getStockQuantity();


            if (cartItem.getQuantity() > stock) {

                throw new IllegalArgumentException(
                        "Insufficient stock for "
                                + product.getName()
                );
            }
        }


        // =====================================================
        // ORDER NUMBER
        // =====================================================

        String orderNumber =
                generateOrderNumber();


        // =====================================================
        // CREATE ORDER
        // =====================================================

        Order order =
                Order.builder()
                        .orderNumber(orderNumber)
                        .user(user)

                        .shippingFullName(
                                address.getFullName()
                        )

                        .shippingPhone(
                                address.getPhone()
                        )

                        .shippingAddressLine1(
                                address.getAddressLine1()
                        )

                        .shippingAddressLine2(
                                address.getAddressLine2()
                        )

                        .shippingCity(
                                address.getCity()
                        )

                        .shippingState(
                                address.getState()
                        )

                        .shippingPincode(
                                address.getPincode()
                        )

                        .orderStatus(
                                OrderStatus.CONFIRMED
                        )

                        .paymentStatus(
                                PaymentStatus.PENDING
                        )

                        .paymentMethod(
                                PaymentMethod.COD
                        )

                        .subtotal(
                                pricing.getOriginalSubtotal()
                        )

                        .discountAmount(
                                pricing.getTotalDiscount()
                        )

                        .couponDiscount(
                                pricing.getCouponDiscount()
                        )

                        .taxAmount(
                                pricing.getTaxAmount()
                        )

                        .deliveryCharge(
                                pricing.getDeliveryCharge()
                        )

                        .grandTotal(
                                pricing.getGrandTotal()
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .updatedAt(
                                LocalDateTime.now()
                        )

                        .build();


        // =====================================================
        // CREATE ORDER ITEMS
        // =====================================================

        for (CartItem cartItem : cart.getItems()) {

            Product product =
                    cartItem.getProduct();


            BigDecimal unitPrice =
                    cartItem.getUnitPrice();

            if (unitPrice == null) {
                unitPrice = product.getSellingPrice();
            }


            BigDecimal itemSubtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );


            OrderItem orderItem =
                    OrderItem.builder()
                            .order(order)

                            .product(product)

                            .productName(
                                    product.getName()
                            )

                            .productSku(
                                    product.getSku()
                            )

                            .productImage(
                                    product.getMainImage()
                            )

                            .unitPrice(
                                    unitPrice
                            )

                            .quantity(
                                    cartItem.getQuantity()
                            )

                            .subtotal(
                                    itemSubtotal
                            )

                            .build();


            order.addItem(orderItem);
        }


        // =====================================================
        // SAVE ORDER
        // =====================================================

        Order savedOrder =
                orderRepository.save(order);


        // =====================================================
        // COUPON USAGE
        // =====================================================

        if (couponCode != null
                && !couponCode.trim().isEmpty()
                && pricing.getCouponDiscount() != null
                && pricing.getCouponDiscount()
                .compareTo(BigDecimal.ZERO) > 0) {

            Coupon coupon =
                    couponService.getCouponByCode(
                            couponCode.trim()
                    );


            if (coupon != null) {

                couponService.recordCouponUsage(
                        coupon,
                        user,
                        savedOrder,
                        pricing.getCouponDiscount()
                );
            }
        }


        // =====================================================
        // DEDUCT STOCK
        // =====================================================

        for (CartItem cartItem : cart.getItems()) {

            Product product =
                    cartItem.getProduct();


            int previousStock =
                    product.getStockQuantity() == null
                            ? 0
                            : product.getStockQuantity();


            int quantity =
                    cartItem.getQuantity();


            int newStock =
                    previousStock - quantity;


            product.setStockQuantity(
                    newStock
            );


            if (newStock <= 0) {

                product.setStockQuantity(0);

                product.setStatus(
                        ProductStatus.OUT_OF_STOCK
                );
            }


            productRepository.save(product);


            InventoryTransaction transaction =
                    InventoryTransaction.builder()
                            .product(product)

                            .quantity(quantity)

                            .previousStock(
                                    previousStock
                            )

                            .newStock(
                                    Math.max(newStock, 0)
                            )

                            .type(
                                    InventoryTransactionType.ORDER
                            )

                            .reason(
                                    "Stock deducted for order "
                                            + savedOrder
                                            .getOrderNumber()
                            )

                            .referenceNumber(
                                    savedOrder
                                            .getOrderNumber()
                            )

                            .build();


            inventoryTransactionRepository.save(
                    transaction
            );
        }


        // =====================================================
        // CLEAR CART
        // =====================================================

        cart.getItems().clear();

        cartRepository.save(cart);


        // =====================================================
        // NOTIFICATION - ORDER PLACED
        // =====================================================

        createOrderNotification(
                savedOrder,
                NotificationType.ORDER_PLACED,
                "Order Placed Successfully",
                "Your order "
                        + savedOrder.getOrderNumber()
                        + " has been placed successfully."
        );


        // =====================================================
        // NOTIFICATION - ORDER CONFIRMED
        // =====================================================

        createOrderNotification(
                savedOrder,
                NotificationType.ORDER_CONFIRMED,
                "Order Confirmed",
                "Your order "
                        + savedOrder.getOrderNumber()
                        + " has been confirmed."
        );


        return savedOrder;
    }


    // =========================================================
    // CUSTOMER - SINGLE ORDER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Order getCustomerOrder(
            String email,
            Long orderId
    ) {

        User user =
                getCustomer(email);


        return orderRepository
                .findByIdAndUserId(
                        orderId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
                        )
                );
    }


    // =========================================================
    // CUSTOMER - ORDERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getCustomerOrders(
            String email,
            Pageable pageable
    ) {

        User user =
                getCustomer(email);


        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }


    // =========================================================
    // ADMIN - ALL ORDERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(
            Pageable pageable
    ) {

        return orderRepository
                .findAll(pageable);
    }


    // =========================================================
    // ADMIN - ORDERS BY STATUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(
            OrderStatus status,
            Pageable pageable
    ) {

        return orderRepository
                .findByOrderStatus(
                        status,
                        pageable
                );
    }


    // =========================================================
    // ADMIN - ORDER BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(
            Long orderId
    ) {

        return orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
                        )
                );
    }


    // =========================================================
    // ADMIN - UPDATE ORDER STATUS
    // =========================================================

    @Override
    public void updateOrderStatus(
            Long orderId,
            OrderStatus newStatus
    ) {

        if (newStatus == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == newStatus) {
            return;
        }

        if (!isValidOrderStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: "
                            + currentStatus + " → " + newStatus
            );
        }

        // Cancellation before shipment returns the reserved stock.
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStockForCancelledOrder(order);
        }

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED
                && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.SUCCESS);
        }

        if (newStatus == OrderStatus.CANCELLED
                && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        createStatusNotification(order, newStatus);
    }

    private boolean isValidOrderStatusTransition(
            OrderStatus current,
            OrderStatus next
    ) {
        if (current == null || next == null) {
            return false;
        }

        return switch (current) {
            case PENDING ->
                    next == OrderStatus.CONFIRMED
                            || next == OrderStatus.CANCELLED;
            case CONFIRMED ->
                    next == OrderStatus.PROCESSING
                            || next == OrderStatus.CANCELLED;
            case PROCESSING ->
                    next == OrderStatus.PACKED
                            || next == OrderStatus.CANCELLED;
            case PACKED ->
                    next == OrderStatus.SHIPPED
                            || next == OrderStatus.CANCELLED;
            case SHIPPED ->
                    next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY ->
                    next == OrderStatus.DELIVERED;
            // Return/refund statuses are controlled by their dedicated workflows.
            case DELIVERED, RETURN_REQUESTED, RETURNED, REFUND_REQUESTED,
                 CANCELLED, REFUNDED -> false;
        };
    }

    private void restoreStockForCancelledOrder(Order order) {
        if (order.getItems() == null) {
            return;
        }

        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            if (product == null) {
                continue;
            }

            int previousStock = product.getStockQuantity() == null
                    ? 0
                    : product.getStockQuantity();
            int quantity = orderItem.getQuantity() == null
                    ? 0
                    : orderItem.getQuantity();

            if (quantity <= 0) {
                continue;
            }

            int newStock = previousStock + quantity;
            product.setStockQuantity(newStock);

            if (newStock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
            }

            productRepository.save(product);

            inventoryTransactionRepository.save(
                    InventoryTransaction.builder()
                            .product(product)
                            .quantity(quantity)
                            .previousStock(previousStock)
                            .newStock(newStock)
                            .type(InventoryTransactionType.CANCELLED_ORDER)
                            .reason("Stock restored after order cancellation " + order.getOrderNumber())
                            .referenceNumber(order.getOrderNumber())
                            .build()
            );
        }
    }

    // =========================================================
    // CUSTOMER - CANCEL ORDER
    // =========================================================

    @Override
    public void cancelCustomerOrder(
            String email,
            Long orderId
    ) {

        User user =
                getCustomer(email);


        Order order =
                orderRepository
                        .findByIdAndUserId(
                                orderId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found"
                                )
                        );


        OrderStatus currentStatus =
                order.getOrderStatus();


        // =====================================================
        // VALIDATE CANCELLATION
        // =====================================================

        if (!isCustomerCancellable(currentStatus)) {

            throw new IllegalArgumentException(
                    "This order can no longer be cancelled"
            );
        }


        // =====================================================
        // RESTORE STOCK
        // =====================================================

        if (order.getItems() != null) {

            for (OrderItem orderItem :
                    order.getItems()) {

                Product product =
                        orderItem.getProduct();


                if (product != null) {

                    int previousStock =
                            product.getStockQuantity() == null
                                    ? 0
                                    : product.getStockQuantity();


                    int quantity =
                            orderItem.getQuantity();


                    int newStock =
                            previousStock + quantity;


                    product.setStockQuantity(
                            newStock
                    );


                    if (newStock > 0
                            && product.getStatus()
                            == ProductStatus.OUT_OF_STOCK) {

                        product.setStatus(
                                ProductStatus.ACTIVE
                        );
                    }


                    productRepository.save(product);


                    InventoryTransaction transaction =
                            InventoryTransaction.builder()
                                    .product(product)

                                    .quantity(quantity)

                                    .previousStock(
                                            previousStock
                                    )

                                    .newStock(
                                            newStock
                                    )

                                    .type(
                                            InventoryTransactionType
                                                    .CANCELLED_ORDER
                                    )

                                    .reason(
                                            "Stock restored after order cancellation "
                                                    + order
                                                    .getOrderNumber()
                                    )

                                    .referenceNumber(
                                            order
                                                    .getOrderNumber()
                                    )

                                    .build();


                    inventoryTransactionRepository.save(
                            transaction
                    );
                }
            }
        }


        // =====================================================
        // CANCEL ORDER
        // =====================================================

        order.setOrderStatus(
                OrderStatus.CANCELLED
        );


        // =====================================================
        // COD PAYMENT
        // =====================================================

        if (order.getPaymentMethod()
                == PaymentMethod.COD) {

            order.setPaymentStatus(
                    PaymentStatus.PENDING
            );
        }


        order.setUpdatedAt(
                LocalDateTime.now()
        );


        orderRepository.save(order);


        // =====================================================
        // CANCELLATION NOTIFICATION
        // =====================================================

        createOrderNotification(
                order,
                NotificationType.ORDER_CANCELLED,
                "Order Cancelled",
                "Your order "
                        + order.getOrderNumber()
                        + " has been cancelled successfully."
        );
    }


    // =========================================================
    // ORDER STATUS NOTIFICATION
    // =========================================================

    private void createStatusNotification(
            Order order,
            OrderStatus status
    ) {

        if (order == null
                || order.getUser() == null
                || status == null) {

            return;
        }


        NotificationType type;

        String title;

        String message;


        switch (status) {

            case CONFIRMED:

                type =
                        NotificationType.ORDER_CONFIRMED;

                title =
                        "Order Confirmed";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " has been confirmed.";

                break;


            case PROCESSING:

                type =
                        NotificationType.ORDER_PROCESSING;

                title =
                        "Order Processing";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " is now being processed.";

                break;


            case PACKED:

                type =
                        NotificationType.ORDER_PACKED;

                title =
                        "Order Packed";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " has been packed.";

                break;


            case SHIPPED:

                type =
                        NotificationType.ORDER_SHIPPED;

                title =
                        "Order Shipped";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " has been shipped.";

                break;


            case OUT_FOR_DELIVERY:

                type =
                        NotificationType.ORDER_OUT_FOR_DELIVERY;

                title =
                        "Out for Delivery";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " is out for delivery.";

                break;


            case DELIVERED:

                type =
                        NotificationType.ORDER_DELIVERED;

                title =
                        "Order Delivered";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " has been delivered successfully.";

                break;


            case CANCELLED:

                type =
                        NotificationType.ORDER_CANCELLED;

                title =
                        "Order Cancelled";

                message =
                        "Your order "
                                + order.getOrderNumber()
                                + " has been cancelled.";

                break;


            default:

                return;
        }


        createOrderNotification(
                order,
                type,
                title,
                message
        );
    }


    // =========================================================
    // CREATE ORDER NOTIFICATION
    // =========================================================

    private void createOrderNotification(
            Order order,
            NotificationType type,
            String title,
            String message
    ) {

        if (order == null
                || order.getUser() == null
                || order.getUser().getId() == null) {

            return;
        }


        String referenceKey =
                "ORDER:"
                        + order.getId()
                        + ":"
                        + type.name();


        notificationService
                .createNotificationIfNotExists(
                        order.getUser().getId(),
                        type,
                        title,
                        message,
                        "/customer/orders/"
                                + order.getId(),
                        order.getId(),
                        referenceKey
                );
    }


    // =========================================================
    // CUSTOMER CANCELLATION VALIDATION
    // =========================================================

    private boolean isCustomerCancellable(
            OrderStatus status
    ) {

        if (status == null) {

            return false;
        }


        return status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING
                || status == OrderStatus.PACKED;
    }


    // =========================================================
    // CUSTOMER VALIDATION
    // =========================================================

    private User getCustomer(
            String email
    ) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "User email is required"
            );
        }


        User user =
                userRepository
                        .findByEmail(
                                email.trim()
                                        .toLowerCase()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );


        if (user.getRole() == null
                || !user.getRole().name()
                .equals("CUSTOMER")) {

            throw new IllegalArgumentException(
                    "Customer account required"
            );
        }


        if (!user.isEnabled()) {

            throw new IllegalArgumentException(
                    "Customer account is disabled"
            );
        }


        return user;
    }


    // =========================================================
    // GENERATE ORDER NUMBER
    // =========================================================

    private String generateOrderNumber() {

        String orderNumber;


        do {

            orderNumber =
                    "DZ"
                            + System.currentTimeMillis()
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 4)
                            .toUpperCase();

        } while (
                orderRepository.existsByOrderNumber(
                        orderNumber
                )
        );


        return orderNumber;
    }
}