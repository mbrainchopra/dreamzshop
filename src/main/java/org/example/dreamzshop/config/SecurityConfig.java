package org.example.dreamzshop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    private final CustomAuthenticationSuccessHandler
            customAuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // ==============================
                // USER DETAILS SERVICE
                // ==============================

                .userDetailsService(
                        customUserDetailsService
                )

                // ==============================
                // AUTHORIZATION
                // ==============================

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC PAGES
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/products",
                                "/products/**"
                        ).permitAll()

                        // STATIC RESOURCES
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ADMIN
                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")

                        // STAFF + ADMIN
                        .requestMatchers(
                                "/staff/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "STAFF"
                        )

                        // CUSTOMER
                        .requestMatchers(
                                "/customer/**"
                        ).hasRole("CUSTOMER")

                        // EVERYTHING ELSE
                        .anyRequest()
                        .authenticated()
                )

                // ==============================
                // LOGIN
                // ==============================

                .formLogin(form -> form

                        .loginPage("/login")

                        .loginProcessingUrl("/login")

                        .successHandler(
                                customAuthenticationSuccessHandler
                        )

                        .failureUrl(
                                "/login?error=true"
                        )

                        .permitAll()
                )

                // ==============================
                // LOGOUT
                // ==============================

                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl(
                                "/login?logout=true"
                        )

                        .invalidateHttpSession(true)

                        .clearAuthentication(true)

                        .deleteCookies(
                                "JSESSIONID"
                        )

                        .permitAll()
                )

                // ==============================
                // SESSION SECURITY
                // ==============================

                .sessionManagement(session -> session

                        .sessionFixation(
                                sessionFixation ->
                                        sessionFixation
                                                .migrateSession()
                        )

                        .maximumSessions(1)
                );

        return http.build();
    }
}