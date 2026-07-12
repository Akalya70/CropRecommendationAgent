package com.cropagent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeafSymptomRequest {

    @NotBlank(message = "Crop name is required")
    private String cropName;

    @NotBlank(message = "Leaf color is required")
    private String leafColor;

    @NotBlank(message = "Leaf condition is required")
    private String leafCondition;

    @NotBlank(message = "Growth status is required")
    private String growth;

    @NotNull(message = "Temperature is required")
    private Double temperature;

    @NotNull(message = "Humidity is required")
    @Min(value = 0, message = "Humidity must be positive")
    @Max(value = 100, message = "Humidity cannot exceed 100")
    private Double humidity;

    @NotNull(message = "Rainfall is required")
    @Min(value = 0, message = "Rainfall must be positive")
    private Double rainfall;

    @NotNull(message = "pH is required")
    @Min(value = 0, message = "pH must be positive")
    @Max(value = 14, message = "pH cannot exceed 14")
    private Double ph;
}
