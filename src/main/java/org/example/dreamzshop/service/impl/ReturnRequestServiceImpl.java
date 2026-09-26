package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.OrderItem;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.InventoryTransaction;
import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.NotificationType;
import org.example.dreamzshop.enums.InventoryTransactionType;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.repository.OrderItemRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.InventoryTransactionRepository;
import org.example.dreamzshop.repository.ReturnRequestRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.NotificationService;
import org.example.dreamzshop.service.ReturnRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReturnRequestServiceImpl
        implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    private final InventoryTransactionRepository inventoryTransactionRepository;

    private final NotificationService notificationService;


    // =========================================================
    // CUSTOMER - CREATE RETURN REQUEST
    // =========================================================

    @Override
    public ReturnRequest createReturnRequest(
            String email,
            Long orderId,
            Long orderItemId,
            String reason,
            String description
    ) {

        User user = getCustomer(email);


        if (orderId == null) {

            throw new IllegalArgumentException(
                    "Order ID is required"
            );
        }


        if (orderItemId == null) {

            throw new IllegalArgumentException(
                    "Order item ID is required"
            );
        }


        if (reason == null
                || reason.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Return reason is required"
            );
        }


        reason = reason.trim();


        if (reason.length() > 500) {

            throw new IllegalArgumentException(
                    "Return reason cannot exceed 500 characters"
            );
        }


        if (description != null) {

            description = description.trim();


            if (description.length() > 1000) {

                throw new IllegalArgumentException(
                        "Return description cannot exceed 1000 characters"
                );
            }
        }


        // =====================================================
        // VERIFY ORDER OWNERSHIP
        // =====================================================

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


        // =====================================================
        // VERIFY ORDER ITEM
        // =====================================================

        OrderItem orderItem =
                orderItemRepository
                        .findById(orderItemId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order item not found"
                                )
                        );

        if (orderItem.getOrder() == null
                || !orderItem.getOrder().getId().equals(order.getId())) {

            throw new IllegalArgumentException(
                    "Invalid order item for this order"
            );
        }


        // =====================================================
        // ONLY DELIVERED ORDERS
        // =====================================================

        if (order.getOrderStatus()
                != OrderStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Only delivered orders can be returned"
            );
        }


        // =====================================================
        // ONE RETURN REQUEST PER ORDER ITEM
        // =====================================================

        if (returnRequestRepository
                .existsByOrderItemId(orderItemId)) {

            throw new IllegalArgumentException(
                    "A return request already exists for this product"
            );
        }


        // =====================================================
        // CREATE RETURN REQUEST
        // =====================================================

        ReturnRequest returnRequest =
                ReturnRequest.builder()
                        .user(user)
                        .order(order)
                        .orderItem(orderItem)
                        .reason(reason)
                        .description(description)
                        .status(
                                ReturnRequestStatus.REQUESTED
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .updatedAt(
                                LocalDateTime.now()
                        )
                        .build();


        ReturnRequest savedReturnRequest =
                returnRequestRepository.save(
                        returnRequest
                );


        // =====================================================
        // UPDATE ORDER STATUS
        // =====================================================

        order.setOrderStatus(
                OrderStatus.RETURN_REQUESTED
        );


        order.setUpdatedAt(
                LocalDateTime.now()
        );


        orderRepository.save(order);


        // =====================================================
        // NOTIFICATION
        // =====================================================

        createReturnNotification(
                savedReturnRequest,
                NotificationType.RETURN_REQUESTED,
                "Return Request Submitted",
                "Your return request for order "
                        + getOrderNumber(order)
                        + " has been submitted successfully."
        );


        return savedReturnRequest;
    }


    // =========================================================
    // CUSTOMER - SINGLE RETURN REQUEST
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ReturnRequest getCustomerReturnRequest(
            String email,
            Long returnRequestId
    ) {

        User user = getCustomer(email);


        if (returnRequestId == null) {

            throw new IllegalArgumentException(
                    "Return request ID is required"
            );
        }


        return returnRequestRepository
                .findByIdAndUserId(
                        returnRequestId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Return request not found"
                        )
                );
    }


    // =========================================================
    // CUSTOMER - ALL RETURN REQUESTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequest> getCustomerReturnRequests(
            String email,
            Pageable pageable
    ) {

        User user = getCustomer(email);


        return returnRequestRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }


    // =========================================================
    // ADMIN - ALL RETURN REQUESTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequest> getAllReturnRequests(
            Pageable pageable
    ) {

        return returnRequestRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }


    // =========================================================
    // ADMIN - RETURN REQUESTS BY STATUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequest> getReturnRequestsByStatus(
            ReturnRequestStatus status,
            Pageable pageable
    ) {

        if (status == null) {

            return getAllReturnRequests(pageable);
        }


        return returnRequestRepository
                .findByStatusOrderByCreatedAtDesc(
                        status,
                        pageable
                );
    }


    // =========================================================
    // ADMIN - SINGLE RETURN REQUEST
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ReturnRequest getReturnRequest(
            Long returnRequestId
    ) {

        if (returnRequestId == null) {

            throw new IllegalArgumentException(
                    "Return request ID is required"
            );
        }


        return returnRequestRepository
                .findById(returnRequestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Return request not found"
                        )
                );
    }


    // =========================================================
    // ADMIN - UPDATE RETURN STATUS
    // =========================================================

    @Override
    public void updateReturnRequestStatus(
            Long returnRequestId,
            ReturnRequestStatus newStatus,
            String adminRemarks
    ) {

        if (returnRequestId == null) {

            throw new IllegalArgumentException(
                    "Return request ID is required"
            );
        }


        if (newStatus == null) {

            throw new IllegalArgumentException(
                    "Return status is required"
            );
        }


        ReturnRequest returnRequest =
                returnRequestRepository
                        .findById(returnRequestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Return request not found"
                                )
                        );


        ReturnRequestStatus currentStatus =
                returnRequest.getStatus();


        // =====================================================
        // VALIDATE STATUS TRANSITION
        // =====================================================

        if (!isValidStatusTransition(
                currentStatus,
                newStatus
        )) {

            throw new IllegalArgumentException(
                    "Invalid return status transition: "
                            + currentStatus
                            + " → "
                            + newStatus
            );
        }


        if (adminRemarks != null) {

            adminRemarks = adminRemarks.trim();


            if (adminRemarks.length() > 1000) {

                throw new IllegalArgumentException(
                        "Admin remarks cannot exceed 1000 characters"
                );
            }
        }


        returnRequest.setStatus(
                newStatus
        );


        if (adminRemarks != null
                && !adminRemarks.isEmpty()) {

            returnRequest.setAdminRemarks(
                    adminRemarks
            );
        }


        returnRequest.setUpdatedAt(
                LocalDateTime.now()
        );


        returnRequestRepository.save(
                returnRequest
        );


        // =====================================================
        // SYNC ORIGINAL ORDER STATUS
        // =====================================================

        Order order =
                returnRequest.getOrder();


        if (order != null) {

            switch (newStatus) {

                case REQUESTED:

                    order.setOrderStatus(
                            OrderStatus.RETURN_REQUESTED
                    );

                    break;


                case APPROVED:

                case PICKUP_SCHEDULED:

                case PICKED_UP:

                    // The item is still physically with the customer.
                    order.setOrderStatus(
                            OrderStatus.RETURN_REQUESTED
                    );

                    break;

                case RECEIVED:

                    // The returned goods have reached the seller.
                    order.setOrderStatus(
                            OrderStatus.RETURNED
                    );

                    restoreReturnedItemStock(returnRequest.getOrderItem());

                    break;


                case REJECTED:

                    /*
                     * Rejected return means the
                     * original order remains delivered.
                     */

                    order.setOrderStatus(
                            OrderStatus.DELIVERED
                    );

                    break;


                case REFUND_INITIATED:

                    order.setOrderStatus(
                            OrderStatus.REFUND_REQUESTED
                    );

                    break;


                case COMPLETED:

                    order.setOrderStatus(
                            OrderStatus.REFUNDED
                    );

                    break;


                case CANCELLED:

                    /*
                     * Cancelled return means the
                     * original order remains delivered.
                     */

                    order.setOrderStatus(
                            OrderStatus.DELIVERED
                    );

                    break;


                default:

                    break;
            }


            order.setUpdatedAt(
                    LocalDateTime.now()
            );


            orderRepository.save(order);
        }


        // =====================================================
        // RETURN STATUS NOTIFICATION
        // =====================================================

        createReturnStatusNotification(
                returnRequest,
                newStatus
        );
    }


    // =========================================================
    // CUSTOMER - CANCEL RETURN REQUEST
    // =========================================================

    @Override
    public void cancelReturnRequest(
            String email,
            Long returnRequestId
    ) {

        User user = getCustomer(email);


        if (returnRequestId == null) {

            throw new IllegalArgumentException(
                    "Return request ID is required"
            );
        }


        ReturnRequest returnRequest =
                returnRequestRepository
                        .findByIdAndUserId(
                                returnRequestId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Return request not found"
                                )
                        );


        ReturnRequestStatus currentStatus =
                returnRequest.getStatus();


        // =====================================================
        // ONLY REQUESTED CAN BE CANCELLED
        // =====================================================

        if (currentStatus
                != ReturnRequestStatus.REQUESTED) {

            throw new IllegalArgumentException(
                    "This return request can no longer be cancelled"
            );
        }


        returnRequest.setStatus(
                ReturnRequestStatus.CANCELLED
        );


        returnRequest.setUpdatedAt(
                LocalDateTime.now()
        );


        returnRequestRepository.save(
                returnRequest
        );


        // =====================================================
        // RESTORE ORDER STATUS
        // =====================================================

        Order order =
                returnRequest.getOrder();


        if (order != null) {

            order.setOrderStatus(
                    OrderStatus.DELIVERED
            );


            order.setUpdatedAt(
                    LocalDateTime.now()
            );


            orderRepository.save(order);
        }
    }


    /**
     * Only the product actually returned is restored to inventory.
     */
    private void restoreReturnedItemStock(OrderItem orderItem) {

        if (orderItem == null) {
            return;
        }

        Product product = orderItem.getProduct();
        int quantity = orderItem.getQuantity() == null
                ? 0
                : orderItem.getQuantity();

        if (product == null || quantity <= 0) {
            return;
        }

        int previousStock = product.getStockQuantity() == null
                ? 0
                : product.getStockQuantity();

        int newStock = previousStock + quantity;

        product.setStockQuantity(newStock);

        if (product.getStatus() == ProductStatus.OUT_OF_STOCK
                && newStock > 0) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .product(product)
                        .quantity(quantity)
                        .previousStock(previousStock)
                        .newStock(newStock)
                        .type(InventoryTransactionType.RETURN)
                        .reason(
                                "Stock restored after return received "
                                        + getOrderNumber(orderItem.getOrder())
                        )
                        .referenceNumber(
                                getOrderNumber(orderItem.getOrder())
                        )
                        .build()
        );
    }

    // =========================================================
    // RETURN STATUS NOTIFICATION
    // =========================================================

    private void createReturnStatusNotification(
            ReturnRequest returnRequest,
            ReturnRequestStatus status
    ) {

        if (returnRequest == null
                || returnRequest.getUser() == null
                || returnRequest.getUser().getId() == null
                || status == null) {

            return;
        }


        NotificationType type;

        String title;

        String message;


        String orderNumber =
                getOrderNumber(
                        returnRequest.getOrder()
                );


        switch (status) {

            case REQUESTED:

                type =
                        NotificationType.RETURN_REQUESTED;

                title =
                        "Return Request Submitted";

                message =
                        "Your return request for order "
                                + orderNumber
                                + " has been submitted.";

                break;


            case APPROVED:

                type =
                        NotificationType.RETURN_APPROVED;

                title =
                        "Return Request Approved";

                message =
                        "Your return request for order "
                                + orderNumber
                                + " has been approved.";

                break;


            case REJECTED:

                type =
                        NotificationType.RETURN_REJECTED;

                title =
                        "Return Request Rejected";

                message =
                        "Your return request for order "
                                + orderNumber
                                + " has been rejected.";

                break;


            case PICKUP_SCHEDULED:

                type =
                        NotificationType.RETURN_PICKUP_SCHEDULED;

                title =
                        "Return Pickup Scheduled";

                message =
                        "Pickup for your return request on order "
                                + orderNumber
                                + " has been scheduled.";

                break;


            case PICKED_UP:

                type =
                        NotificationType.RETURN_PICKED_UP;

                title =
                        "Return Picked Up";

                message =
                        "The returned item from order "
                                + orderNumber
                                + " has been picked up.";

                break;


            case RECEIVED:

                type =
                        NotificationType.RETURN_RECEIVED;

                title =
                        "Return Received";

                message =
                        "Your returned item for order "
                                + orderNumber
                                + " has been received.";

                break;


            case REFUND_INITIATED:

                type =
                        NotificationType.REFUND_INITIATED;

                title =
                        "Refund Initiated";

                message =
                        "Your refund for order "
                                + orderNumber
                                + " has been initiated.";

                break;


            case COMPLETED:

                type =
                        NotificationType.REFUND_COMPLETED;

                title =
                        "Refund Completed";

                message =
                        "Your refund for order "
                                + orderNumber
                                + " has been completed successfully.";

                break;


            case CANCELLED:

                /*
                 * NotificationType does not contain
                 * RETURN_CANCELLED.
                 *
                 * Therefore no dedicated notification
                 * is created for cancellation.
                 */

                return;


            default:

                return;
        }


        createReturnNotification(
                returnRequest,
                type,
                title,
                message
        );
    }


    // =========================================================
    // CREATE RETURN NOTIFICATION
    // =========================================================

    private void createReturnNotification(
            ReturnRequest returnRequest,
            NotificationType type,
            String title,
            String message
    ) {

        if (returnRequest == null
                || returnRequest.getUser() == null
                || returnRequest.getUser().getId() == null) {

            return;
        }


        Long userId =
                returnRequest
                        .getUser()
                        .getId();


        Long returnRequestId =
                returnRequest.getId();


        if (returnRequestId == null) {

            return;
        }


        String referenceKey =
                "RETURN:"
                        + returnRequestId
                        + ":"
                        + type.name();


        notificationService
                .createNotificationIfNotExists(
                        userId,
                        type,
                        title,
                        message,
                        "/customer/returns/"
                                + returnRequestId,
                        returnRequestId,
                        referenceKey
                );
    }


    // =========================================================
    // ORDER NUMBER HELPER
    // =========================================================

    private String getOrderNumber(
            Order order
    ) {

        if (order == null) {

            return "your order";
        }


        if (order.getOrderNumber() == null
                || order.getOrderNumber()
                .trim()
                .isEmpty()) {

            return "your order";
        }


        return order
                .getOrderNumber()
                .trim();
    }


    // =========================================================
    // VALID RETURN STATUS TRANSITION
    // =========================================================

    private boolean isValidStatusTransition(
            ReturnRequestStatus currentStatus,
            ReturnRequestStatus newStatus
    ) {

        if (currentStatus == null
                || newStatus == null) {

            return false;
        }


        if (currentStatus == newStatus) {

            return true;
        }


        switch (currentStatus) {

            case REQUESTED:

                return newStatus
                        == ReturnRequestStatus.APPROVED

                        || newStatus
                        == ReturnRequestStatus.REJECTED

                        || newStatus
                        == ReturnRequestStatus.CANCELLED;


            case APPROVED:

                return newStatus
                        == ReturnRequestStatus.PICKUP_SCHEDULED

                        || newStatus
                        == ReturnRequestStatus.CANCELLED;


            case PICKUP_SCHEDULED:

                return newStatus
                        == ReturnRequestStatus.PICKED_UP;


            case PICKED_UP:

                return newStatus
                        == ReturnRequestStatus.RECEIVED;


            case RECEIVED:

                return newStatus
                        == ReturnRequestStatus.REFUND_INITIATED;


            case REFUND_INITIATED:

                return newStatus
                        == ReturnRequestStatus.COMPLETED;


            case REJECTED:

            case COMPLETED:

            case CANCELLED:

                return false;


            default:

                return false;
        }
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
                || !"CUSTOMER".equals(
                user.getRole().name()
        )) {

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
}