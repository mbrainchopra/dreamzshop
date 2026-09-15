package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllStaff(String keyword, Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findByRole(
                    Role.STAFF,
                    pageable
            );
        }

        String search = keyword.trim();

        return userRepository
                .findByRoleAndFullNameContainingIgnoreCase(
                        Role.STAFF,
                        search,
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public User getStaff(Long id) {

        User staff = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Staff member not found"
                        )
                );

        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException(
                    "The selected user is not a staff member"
            );
        }

        return staff;
    }

    @Override
    public User createStaff(
            String fullName,
            String email,
            String phone,
            String password
    ) {

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Email address is already registered"
            );
        }

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new IllegalArgumentException(
                    "Phone number is already registered"
            );
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Password is required"
            );
        }

        User staff = User.builder()
                .fullName(fullName.trim())
                .email(normalizedEmail)
                .phone(normalizedPhone)
                .password(passwordEncoder.encode(password))
                .role(Role.STAFF)
                .enabled(true)
                .build();

        return userRepository.save(staff);
    }

    @Override
    public User updateStaff(
            Long id,
            String fullName,
            String email,
            String phone,
            String password
    ) {

        User staff = getStaff(id);

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim();

        if (!staff.getEmail().equalsIgnoreCase(normalizedEmail)) {

            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new IllegalArgumentException(
                        "Email address is already registered"
                );
            }

            staff.setEmail(normalizedEmail);
        }

        if (!staff.getPhone().equals(normalizedPhone)) {

            if (userRepository.existsByPhone(normalizedPhone)) {
                throw new IllegalArgumentException(
                        "Phone number is already registered"
                );
            }

            staff.setPhone(normalizedPhone);
        }

        staff.setFullName(fullName.trim());

        if (password != null && !password.trim().isEmpty()) {
            staff.setPassword(
                    passwordEncoder.encode(password)
            );
        }

        staff.setRole(Role.STAFF);

        return userRepository.save(staff);
    }

    @Override
    public void toggleStaff(Long id) {

        User staff = getStaff(id);

        staff.setEnabled(!staff.isEnabled());

        userRepository.save(staff);
    }
}