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
public class AddModuleToCompanyRequest {

    @NotBlank(message = "Module key is required")
    private String moduleKey; // moduleKey from Module entity
}

