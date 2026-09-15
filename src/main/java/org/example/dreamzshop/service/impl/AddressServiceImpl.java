package org.example.dreamzshop.service.impl;

import org.example.dreamzshop.entity.Address;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.repository.AddressRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            UserRepository userRepository
    ) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getCustomerAddresses(String email) {

        User user = getCustomer(email);

        return addressRepository
                .findByUserIdOrderByDefaultAddressDescCreatedAtDesc(
                        user.getId()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddress(
            String email,
            Long addressId
    ) {

        User user = getCustomer(email);

        return addressRepository
                .findByIdAndUserId(
                        addressId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"
                        )
                );
    }

    @Override
    public Address saveAddress(
            String email,
            Address address
    ) {

        User user = getCustomer(email);

        if (address == null) {
            throw new IllegalArgumentException(
                    "Address details are required"
            );
        }

        /*
         * New address
         */
        if (address.getId() == null) {

            address.setUser(user);

            /*
             * First address automatically becomes default.
             */
            long addressCount =
                    addressRepository.countByUserId(
                            user.getId()
                    );

            if (addressCount == 0) {
                address.setDefaultAddress(true);
            }

            if (address.isDefaultAddress()) {
                removeExistingDefault(user.getId());
            }

        } else {

            /*
             * Existing address must belong
             * to the logged-in customer.
             */
            Address existing =
                    addressRepository
                            .findByIdAndUserId(
                                    address.getId(),
                                    user.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Address not found"
                                    )
                            );

            address.setUser(user);

            if (address.isDefaultAddress()) {
                removeExistingDefault(
                        user.getId()
                );
            } else if (existing.isDefaultAddress()) {

                /*
                 * Do not allow the customer to end up
                 * without a default address when editing
                 * the current default.
                 */
                address.setDefaultAddress(true);
            }
        }

        address.setUpdatedAt(
                LocalDateTime.now()
        );

        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(
            String email,
            Long addressId
    ) {

        User user = getCustomer(email);

        Address address =
                addressRepository
                        .findByIdAndUserId(
                                addressId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Address not found"
                                )
                        );

        boolean wasDefault =
                address.isDefaultAddress();

        addressRepository.delete(address);

        /*
         * If the deleted address was default,
         * automatically make another address default.
         */
        if (wasDefault) {

            List<Address> remaining =
                    addressRepository
                            .findByUserIdOrderByDefaultAddressDescCreatedAtDesc(
                                    user.getId()
                            );

            if (!remaining.isEmpty()) {

                Address newDefault =
                        remaining.get(0);

                newDefault.setDefaultAddress(true);

                newDefault.setUpdatedAt(
                        LocalDateTime.now()
                );

                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public void setDefaultAddress(
            String email,
            Long addressId
    ) {

        User user = getCustomer(email);

        Address address =
                addressRepository
                        .findByIdAndUserId(
                                addressId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Address not found"
                                )
                        );

        removeExistingDefault(
                user.getId()
        );

        address.setDefaultAddress(true);

        address.setUpdatedAt(
                LocalDateTime.now()
        );

        addressRepository.save(address);
    }

    private void removeExistingDefault(
            Long userId
    ) {

        addressRepository
                .findByUserIdAndDefaultAddressTrue(
                        userId
                )
                .ifPresent(existingDefault -> {

                    existingDefault.setDefaultAddress(false);

                    existingDefault.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    addressRepository.save(
                            existingDefault
                    );
                });
    }

    private User getCustomer(
            String email
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"
                                )
                        );

        if (user.getRole() == null
                || !"CUSTOMER".equals(
                user.getRole().name()
        )) {

            throw new IllegalArgumentException(
                    "Only customers can manage addresses"
            );
        }

        if (!user.isEnabled()) {

            throw new IllegalArgumentException(
                    "Your account is disabled"
            );
        }

        return user;
    }
}