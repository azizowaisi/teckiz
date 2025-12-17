package com.teckiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRoleMapperResponse {

    private Long id;
    private String companyRoleKey;
    private Boolean archived;
    private Long companyId;
    private String companyName;
    private Long roleId;
    private String roleName;
    private String roleType;
}

