package org.example.dreamzshop.config;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@dreamzshop.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    @Bean
    CommandLineRunner createDefaultAdmin() {

        return args -> {

            if (!userRepository.existsByEmail(adminEmail)) {

                User admin = User.builder()
                        .fullName("Dreamz Shop Admin")
                        .email(adminEmail)
                        .phone("9999999999")
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();

                userRepository.save(admin);

                System.out.println("=================================");
                System.out.println("DEFAULT ADMIN CREATED");
                System.out.println("Email    : admin@dreamzshop.com");
                System.out.println("Password : configured via ADMIN_PASSWORD/app.admin.password");
                System.out.println("=================================");
            }
        };
    }
}