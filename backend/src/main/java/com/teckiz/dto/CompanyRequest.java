package com.teckiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String address;
    private String city;
    private String country;
    private String timeZone;
    private String stripeId;
    private String language;
    private Boolean isActive;
}

