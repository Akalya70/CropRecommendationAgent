package com.cropagent.service;

import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.LeafAnalysis;
import com.cropagent.exception.CustomException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(46, 125, 50));
    private final Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
    private final Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(46, 125, 50));
    private final Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private final Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private final Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

    public byte[] generateLandPdf(LandAnalysis land) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            Paragraph title = new Paragraph("CROP RECOMMENDATION AGENT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Land Analysis & Crop Recommendation Report", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Metadata Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);
            metaTable.addCell(createLabelCell("Farmer Name:"));
            metaTable.addCell(createValueCell(land.getUser().getFullName()));
            metaTable.addCell(createLabelCell("Email:"));
            metaTable.addCell(createValueCell(land.getUser().getEmail()));
            metaTable.addCell(createLabelCell("Date of Analysis:"));
            metaTable.addCell(createValueCell(land.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(metaTable);

            // Soil Parameters Table
            document.add(new Paragraph("Soil & Environmental Conditions", sectionHeaderFont));
            Paragraph spacing = new Paragraph(" ");
            spacing.setSpacingBefore(5);
            document.add(spacing);

            PdfPTable soilTable = new PdfPTable(4);
            soilTable.setWidthPercentage(100);
            soilTable.setSpacingAfter(20);
            soilTable.addCell(createHeaderCell("Parameter"));
            soilTable.addCell(createHeaderCell("Value"));
            soilTable.addCell(createHeaderCell("Parameter"));
            soilTable.addCell(createHeaderCell("Value"));

            soilTable.addCell(createLabelCell("Nitrogen (N)"));
            soilTable.addCell(createValueCell(land.getNitrogen() + " mg/kg"));
            soilTable.addCell(createLabelCell("Phosphorus (P)"));
            soilTable.addCell(createValueCell(land.getPhosphorus() + " mg/kg"));

            soilTable.addCell(createLabelCell("Potassium (K)"));
            soilTable.addCell(createValueCell(land.getPotassium() + " mg/kg"));
            soilTable.addCell(createLabelCell("Soil pH"));
            soilTable.addCell(createValueCell(String.valueOf(land.getPh())));

            soilTable.addCell(createLabelCell("Temperature"));
            soilTable.addCell(createValueCell(land.getTemperature() + " °C"));
            soilTable.addCell(createLabelCell("Humidity"));
            soilTable.addCell(createValueCell(land.getHumidity() + " %"));

            soilTable.addCell(createLabelCell("Rainfall"));
            soilTable.addCell(createValueCell(land.getRainfall() + " mm"));
            soilTable.addCell(createLabelCell("Location"));
            soilTable.addCell(createValueCell(land.getDistrict() + ", " + land.getState()));
            document.add(soilTable);

            // Recommendation Details
            document.add(new Paragraph("AI Recommendation & Insights", sectionHeaderFont));
            document.add(spacing);

            PdfPTable recTable = new PdfPTable(1);
            recTable.setWidthPercentage(100);
            recTable.setSpacingAfter(20);

            PdfPCell cropCell = new PdfPCell(new Paragraph("RECOMMENDED CROP: " + land.getCropName().toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(46, 125, 50))));
            cropCell.setBackgroundColor(new Color(241, 248, 233));
            cropCell.setPadding(8);
            cropCell.setBorderColor(new Color(46, 125, 50));
            recTable.addCell(cropCell);

            recTable.addCell(createBlockCell("Why this crop is suitable:\n" + land.getReason()));
            recTable.addCell(createBlockCell("Optimal Season: " + land.getSuitableSeason()));
            recTable.addCell(createBlockCell("Water Requirement: " + land.getWaterRequirement()));
            recTable.addCell(createBlockCell("Expected Yield: " + land.getExpectedYield()));
            recTable.addCell(createBlockCell("Cultivation & Fertilizer Tips:\n" + land.getCultivationTips()));

            document.add(recTable);

            // Footer
            Paragraph footer = new Paragraph("Generated automatically by Crop Recommendation Agent. Disclaimer: Recommendations are AI-generated and should be verified based on local farming practices.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new CustomException("Failed to generate PDF: " + e.getMessage());
        }
    }

    public byte[] generateLeafPdf(LeafAnalysis leaf) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            Paragraph title = new Paragraph("CROP RECOMMENDATION AGENT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Crop Leaf Health & Diagnostics Report", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Metadata Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);
            metaTable.addCell(createLabelCell("Farmer Name:"));
            metaTable.addCell(createValueCell(leaf.getUser().getFullName()));
            metaTable.addCell(createLabelCell("Email:"));
            metaTable.addCell(createValueCell(leaf.getUser().getEmail()));
            metaTable.addCell(createLabelCell("Date of Analysis:"));
            metaTable.addCell(createValueCell(leaf.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            metaTable.addCell(createLabelCell("Analysis Method:"));
            metaTable.addCell(createValueCell(leaf.getAnalysisType() + " BASED"));
            document.add(metaTable);

            Paragraph spacing = new Paragraph(" ");
            spacing.setSpacingBefore(5);

            // Input parameters (If Symptom Based)
            if ("SYMPTOM".equalsIgnoreCase(leaf.getAnalysisType())) {
                document.add(new Paragraph("Submitted Crop Symptoms & Environment", sectionHeaderFont));
                document.add(spacing);

                PdfPTable symTable = new PdfPTable(4);
                symTable.setWidthPercentage(100);
                symTable.setSpacingAfter(20);
                symTable.addCell(createHeaderCell("Parameter"));
                symTable.addCell(createHeaderCell("Value"));
                symTable.addCell(createHeaderCell("Parameter"));
                symTable.addCell(createHeaderCell("Value"));

                symTable.addCell(createLabelCell("Crop Name"));
                symTable.addCell(createValueCell(leaf.getCropName()));
                symTable.addCell(createLabelCell("Leaf Color"));
                symTable.addCell(createValueCell(leaf.getLeafColor()));

                symTable.addCell(createLabelCell("Leaf Condition"));
                symTable.addCell(createValueCell(leaf.getLeafCondition()));
                symTable.addCell(createLabelCell("Growth Status"));
                symTable.addCell(createValueCell(leaf.getGrowth()));

                symTable.addCell(createLabelCell("Temperature"));
                symTable.addCell(createValueCell(leaf.getTemperature() + " °C"));
                symTable.addCell(createLabelCell("Humidity"));
                symTable.addCell(createValueCell(leaf.getHumidity() + " %"));

                symTable.addCell(createLabelCell("Rainfall"));
                symTable.addCell(createValueCell(leaf.getRainfall() + " mm"));
                symTable.addCell(createLabelCell("Soil pH"));
                symTable.addCell(createValueCell(String.valueOf(leaf.getPh())));

                document.add(symTable);
            }

            // AI Recommendation Details
            document.add(new Paragraph("AI Diagnostic Results & Remedies", sectionHeaderFont));
            document.add(spacing);

            PdfPTable recTable = new PdfPTable(1);
            recTable.setWidthPercentage(100);
            recTable.setSpacingAfter(20);

            String statusHeader = "DIAGNOSED DISEASE: " + leaf.getDisease().toUpperCase();
            if (leaf.getConfidence() != null && !leaf.getConfidence().equals("N/A")) {
                statusHeader += " (CONFIDENCE: " + leaf.getConfidence() + ")";
            }
            PdfPCell diseaseCell = new PdfPCell(new Paragraph(statusHeader, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(46, 125, 50))));
            diseaseCell.setBackgroundColor(new Color(241, 248, 233));
            diseaseCell.setPadding(8);
            diseaseCell.setBorderColor(new Color(46, 125, 50));
            recTable.addCell(diseaseCell);

            if ("SYMPTOM".equalsIgnoreCase(leaf.getAnalysisType())) {
                recTable.addCell(createBlockCell("Primary Problem:\n" + leaf.getProblem()));
                recTable.addCell(createBlockCell("Nutrient Deficiency:\n" + leaf.getNutrientDeficiency()));
                recTable.addCell(createBlockCell("Recommended Chemical Solution:\n" + leaf.getRecommendedFertilizer() + " (Dosage: " + leaf.getDosage() + ", Method: " + leaf.getApplicationMethod() + ")"));
                recTable.addCell(createBlockCell("Organic Solution:\n" + leaf.getOrganicSolution()));
            } else {
                recTable.addCell(createBlockCell("Treatment Plan:\n" + leaf.getTreatment()));
                recTable.addCell(createBlockCell("Recommended Chemical Solution:\n" + leaf.getRecommendedFertilizer()));
                recTable.addCell(createBlockCell("Organic Solution Alternative:\n" + leaf.getOrganicSolution()));
            }
            
            recTable.addCell(createBlockCell("Prevention & Care Tips:\n" + leaf.getPrecautions()));
            document.add(recTable);

            // Footer
            Paragraph footer = new Paragraph("Generated automatically by Crop Recommendation Agent. Disclaimer: Recommendations are AI-generated and should be verified based on local farming practices.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new CustomException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private PdfPCell createLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, labelFont));
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text == null ? "N/A" : text, valueFont));
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(new Color(46, 125, 50));
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createBlockCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text == null ? "" : text, bodyFont));
        cell.setPadding(8);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }
}
