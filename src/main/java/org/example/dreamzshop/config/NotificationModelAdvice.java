package org.example.dreamzshop.config;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class NotificationModelAdvice {

    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @ModelAttribute
    public void addNotificationData(
            Authentication authentication,
            org.springframework.ui.Model model
    ) {

        /*
         * Default value.
         *
         * This prevents Thymeleaf errors on pages where
         * the user is not logged in or is not a customer.
         */
        model.addAttribute(
                "unreadCount",
                0L
        );


        /*
         * No authenticated user.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return;
        }


        /*
         * Ignore anonymous authentication.
         */
        if ("anonymousUser".equals(
                authentication.getName()
        )) {

            return;
        }


        try {

            String email =
                    authentication.getName();

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);


            /*
             * Notification badge is only for customers.
             */
            if (user == null
                    || user.getRole() != Role.CUSTOMER
                    || !user.isEnabled()) {

                return;
            }


            /*
             * Get current unread notification count.
             */
            long unreadCount =
                    notificationService
                            .getUnreadCount(email);


            model.addAttribute(
                    "unreadCount",
                    unreadCount
            );

        } catch (Exception ignored) {

            /*
             * Notification loading must never break
             * the customer's main page.
             *
             * If notification retrieval fails,
             * simply keep unreadCount = 0.
             */

            model.addAttribute(
                    "unreadCount",
                    0L
            );
        }
    }
}