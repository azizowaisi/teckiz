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
public class ProgramCourseResponse {

    private Long id;
    private String courseKey;
    private String name;
    private String code;
    private String description;
    private Integer credits;
    private Integer position;
    private Boolean published;
    private Boolean archived;
    private Long programLevelId;
    private String programLevelName;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

