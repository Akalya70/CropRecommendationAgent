package com.cropagent.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cropagent.dto.UserLoginDto;
import com.cropagent.dto.UserRegisterDto;
import com.cropagent.entity.User;
import com.cropagent.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register User
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid UserRegisterDto registerDto) {

        userService.registerUser(registerDto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login User
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid UserLoginDto loginDto,
            HttpServletRequest request) {

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDto.getEmail(),
                                    loginDto.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);

            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context);

            User user = userService.findByEmail(loginDto.getEmail());

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("id", user.getId());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid email or password");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }
    }

    /**
     * Current Logged-in User
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Principal principal) {

        if (principal == null) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Not authenticated");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        User user = userService.findByEmail(principal.getName());

        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("id", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * Check Login Status
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkAuthentication(Principal principal) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("authenticated", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userService.findByEmail(principal.getName());

        response.put("authenticated", true);
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }
}