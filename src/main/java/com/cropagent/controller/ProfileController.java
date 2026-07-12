package com.cropagent.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cropagent.dto.PasswordChangeDto;
import com.cropagent.dto.UserProfileDto;
import com.cropagent.entity.User;
import com.cropagent.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    // Constructor Injection
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody @Valid UserProfileDto profileDto,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }

        User updated = userService.updateProfile(principal.getName(), profileDto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("fullName", updated.getFullName());
        response.put("email", updated.getEmail());
        response.put("phoneNumber", updated.getPhoneNumber());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid PasswordChangeDto changeDto,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }

        userService.changePassword(principal.getName(), changeDto);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully"));
    }
}