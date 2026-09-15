package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnRequestService {

    ReturnRequest createReturnRequest(
            String email,
            Long orderId,
            String reason,
            String description
    );

    ReturnRequest getCustomerReturnRequest(
            String email,
            Long returnRequestId
    );

    Page<ReturnRequest> getCustomerReturnRequests(
            String email,
            Pageable pageable
    );

    Page<ReturnRequest> getAllReturnRequests(
            Pageable pageable
    );

    Page<ReturnRequest> getReturnRequestsByStatus(
            ReturnRequestStatus status,
            Pageable pageable
    );

    ReturnRequest getReturnRequest(
            Long returnRequestId
    );

    void updateReturnRequestStatus(
            Long returnRequestId,
            ReturnRequestStatus newStatus,
            String adminRemarks
    );

    void cancelReturnRequest(
            String email,
            Long returnRequestId
    );
}