package com.localservicefinder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServiceRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Service title is required")
    private String title;

    private String description;

    @NotNull(message = "Price rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price rate must be greater than zero")
    private BigDecimal priceRate;

    private String duration = "Per Hour"; // 'Per Hour', 'Per Service', 'Fixed'

    private Boolean isPremium = false;

    // Getters and Setters
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPriceRate() { return priceRate; }
    public void setPriceRate(BigDecimal priceRate) { this.priceRate = priceRate; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }
}
