package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.dto.RegisterRequest;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute("registerRequest", new RegisterRequest());

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid RegisterRequest registerRequest,
            BindingResult bindingResult,
            Model model) {

        // Validation errors
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Check password confirmation
        if (!registerRequest.getPassword()
                .equals(registerRequest.getConfirmPassword())) {

            model.addAttribute(
                    "passwordError",
                    "Passwords do not match"
            );

            return "auth/register";
        }

        // Check duplicate email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {

            model.addAttribute(
                    "emailError",
                    "Email address is already registered"
            );

            return "auth/register";
        }

        // Check duplicate phone
        if (userRepository.existsByPhone(registerRequest.getPhone())) {

            model.addAttribute(
                    "phoneError",
                    "Phone number is already registered"
            );

            return "auth/register";
        }

        // Create customer
        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .password(passwordEncoder.encode(
                        registerRequest.getPassword()
                ))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        userRepository.save(user);

        return "redirect:/login?registered=true";
    }
}