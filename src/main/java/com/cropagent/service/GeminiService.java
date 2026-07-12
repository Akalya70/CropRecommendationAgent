package com.cropagent.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    // Groq endpoint
    private static final String URL =
            "https://api.groq.com/openai/v1/chat/completions";

    // Text-only Groq model
    private static final String MODEL = "llama-3.3-70b-versatile";

    // Groq vision-capable model (supports image + text input)
    private static final String VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    public String generateText(String prompt) {

        try {

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

            ResponseEntity<String> response =
                    restTemplate.postForEntity(URL, entity, String.class);

            return extractContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to call Groq API: " + e.getMessage(), e);
        }
    }

    public String generateMultimodal(String prompt,
                                     String mimeType,
                                     String base64Image) {

        try {

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

            ResponseEntity<String> response =
                    restTemplate.postForEntity(URL, entity, String.class);

            return extractContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to call Groq API (vision): " + e.getMessage(), e);
        }
    }

    private String extractContent(String response) {

        try {

            JsonNode root = objectMapper.readTree(response);

            String text = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return extractJson(text);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to parse Groq response", e);
        }
    }

    private String extractJson(String text) {

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }

        return text;
    }
}