package com.teckiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyModuleMapperResponse {

    private Long id;
    private String moduleMapperKey;
    private String directory;
    private String email;
    private String host;
    private Boolean live;
    private Boolean master;
    private Boolean archived;
    private String header;
    private Long companyId;
    private String companyName;
    private Long moduleId;
    private String moduleName;
    private String moduleType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

