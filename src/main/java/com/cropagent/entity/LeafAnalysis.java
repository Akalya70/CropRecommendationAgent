package com.cropagent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaf_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeafAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "analysis_type", nullable = false, length = 20)
    private String analysisType; // 'SYMPTOM' or 'IMAGE'

    // Inputs (Common & Symptom-Based)
    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "leaf_color", length = 50)
    private String leafColor;

    @Column(name = "leaf_condition", length = 50)
    private String leafCondition;

    @Column(length = 50)
    private String growth;

    private Double temperature;
    private Double humidity;
    private Double rainfall;
    private Double ph;

    // Inputs (Image-Based)
    @Column(name = "image_name", length = 255)
    private String imageName;

    // Gemini API Output Fields
    @Column(length = 150)
    private String disease;

    @Column(length = 20)
    private String confidence;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "recommended_fertilizer", columnDefinition = "TEXT")
    private String recommendedFertilizer;

    @Column(name = "organic_solution", columnDefinition = "TEXT")
    private String organicSolution;

    @Column(columnDefinition = "TEXT")
    private String precautions;

    // Additional output fields for Symptom-Based
    @Column(columnDefinition = "TEXT")
    private String problem;

    @Column(name = "nutrient_deficiency", columnDefinition = "TEXT")
    private String nutrientDeficiency;

    @Column(columnDefinition = "TEXT")
    private String dosage;

    @Column(name = "application_method", columnDefinition = "TEXT")
    private String applicationMethod;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isFavorite == null) {
            this.isFavorite = false;
        }
    }
}
