package com.cropagent.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.LeafAnalysis;
import com.cropagent.exception.CustomException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendLandRecommendation(String recipientEmail, LandAnalysis land) {

        if (mailSender == null) {
            throw new CustomException("Email service is not configured. Please check SMTP settings in application.properties.");
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(recipientEmail);
            message.setSubject("Crop Recommendation Report: " + land.getCropName());

            String text =
                    """
                    Dear Farmer,
                    
                    Here is your AI-powered Land Analysis & Crop Recommendation:
                    
                    --- INPUTS ---
                    Soil N-P-K: """ + land.getNitrogen() + " - " +
                    land.getPhosphorus() + " - " +
                    land.getPotassium() + " mg/kg\n" +

                    "Temperature: " + land.getTemperature() + " °C\n" +
                    "Humidity: " + land.getHumidity() + " %\n" +
                    "Soil pH: " + land.getPh() + "\n" +
                    "Rainfall: " + land.getRainfall() + " mm\n" +
                    "Location: " + land.getDistrict() + ", " + land.getState() + "\n\n" +

                    "--- RECOMMENDATION ---\n" +
                    "Recommended Crop: " + land.getCropName() + "\n" +
                    "Suitability Reason: " + land.getReason() + "\n" +
                    "Suitable Season: " + land.getSuitableSeason() + "\n" +
                    "Water Requirement: " + land.getWaterRequirement() + "\n" +
                    "Expected Yield: " + land.getExpectedYield() + "\n" +
                    "Cultivation Tips:\n" +
                    land.getCultivationTips() +

                    "\n\nWishing you a successful harvest!\n\n" +
                    "Crop Recommendation Agent Team";

            message.setText(text);

            mailSender.send(message);

        } catch (Exception e) {

            throw new CustomException(
                    "Failed to send email. Ensure SMTP settings in application.properties are valid.\n\nError: "
                            + e.getMessage());

        }
    }

    public void sendLeafRecommendation(String recipientEmail, LeafAnalysis leaf) {

        if (mailSender == null) {
            throw new CustomException("Email service is not configured. Please check SMTP settings in application.properties.");
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(recipientEmail);
            message.setSubject("Crop Leaf Analysis Report - Disease: " + leaf.getDisease());

            StringBuilder sb = new StringBuilder();

            sb.append("Dear Farmer,\n\n");
            sb.append("Here is your AI-powered Leaf Condition Analysis Report.\n\n");

            if ("SYMPTOM".equalsIgnoreCase(leaf.getAnalysisType())) {

                sb.append("----- SYMPTOMS -----\n");
                sb.append("Crop Name: ").append(leaf.getCropName()).append("\n");
                sb.append("Leaf Color: ").append(leaf.getLeafColor()).append("\n");
                sb.append("Leaf Condition: ").append(leaf.getLeafCondition()).append("\n");
                sb.append("Growth Status: ").append(leaf.getGrowth()).append("\n");
                sb.append("Temperature: ").append(leaf.getTemperature()).append(" °C\n");
                sb.append("Humidity: ").append(leaf.getHumidity()).append(" %\n\n");

                sb.append("----- ANALYSIS RESULT -----\n");
                sb.append("Problem: ").append(leaf.getProblem()).append("\n");
                sb.append("Disease: ").append(leaf.getDisease()).append("\n");
                sb.append("Nutrient Deficiency: ").append(leaf.getNutrientDeficiency()).append("\n");
                sb.append("Recommended Fertilizer: ").append(leaf.getRecommendedFertilizer()).append("\n");
                sb.append("Dosage: ").append(leaf.getDosage()).append("\n");
                sb.append("Application Method: ").append(leaf.getApplicationMethod()).append("\n");
                sb.append("Organic Alternative: ").append(leaf.getOrganicSolution()).append("\n");
                sb.append("Prevention Tips: ").append(leaf.getPrecautions()).append("\n");

            } else {

                sb.append("----- IMAGE ANALYSIS -----\n");
                sb.append("Image Name: ").append(leaf.getImageName()).append("\n");
                sb.append("Disease: ").append(leaf.getDisease()).append("\n");
                sb.append("Confidence: ").append(leaf.getConfidence()).append("\n");
                sb.append("Treatment: ").append(leaf.getTreatment()).append("\n");
                sb.append("Recommended Fertilizer: ").append(leaf.getRecommendedFertilizer()).append("\n");
                sb.append("Organic Solution: ").append(leaf.getOrganicSolution()).append("\n");
                sb.append("Precautions: ").append(leaf.getPrecautions()).append("\n");
            }

            sb.append("\n");
            sb.append("We wish you healthy crops and a successful harvest.\n\n");
            sb.append("Crop Recommendation Agent Team");

            message.setText(sb.toString());

            mailSender.send(message);

        } catch (Exception e) {

            throw new CustomException(
                    "Failed to send email. Ensure SMTP settings in application.properties are valid.\n\nError: "
                            + e.getMessage());

        }
    }
}