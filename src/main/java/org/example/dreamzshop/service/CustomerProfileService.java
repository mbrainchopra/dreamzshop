package org.example.dreamzshop.service;

import org.example.dreamzshop.dto.ChangePasswordRequest;
import org.example.dreamzshop.dto.CustomerProfileRequest;
import org.example.dreamzshop.entity.User;

public interface CustomerProfileService {

    User getCustomerProfile(String email);

    void updateProfile(
            String currentEmail,
            CustomerProfileRequest request
    );

    void changePassword(
            String email,
            ChangePasswordRequest request
    );
}