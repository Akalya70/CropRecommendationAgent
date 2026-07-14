package com.cropagent.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: GROQ API KEY NOT FOUND!");
        } else {
            System.out.println("Groq API Key Loaded Successfully");
            System.out.println("Key starts with: " + apiKey.substring(0, Math.min(10, apiKey.length())));
        }
    }

    private static final String URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final String MODEL =
            "llama-3.3-70b-versatile";

    private static final String VISION_MODEL =
            "meta-llama/llama-4-scout-17b-16e-instruct";

    public String generateText(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> request = new HashMap<>();
        request.put("model", MODEL);
        request.put("messages", Collections.singletonList(message));
        request.put("temperature", 0.2);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(URL, entity, String.class);

            return extractContent(response.getBody());

        } catch (Exception e) {

            System.out.println("Groq API Error: " + e.getMessage());

            throw new RuntimeException(
                    "Failed to call Groq API: " + e.getMessage(), e);
        }
    }

    public String generateMultimodal(String prompt,
                                     String mimeType,
                                     String base64Image) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrl);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", java.util.Arrays.asList(textPart, imagePart));

        Map<String, Object> request = new HashMap<>();
        request.put("model", VISION_MODEL);
        request.put("messages", Collections.singletonList(message));
        request.put("temperature", 0.2);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(URL, entity, String.class);

            return extractContent(response.getBody());

        } catch (Exception e) {

            System.out.println("Groq Vision API Error: " + e.getMessage());

            throw new RuntimeException(
                    "Failed to call Groq Vision API: " + e.getMessage(), e);
        }
    }

    private String extractContent(String response) {

        try {

            JsonNode root = objectMapper.readTree(response);

            if (root.has("choices")) {

                return root.path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

            return response;

        } catch (Exception e) {

            throw new RuntimeException("Unable to parse Groq response", e);
        }
    }
}