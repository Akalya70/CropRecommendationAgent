package com.cropagent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cropagent.dto.LandAnalysisRequest;
import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.User;
import com.cropagent.exception.CustomException;
import com.cropagent.repository.LandAnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LandAnalysisService {

    private final GeminiService geminiService;
    private final LandAnalysisRepository landAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LandAnalysisService(GeminiService geminiService,
                               LandAnalysisRepository landAnalysisRepository) {
        this.geminiService = geminiService;
        this.landAnalysisRepository = landAnalysisRepository;
    }

    public LandAnalysis analyzeLand(LandAnalysisRequest req, User user) {

        String prompt =
                "You are an agriculture expert. " +
                "Return ONLY valid JSON. Do not write explanations, markdown, or code blocks.\n\n" +

                "Analyze the following soil conditions:\n" +
                "Nitrogen: " + req.getNitrogen() + "\n" +
                "Phosphorus: " + req.getPhosphorus() + "\n" +
                "Potassium: " + req.getPotassium() + "\n" +
                "Temperature: " + req.getTemperature() + "\n" +
                "Humidity: " + req.getHumidity() + "\n" +
                "pH: " + req.getPh() + "\n" +
                "Rainfall: " + req.getRainfall() + "\n" +
                "State: " + req.getState() + "\n" +
                "District: " + req.getDistrict() + "\n\n" +

                "Return exactly this JSON format:\n" +

                "{\n" +
                "\"cropName\":\"\",\n" +
                "\"reason\":\"\",\n" +
                "\"suitableSeason\":\"\",\n" +
                "\"waterRequirement\":\"\",\n" +
                "\"expectedYield\":\"\",\n" +
                "\"cultivationTips\":\"\"\n" +
                "}";

        String geminiResult = geminiService.generateText(prompt);

        try {

            JsonNode root = objectMapper.readTree(geminiResult);

            LandAnalysis land = new LandAnalysis();

            land.setUser(user);

            land.setNitrogen(req.getNitrogen());
            land.setPhosphorus(req.getPhosphorus());
            land.setPotassium(req.getPotassium());
            land.setTemperature(req.getTemperature());
            land.setHumidity(req.getHumidity());
            land.setPh(req.getPh());
            land.setRainfall(req.getRainfall());
            land.setState(req.getState());
            land.setDistrict(req.getDistrict());

            land.setCropName(root.path("cropName").asText());
            land.setReason(root.path("reason").asText());
            land.setSuitableSeason(root.path("suitableSeason").asText());
            land.setWaterRequirement(root.path("waterRequirement").asText());
            land.setExpectedYield(root.path("expectedYield").asText());
            land.setCultivationTips(root.path("cultivationTips").asText());

            return landAnalysisRepository.save(land);

        } catch (Exception e) {

            throw new CustomException(
                    "Failed to parse AI response.\n\n" +
                    geminiResult);
        }
    }

    public List<LandAnalysis> getHistory(User user) {
        return landAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<LandAnalysis> searchHistory(User user, String query) {

        if (query == null || query.trim().isEmpty()) {
            return getHistory(user);
        }

        return landAnalysisRepository.searchByUserAndQuery(user, query);
    }

    public LandAnalysis getByIdAndUser(Long id, User user) {

        LandAnalysis analysis = landAnalysisRepository.findById(id)
                .orElseThrow(() -> new CustomException("Record not found"));

        if (!analysis.getUser().getId().equals(user.getId())
                && !user.getRole().equals("ROLE_ADMIN")) {
            throw new CustomException("Access denied");
        }

        return analysis;
    }

    public void deleteRecord(Long id, User user) {

        LandAnalysis analysis = getByIdAndUser(id, user);
        landAnalysisRepository.delete(analysis);
    }

    public LandAnalysis toggleFavorite(Long id, User user) {

        LandAnalysis analysis = getByIdAndUser(id, user);

        analysis.setIsFavorite(!analysis.getIsFavorite());

        return landAnalysisRepository.save(analysis);
    }
}