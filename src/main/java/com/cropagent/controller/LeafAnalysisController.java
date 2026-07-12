package com.cropagent.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cropagent.dto.LeafSymptomRequest;
import com.cropagent.entity.LeafAnalysis;
import com.cropagent.entity.User;
import com.cropagent.service.LeafAnalysisService;
import com.cropagent.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leaf")
public class LeafAnalysisController {

    private final LeafAnalysisService leafAnalysisService;
    private final UserService userService;

    // Constructor Injection
    public LeafAnalysisController(LeafAnalysisService leafAnalysisService,
                                  UserService userService) {
        this.leafAnalysisService = leafAnalysisService;
        this.userService = userService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeSymptoms(
            @RequestBody @Valid LeafSymptomRequest request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body("{\"error\":\"Unauthorized\"}");
        }

        User user = userService.findByEmail(principal.getName());

        LeafAnalysis analysis = leafAnalysisService.analyzeSymptoms(request, user);

        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadLeafImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body("{\"error\":\"Unauthorized\"}");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Please upload a file\"}");
        }

        User user = userService.findByEmail(principal.getName());

        LeafAnalysis analysis = leafAnalysisService.analyzeImage(file, user);

        return ResponseEntity.ok(analysis);
    }
}