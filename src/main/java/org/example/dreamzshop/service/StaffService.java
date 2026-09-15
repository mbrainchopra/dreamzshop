package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffService {

    Page<User> getAllStaff(String keyword, Pageable pageable);

    User getStaff(Long id);

    User createStaff(
            String fullName,
            String email,
            String phone,
            String password
    );

    User updateStaff(
            Long id,
            String fullName,
            String email,
            String phone,
            String password
    );

    void toggleStaff(Long id);
}