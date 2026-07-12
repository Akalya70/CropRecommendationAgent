package com.cropagent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LandAnalysisRequest {

    @NotNull(message = "Nitrogen is required")
    @Min(value = 0, message = "Nitrogen must be positive")
    private Double nitrogen;

    @NotNull(message = "Phosphorus is required")
    @Min(value = 0, message = "Phosphorus must be positive")
    private Double phosphorus;

    @NotNull(message = "Potassium is required")
    @Min(value = 0, message = "Potassium must be positive")
    private Double potassium;

    @NotNull(message = "Temperature is required")
    private Double temperature;

    @NotNull(message = "Humidity is required")
    @Min(value = 0, message = "Humidity must be positive")
    @Max(value = 100, message = "Humidity cannot exceed 100")
    private Double humidity;

    @NotNull(message = "pH is required")
    @Min(value = 0, message = "pH must be positive")
    @Max(value = 14, message = "pH cannot exceed 14")
    private Double ph;

    @NotNull(message = "Rainfall is required")
    @Min(value = 0, message = "Rainfall must be positive")
    private Double rainfall;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;
}
