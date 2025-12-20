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
public class ProgramCourseRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String code;
    private String description;
    private Integer credits;
    private Integer position;
    private Boolean published;
    private Long programLevelId;
}

