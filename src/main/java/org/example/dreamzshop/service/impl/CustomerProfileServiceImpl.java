package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.dto.ChangePasswordRequest;
import org.example.dreamzshop.dto.CustomerProfileRequest;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.CustomerProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerProfileServiceImpl
        implements CustomerProfileService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    public User getCustomerProfile(
            String email
    ) {

        return getCustomer(email);
    }


    @Override
    public void updateProfile(
            String currentEmail,
            CustomerProfileRequest request
    ) {

        User customer =
                getCustomer(currentEmail);


        String newEmail =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        String newPhone =
                request.getPhone()
                        .trim();


        /*
         * Check email uniqueness.
         */

        if (!customer.getEmail()
                .equalsIgnoreCase(newEmail)
                && userRepository.existsByEmail(newEmail)) {

            throw new IllegalArgumentException(
                    "Email address is already registered."
            );
        }


        /*
         * Check phone uniqueness.
         */

        if (!customer.getPhone()
                .equals(newPhone)
                && userRepository.existsByPhone(newPhone)) {

            throw new IllegalArgumentException(
                    "Phone number is already registered."
            );
        }


        customer.setFullName(
                request.getFullName().trim()
        );

        customer.setEmail(
                newEmail
        );

        customer.setPhone(
                newPhone
        );


        userRepository.save(
                customer
        );
    }


    @Override
    public void changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        User customer =
                getCustomer(email);


        /*
         * Check current password.
         */

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                customer.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }


        /*
         * Check new password confirmation.
         */

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "New password and confirm password do not match."
            );
        }


        /*
         * Prevent using the same password.
         */

        if (passwordEncoder.matches(
                request.getNewPassword(),
                customer.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from the current password."
            );
        }


        customer.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );


        userRepository.save(
                customer
        );
    }


    private User getCustomer(
            String email
    ) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Customer account not found."
                                )
                        );


        if (user.getRole() != Role.CUSTOMER) {

            throw new IllegalArgumentException(
                    "This profile is available only for customers."
            );
        }


        if (!user.isEnabled()) {

            throw new IllegalArgumentException(
                    "Your account is disabled."
            );
        }


        return user;
    }
}