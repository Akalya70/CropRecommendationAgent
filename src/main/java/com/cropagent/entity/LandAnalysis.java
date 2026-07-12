package com.cropagent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "land_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double nitrogen;

    @Column(nullable = false)
    private Double phosphorus;

    @Column(nullable = false)
    private Double potassium;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private Double ph;

    @Column(nullable = false)
    private Double rainfall;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String district;

    // AI Prediction results
    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "suitable_season", nullable = false, length = 100)
    private String suitableSeason;

    @Column(name = "water_requirement", nullable = false, length = 100)
    private String waterRequirement;

    @Column(name = "expected_yield", nullable = false, length = 100)
    private String expectedYield;

    @Column(name = "cultivation_tips", nullable = false, columnDefinition = "TEXT")
    private String cultivationTips;

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
