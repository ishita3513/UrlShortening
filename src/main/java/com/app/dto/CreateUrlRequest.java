package com.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUrlRequest {

    @NotBlank(message = "originalUrl is required")
    @Pattern(regexp = "https?://.+", message = "originalUrl must be a valid absolute URL")
    private String originalUrl;

    @Size(max = 50, message = "customAlias must be at most 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]{2,50}$", message = "customAlias may only contain letters, numbers and hyphens")
    private String customAlias;

    @Min(value = 1, message = "expiredInDays must be at least 1")
    @Max(value = 3650, message = "expiredInDays must be at most 3650")
    private Integer expiredInDays;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public Integer getExpiredInDays() {
        return expiredInDays;
    }

    public void setExpiredInDays(Integer expiredInDays) {
        this.expiredInDays = expiredInDays;
    }
}
