package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import org.example.dreamzshop.entity.Address;
import org.example.dreamzshop.enums.AddressType;
import org.example.dreamzshop.service.AddressService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(
            AddressService addressService
    ) {
        this.addressService = addressService;
    }

    @GetMapping
    public String addresses(
            Authentication authentication,
            Model model
    ) {

        model.addAttribute(
                "addresses",
                addressService.getCustomerAddresses(
                        authentication.getName()
                )
        );

        return "customer/addresses";
    }

    @GetMapping("/add")
    public String addAddress(
            Model model
    ) {

        Address address = new Address();

        address.setAddressType(
                AddressType.HOME
        );

        model.addAttribute(
                "address",
                address
        );

        model.addAttribute(
                "addressTypes",
                AddressType.values()
        );

        return "customer/address-form";
    }

    @GetMapping("/edit/{id}")
    public String editAddress(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {

        try {

            Address address =
                    addressService.getAddress(
                            authentication.getName(),
                            id
                    );

            model.addAttribute(
                    "address",
                    address
            );

            model.addAttribute(
                    "addressTypes",
                    AddressType.values()
            );

            return "customer/address-form";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/addresses?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/save")
    public String saveAddress(
            @Valid @ModelAttribute("address") Address address,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "addressTypes",
                    AddressType.values()
            );

            return "customer/address-form";
        }

        try {

            addressService.saveAddress(
                    authentication.getName(),
                    address
            );

            return "redirect:/customer/addresses?saved=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "addressTypes",
                    AddressType.values()
            );

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "customer/address-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            addressService.deleteAddress(
                    authentication.getName(),
                    id
            );

            return "redirect:/customer/addresses?deleted=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/addresses?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/default/{id}")
    public String setDefaultAddress(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            addressService.setDefaultAddress(
                    authentication.getName(),
                    id
            );

            return "redirect:/customer/addresses?default=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/addresses?error="
                    + encode(e.getMessage());
        }
    }

    private String encode(
            String message
    ) {

        if (message == null) {
            return "Something went wrong";
        }

        return message
                .replace(" ", "%20")
                .replace("?", "%3F")
                .replace("&", "%26");
    }
}