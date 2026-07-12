package com.cropagent.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cropagent.dto.LeafSymptomRequest;
import com.cropagent.entity.LeafAnalysis;
import com.cropagent.entity.User;
import com.cropagent.exception.CustomException;
import com.cropagent.repository.LeafAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LeafAnalysisService {

    private final GeminiService geminiService;

    private final LeafAnalysisRepository leafAnalysisRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    LeafAnalysisService(LeafAnalysisRepository leafAnalysisRepository, GeminiService geminiService) {
        this.leafAnalysisRepository = leafAnalysisRepository;
        this.geminiService = geminiService;
    }

    /**
     * OPTION 1: Symptom Based Analysis
     */
    public LeafAnalysis analyzeSymptoms(LeafSymptomRequest req, User user) {
        String prompt = "You are a plant pathologist and crop health expert. Analyze the plant health from the following symptoms:\n" +
                "- Crop Name: " + req.getCropName() + "\n" +
                "- Leaf Color: " + req.getLeafColor() + "\n" +
                "- Leaf Condition: " + req.getLeafCondition() + "\n" +
                "- Growth Status: " + req.getGrowth() + "\n" +
                "- Temperature: " + req.getTemperature() + " °C\n" +
                "- Humidity: " + req.getHumidity() + " %\n" +
                "- Rainfall: " + req.getRainfall() + " mm\n" +
                "- Soil pH: " + req.getPh() + "\n\n" +
                "Provide plant analysis in the following JSON format. Make sure to respond with a valid JSON containing exactly these keys:\n" +
                "{\n" +
                "  \"problem\": \"Primary diagnosis or problem detected (e.g. Fungal Infection, Heat Stress)\",\n" +
                "  \"disease\": \"Specific disease name or 'None' if healthy (e.g. Leaf Spot, Blight)\",\n" +
                "  \"nutrientDeficiency\": \"Deficient nutrient (e.g. Nitrogen, Iron) or 'None'\",\n" +
                "  \"recommendedFertilizer\": \"Specific chemical or fertilizer to apply\",\n" +
                "  \"dosage\": \"Prescribed dosage per liter of water or per acre\",\n" +
                "  \"applicationMethod\": \"How to apply (e.g., Foliar spray, soil drenching)\",\n" +
                "  \"organicSolution\": \"Alternative organic remedy (e.g. Neem oil spray, Compost tea)\",\n" +
                "  \"precautions\": \"Prevention tips and precautions to avoid recurrence\"\n" +
                "}\n" +
                "Note: Respond with ONLY the raw JSON string matching this schema. Do not include markdown tags.";

        String geminiResult = geminiService.generateText(prompt);

        try {
            JsonNode root = objectMapper.readTree(geminiResult);
            String problem = root.path("problem").asText("N/A");
            String disease = root.path("disease").asText("N/A");
            String nutrientDeficiency = root.path("nutrientDeficiency").asText("N/A");
            String recommendedFertilizer = root.path("recommendedFertilizer").asText("N/A");
            String dosage = root.path("dosage").asText("N/A");
            String applicationMethod = root.path("applicationMethod").asText("N/A");
            String organicSolution = root.path("organicSolution").asText("N/A");
            String precautions = root.path("precautions").asText("N/A");

            LeafAnalysis analysis = new LeafAnalysis();
            analysis.setUser(user);
            analysis.setAnalysisType("SYMPTOM");
            analysis.setCropName(req.getCropName());
            analysis.setLeafColor(req.getLeafColor());
            analysis.setLeafCondition(req.getLeafCondition());
            analysis.setGrowth(req.getGrowth());
            analysis.setTemperature(req.getTemperature());
            analysis.setHumidity(req.getHumidity());
            analysis.setRainfall(req.getRainfall());
            analysis.setPh(req.getPh());

            analysis.setProblem(problem);
            analysis.setDisease(disease);
            analysis.setNutrientDeficiency(nutrientDeficiency);
            analysis.setRecommendedFertilizer(recommendedFertilizer);
            analysis.setDosage(dosage);
            analysis.setApplicationMethod(applicationMethod);
            analysis.setOrganicSolution(organicSolution);
            analysis.setPrecautions(precautions);

            return leafAnalysisRepository.save(analysis);
        } catch (Exception e) {
            throw new CustomException("Failed to analyze symptoms due to parsing error: " + e.getMessage() + "\nRaw response: " + geminiResult);
        }
    }

    /**
     * OPTION 2: Image Upload Analysis
     */
    public LeafAnalysis analyzeImage(MultipartFile file, User user) {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png"))) {
            throw new CustomException("Only JPG, JPEG, and PNG formats are accepted");
        }

        // Store file physically
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        String savedFilename = UUID.randomUUID().toString() + "." + ext;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(savedFilename));
        } catch (IOException e) {
            throw new CustomException("Failed to save uploaded image: " + e.getMessage());
        }

        // Convert file to Base64
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new CustomException("Failed to read uploaded image bytes: " + e.getMessage());
        }
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);

        // Formulate prompt
        String prompt = "You are an expert plant pathologist. Analyze this plant leaf image for diseases, pests, or deficiencies. " +
                "Provide analysis in the following JSON format. Make sure to respond with a valid JSON containing exactly these keys:\n" +
                "{\n" +
                "  \"disease\": \"Name of the disease (e.g. Powdery Mildew, Early Blight) or 'Healthy' if no disease is found\",\n" +
                "  \"confidence\": \"Estimate your confidence in this analysis (e.g. 92%)\",\n" +
                "  \"treatment\": \"Standard chemical or physiological treatment instructions\",\n" +
                "  \"recommendedFertilizer\": \"Fertilizers or nutrients needed to recover plant health\",\n" +
                "  \"organicSolution\": \"Organic treatment alternative (e.g., neem oil, copper fungicides, organic compost)\",\n" +
                "  \"precautions\": \"Preventive steps and agronomic practices to avoid this disease in the future\"\n" +
                "}\n" +
                "Note: Respond with ONLY the raw JSON string matching this schema. Do not include markdown tags.";

        String geminiResult = geminiService.generateMultimodal(prompt, contentType, base64Image);

        try {
            JsonNode root = objectMapper.readTree(geminiResult);
            String disease = root.path("disease").asText("Unknown");
            String confidence = root.path("confidence").asText("N/A");
            String treatment = root.path("treatment").asText("N/A");
            String recommendedFertilizer = root.path("recommendedFertilizer").asText("N/A");
            String organicSolution = root.path("organicSolution").asText("N/A");
            String precautions = root.path("precautions").asText("N/A");

            LeafAnalysis analysis = new LeafAnalysis();
            analysis.setUser(user);
            analysis.setAnalysisType("IMAGE");
            analysis.setImageName(savedFilename);
            analysis.setDisease(disease);
            analysis.setConfidence(confidence);
            analysis.setTreatment(treatment);
            analysis.setRecommendedFertilizer(recommendedFertilizer);
            analysis.setOrganicSolution(organicSolution);
            analysis.setPrecautions(precautions);

            // Set crop name dynamically from disease string or keep blank for general prediction
            analysis.setCropName("Leaf Image");

            return leafAnalysisRepository.save(analysis);
        } catch (JsonProcessingException e) {
            // Delete physically saved file if analysis fails to avoid cluttering disk
            try {
                Files.deleteIfExists(Paths.get(uploadDir).resolve(savedFilename));
            } catch (IOException ioEx) {
                // Ignore
            }
            throw new CustomException("Failed to analyze leaf image due to parsing error: " + e.getMessage() + "\nRaw response: " + geminiResult);
        }
    }

    public List<LeafAnalysis> getHistory(User user) {
        return leafAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<LeafAnalysis> searchHistory(User user, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getHistory(user);
        }
        return leafAnalysisRepository.searchByUserAndQuery(user, query);
    }

    public LeafAnalysis getByIdAndUser(Long id, User user) {
        LeafAnalysis analysis = leafAnalysisRepository.findById(id)
                .orElseThrow(() -> new CustomException("Record not found"));
        // Allow access to own records OR admin
        if (!analysis.getUser().getId().equals(user.getId()) && !user.getRole().equals("ROLE_ADMIN")) {
            throw new CustomException("Access denied to this record");
        }
        return analysis;
    }

    public void deleteRecord(Long id, User user) {
        LeafAnalysis analysis = getByIdAndUser(id, user);
        
        // Remove image file if it exists
        if (analysis.getImageName() != null) {
            try {
                Files.deleteIfExists(Paths.get(uploadDir).resolve(analysis.getImageName()));
            } catch (IOException e) {
                // Log and ignore
            }
        }
        leafAnalysisRepository.delete(analysis);
    }

    public LeafAnalysis toggleFavorite(Long id, User user) {
        LeafAnalysis analysis = getByIdAndUser(id, user);
        analysis.setIsFavorite(!analysis.getIsFavorite());
        return leafAnalysisRepository.save(analysis);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "png";
        int lastIndex = filename.lastIndexOf('.');
        return (lastIndex == -1) ? "png" : filename.substring(lastIndex + 1);
    }
}
