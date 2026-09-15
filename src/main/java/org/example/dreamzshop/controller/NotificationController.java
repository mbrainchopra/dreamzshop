package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Notification;
import org.example.dreamzshop.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // =========================================================
    // ALL NOTIFICATIONS
    // =========================================================

    @GetMapping
    public String notifications(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model) {

        if (page < 0) {
            page = 0;
        }

        String email =
                authentication.getName();

        PageRequest pageable =
                PageRequest.of(
                        page,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Notification> notifications =
                notificationService
                        .getCustomerNotifications(
                                email,
                                pageable
                        );

        model.addAttribute(
                "notifications",
                notifications
        );

        return "customer/notifications";
    }

    // =========================================================
    // MARK AS READ
    // =========================================================

    @PostMapping("/{id}/read")
    public String markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        notificationService.markAsRead(
                authentication.getName(),
                id
        );

        return "redirect:/customer/notifications";
    }

    // =========================================================
    // MARK AS UNREAD
    // =========================================================

    @PostMapping("/{id}/unread")
    public String markAsUnread(
            @PathVariable Long id,
            Authentication authentication) {

        notificationService.markAsUnread(
                authentication.getName(),
                id
        );

        return "redirect:/customer/notifications";
    }

    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @PostMapping("/read-all")
    public String markAllAsRead(
            Authentication authentication) {

        notificationService.markAllAsRead(
                authentication.getName()
        );

        return "redirect:/customer/notifications";
    }

    // =========================================================
    // DELETE ONE
    // =========================================================

    @PostMapping("/{id}/delete")
    public String deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {

        notificationService.deleteNotification(
                authentication.getName(),
                id
        );

        return "redirect:/customer/notifications";
    }

    // =========================================================
    // DELETE READ
    // =========================================================

    @PostMapping("/delete-read")
    public String deleteReadNotifications(
            Authentication authentication) {

        notificationService.deleteReadNotifications(
                authentication.getName()
        );

        return "redirect:/customer/notifications";
    }

    // =========================================================
    // DELETE ALL
    // =========================================================

    @PostMapping("/delete-all")
    public String deleteAllNotifications(
            Authentication authentication) {

        notificationService.deleteAllNotifications(
                authentication.getName()
        );

        return "redirect:/customer/notifications";
    }
}