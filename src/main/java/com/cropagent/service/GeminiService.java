package com.cropagent.service;

import java.util.Arrays;
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

import jakarta.annotation.PostConstruct;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final String TEXT_MODEL =
            "llama-3.3-70b-versatile";

    private static final String VISION_MODEL =
            "meta-llama/llama-4-scout-17b-16e-instruct";

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {

        System.out.println("========== GROQ DEBUG ==========");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR : GROQ_API_KEY NOT FOUND");
        } else {
            System.out.println("Groq API Loaded Successfully");
            System.out.println("API KEY = " + apiKey);
        }

        System.out.println("================================");
    }

    public String generateText(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", TEXT_MODEL);
        body.put("messages", Collections.singletonList(message));
        body.put("temperature", 0.2);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(API_URL, request, String.class);

            return getContent(response.getBody());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to call Groq API : " + e.getMessage(), e);
        }
    }

    public String generateMultimodal(String prompt,
                                     String mimeType,
                                     String base64Image) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> text = new HashMap<>();
        text.put("type", "text");
        text.put("text", prompt);

        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url",
                "data:" + mimeType + ";base64," + base64Image);

        Map<String, Object> image = new HashMap<>();
        image.put("type", "image_url");
        image.put("image_url", imageUrl);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", Arrays.asList(text, image));

        Map<String, Object> body = new HashMap<>();
        body.put("model", VISION_MODEL);
        body.put("messages", Collections.singletonList(message));
        body.put("temperature", 0.2);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(API_URL, request, String.class);

            return getContent(response.getBody());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq Vision Error : " + e.getMessage(), e);
        }
    }

    private String getContent(String response) {

        try {

            JsonNode root = mapper.readTree(response);

            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to parse Groq response", e);
        }
    }
}