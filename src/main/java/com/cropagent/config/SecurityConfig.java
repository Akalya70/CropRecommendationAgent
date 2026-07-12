package com.cropagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

            // Disable CSRF for REST APIs
            .csrf(csrf -> csrf.disable())

            // Session Management
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth

                    // Public Pages
                    .requestMatchers(
                            "/",
                            "/index.html",
                            "/login.html",
                            "/register.html",
                            "/about.html",
                            "/contact.html",
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/favicon.ico",
                            "/api/auth/**",
                            "/api/feedback"
                    ).permitAll()

                    // Admin Only
                    .requestMatchers(
                            "/admin.html",
                            "/api/admin/**"
                    ).hasRole("ADMIN")

                    // Logged-in Users
                    .requestMatchers(
                            "/dashboard.html",
                            "/land-analysis.html",
                            "/leaf-analysis.html",
                            "/history.html",
                            "/profile.html",
                            "/api/land/**",
                            "/api/leaf/**",
                            "/api/history/**",
                            "/api/profile/**"
                    ).authenticated()

                    .anyRequest().authenticated()
            )

            // Exception Handling
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {

                        if (request.getRequestURI().startsWith("/api/")) {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");

                            response.getWriter().write("""
                                {
                                    "success": false,
                                    "error": "Unauthorized. Please login first."
                                }
                            """);

                        } else {

                            response.sendRedirect("/login.html");

                        }

                    })
            )

            // Logout
            .logout(logout -> logout

                    .logoutUrl("/api/auth/logout")

                    .logoutSuccessHandler((request, response, authentication) -> {

                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("application/json");

                        response.getWriter().write("""
                            {
                                "success": true,
                                "message": "Logged out successfully"
                            }
                        """);

                    })

                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()

            );

        return http.build();
    }
}