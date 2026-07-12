package com.cropagent.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.LeafAnalysis;
import com.cropagent.entity.User;
import com.cropagent.service.EmailService;
import com.cropagent.service.LandAnalysisService;
import com.cropagent.service.LeafAnalysisService;
import com.cropagent.service.PdfService;
import com.cropagent.service.UserService;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final LandAnalysisService landAnalysisService;
    private final LeafAnalysisService leafAnalysisService;
    private final UserService userService;
    private final PdfService pdfService;
    private final EmailService emailService;

    // Constructor Injection
    public HistoryController(
            LandAnalysisService landAnalysisService,
            LeafAnalysisService leafAnalysisService,
            UserService userService,
            PdfService pdfService,
            EmailService emailService) {

        this.landAnalysisService = landAnalysisService;
        this.leafAnalysisService = leafAnalysisService;
        this.userService = userService;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<?> getHistory(
            @RequestParam(value = "query", required = false) String query,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        User user = userService.findByEmail(principal.getName());

        List<LandAnalysis> landHistory =
                landAnalysisService.searchHistory(user, query);

        List<LeafAnalysis> leafHistory =
                leafAnalysisService.searchHistory(user, query);

        Map<String, Object> response = new HashMap<>();
        response.put("land", landHistory);
        response.put("leaf", leafHistory);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHistory(
            @PathVariable Long id,
            @RequestParam("type") String type,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        User user = userService.findByEmail(principal.getName());

        if ("LAND".equalsIgnoreCase(type)) {
            landAnalysisService.deleteRecord(id, user);
        } else if ("LEAF".equalsIgnoreCase(type)) {
            leafAnalysisService.deleteRecord(id, user);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid analysis type"));
        }

        return ResponseEntity.ok(
                Map.of("message", "Record deleted successfully"));
    }

    @PostMapping("/favorite/{id}")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            @RequestParam("type") String type,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        User user = userService.findByEmail(principal.getName());

        Object result;

        if ("LAND".equalsIgnoreCase(type)) {
            result = landAnalysisService.toggleFavorite(id, user);
        } else if ("LEAF".equalsIgnoreCase(type)) {
            result = leafAnalysisService.toggleFavorite(id, user);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid analysis type"));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @RequestParam("type") String type,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByEmail(principal.getName());

        byte[] pdfBytes;
        String filename;

        if ("LAND".equalsIgnoreCase(type)) {

            LandAnalysis land =
                    landAnalysisService.getByIdAndUser(id, user);

            pdfBytes = pdfService.generateLandPdf(land);
            filename = "land_analysis_" + id + ".pdf";

        } else if ("LEAF".equalsIgnoreCase(type)) {

            LeafAnalysis leaf =
                    leafAnalysisService.getByIdAndUser(id, user);

            pdfBytes = pdfService.generateLeafPdf(leaf);
            filename = "leaf_analysis_" + id + ".pdf";

        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/email/{id}")
    public ResponseEntity<?> emailReport(
            @PathVariable Long id,
            @RequestParam("type") String type,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        User user = userService.findByEmail(principal.getName());

        if ("LAND".equalsIgnoreCase(type)) {

            LandAnalysis land =
                    landAnalysisService.getByIdAndUser(id, user);

            emailService.sendLandRecommendation(user.getEmail(), land);

        } else if ("LEAF".equalsIgnoreCase(type)) {

            LeafAnalysis leaf =
                    leafAnalysisService.getByIdAndUser(id, user);

            emailService.sendLeafRecommendation(user.getEmail(), leaf);

        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid analysis type"));
        }

        return ResponseEntity.ok(
                Map.of("message",
                        "Report emailed successfully to " + user.getEmail()));
    }
}