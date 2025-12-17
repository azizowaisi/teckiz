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
public class RoleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role type is required")
    private String roleType; // e.g., ROLE_COMPANY_ADMIN

    @Builder.Default
    private Boolean companyRole = true;
}

