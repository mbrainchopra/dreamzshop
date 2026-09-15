package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Address;

import java.util.List;

public interface AddressService {

    List<Address> getCustomerAddresses(String email);

    Address getAddress(
            String email,
            Long addressId
    );

    Address saveAddress(
            String email,
            Address address
    );

    void deleteAddress(
            String email,
            Long addressId
    );

    void setDefaultAddress(
            String email,
            Long addressId
    );
}