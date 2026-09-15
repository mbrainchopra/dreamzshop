package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository
        extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(
            Long userId
    );

    Optional<Address> findByIdAndUserId(
            Long id,
            Long userId
    );

    Optional<Address> findByUserIdAndDefaultAddressTrue(
            Long userId
    );

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}