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
public class ProgramClassRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String room;
    private String schedule;
    private String instructor;
    private Integer capacity;
    private Boolean published;
    private Long programCourseId;
    private Long programTermId;
}

