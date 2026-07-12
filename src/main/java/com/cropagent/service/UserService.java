package com.cropagent.service;

import com.cropagent.dto.PasswordChangeDto;
import com.cropagent.dto.UserProfileDto;
import com.cropagent.dto.UserRegisterDto;
import com.cropagent.entity.User;
import com.cropagent.exception.CustomException;
import com.cropagent.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegisterDto registerDto) {

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new CustomException("Passwords do not match");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new CustomException("Email is already registered");
        }

        User user = new User();
        user.setFullName(registerDto.getFullName());
        user.setEmail(registerDto.getEmail());
        user.setPhoneNumber(registerDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException("User not found with email: " + email));
    }

    public User updateProfile(String email, UserProfileDto profileDto) {

        User user = findByEmail(email);

        if (!user.getEmail().equalsIgnoreCase(profileDto.getEmail())) {

            if (userRepository.existsByEmail(profileDto.getEmail())) {
                throw new CustomException("New email is already in use by another user");
            }

            user.setEmail(profileDto.getEmail());
        }

        user.setFullName(profileDto.getFullName());
        user.setPhoneNumber(profileDto.getPhoneNumber());

        return userRepository.save(user);
    }

    public void changePassword(String email, PasswordChangeDto changeDto) {

        User user = findByEmail(email);

        if (!passwordEncoder.matches(changeDto.getOldPassword(), user.getPassword())) {
            throw new CustomException("Incorrect current password");
        }

        if (!changeDto.getNewPassword().equals(changeDto.getConfirmPassword())) {
            throw new CustomException("New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(changeDto.getNewPassword()));
        userRepository.save(user);
    }
}