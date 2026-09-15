package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    Optional<User> findByEmail(
            String email
    );

    boolean existsByEmail(
            String email
    );

    boolean existsByPhone(
            String phone
    );


    // =========================================================
    // ROLE
    // =========================================================

    Page<User> findByRole(
            Role role,
            Pageable pageable
    );


    // =========================================================
    // CUSTOMER SEARCH
    // =========================================================

    Page<User> findByRoleAndFullNameContainingIgnoreCase(
            Role role,
            String fullName,
            Pageable pageable
    );

    Page<User> findByRoleAndEmailContainingIgnoreCase(
            Role role,
            String email,
            Pageable pageable
    );

    Page<User> findByRoleAndPhoneContaining(
            Role role,
            String phone,
            Pageable pageable
    );


    // =========================================================
    // COUNTS
    // =========================================================

    long countByRole(
            Role role
    );

    long countByRoleAndEnabledTrue(
            Role role
    );

    long countByRoleAndEnabledFalse(
            Role role
    );


    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    Page<User> findTop5ByRoleOrderByCreatedAtDesc(
            Role role,
            Pageable pageable
    );
}