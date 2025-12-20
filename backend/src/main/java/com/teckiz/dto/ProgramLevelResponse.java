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
public class ProgramLevelResponse {

    private Long id;
    private String levelKey;
    private String name;
    private String description;
    private Integer position;
    private Boolean active;
    private Boolean archived;
    private Long programLevelTypeId;
    private String programLevelTypeName;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

