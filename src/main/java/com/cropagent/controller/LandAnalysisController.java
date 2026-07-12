package com.cropagent.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cropagent.dto.LandAnalysisRequest;
import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.User;
import com.cropagent.service.LandAnalysisService;
import com.cropagent.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/land")
public class LandAnalysisController {

    private final LandAnalysisService landAnalysisService;

    private final UserService userService;

    // Constructor Injection
    public LandAnalysisController(LandAnalysisService landAnalysisService,
                                  UserService userService) {
        this.landAnalysisService = landAnalysisService;
        this.userService = userService;
    }


    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeLand(@RequestBody @Valid LandAnalysisRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("{\"error\": \"Unauthorized - Please log in\"}");
        }
        User user = userService.findByEmail(principal.getName());
        LandAnalysis analysis = landAnalysisService.analyzeLand(request, user);
        return ResponseEntity.ok(analysis);
    }
}
