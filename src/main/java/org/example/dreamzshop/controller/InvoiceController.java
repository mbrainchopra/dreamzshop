package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.service.InvoiceService;
import org.example.dreamzshop.service.OrderService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class InvoiceController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    // =========================================================
    // CUSTOMER - DOWNLOAD INVOICE
    // =========================================================

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ByteArrayResource> downloadInvoice(
            @PathVariable Long orderId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(403)
                    .build();
        }

        String email = authentication.getName();

        Order order =
                orderService.getCustomerOrder(
                        email,
                        orderId
                );

        byte[] pdf =
                invoiceService.generateInvoice(order);

        ByteArrayResource resource =
                new ByteArrayResource(pdf);

        String fileName =
                "Dreamz-Shop-Invoice-"
                        + order.getOrderNumber()
                        + ".pdf";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(fileName)
                        .build()
        );

        headers.setContentLength(
                pdf.length
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(resource);
    }
}