package com.cropagent.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cropagent.entity.Feedback;
import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.LeafAnalysis;
import com.cropagent.entity.User;
import com.cropagent.repository.FeedbackRepository;
import com.cropagent.repository.LandAnalysisRepository;
import com.cropagent.repository.LeafAnalysisRepository;
import com.cropagent.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final LandAnalysisRepository landAnalysisRepository;
    private final LeafAnalysisRepository leafAnalysisRepository;
    private final FeedbackRepository feedbackRepository;

    public AdminController(UserRepository userRepository,
                           LandAnalysisRepository landAnalysisRepository,
                           LeafAnalysisRepository leafAnalysisRepository,
                           FeedbackRepository feedbackRepository) {

        this.userRepository = userRepository;
        this.landAnalysisRepository = landAnalysisRepository;
        this.leafAnalysisRepository = leafAnalysisRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> users = userRepository.findAll();

        users.forEach(user -> user.setPassword("PROTECTED"));

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @NonNull Long id,
                                        Principal principal) {

        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        if (admin.getId().equals(id)) {

            Map<String, String> response = new HashMap<>();
            response.put("error", "Cannot delete your own admin account");

            return ResponseEntity.badRequest().body(response);
        }

        userRepository.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message",
                "User and all their history deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/land")
    public ResponseEntity<List<LandAnalysis>> getAllLandHistory() {

        List<LandAnalysis> list = landAnalysisRepository.findAll();

        list.forEach(item -> {
            if (item.getUser() != null) {
                item.getUser().setPassword(null);
            }
        });

        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/history/land/{id}")
    public ResponseEntity<?> deleteLandRecord(@PathVariable Long id) {

        landAnalysisRepository.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Land record deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/leaf")
    public ResponseEntity<List<LeafAnalysis>> getAllLeafHistory() {

        List<LeafAnalysis> list = leafAnalysisRepository.findAll();

        list.forEach(item -> {
            if (item.getUser() != null) {
                item.getUser().setPassword(null);
            }
        });

        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/history/leaf/{id}")
    public ResponseEntity<?> deleteLeafRecord(@PathVariable Long id) {

        leafAnalysisRepository.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Leaf record deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<Feedback>> getAllFeedback() {

        List<Feedback> feedback =
                feedbackRepository.findAllByOrderByCreatedAtDesc();

        return ResponseEntity.ok(feedback);
    }

    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {

        feedbackRepository.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Feedback deleted successfully");

        return ResponseEntity.ok(response);
    }
}