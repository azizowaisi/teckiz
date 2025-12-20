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
public class ProgramClassResponse {

    private Long id;
    private String classKey;
    private String name;
    private String description;
    private String room;
    private String schedule;
    private String instructor;
    private Integer capacity;
    private Integer enrolled;
    private Boolean published;
    private Boolean archived;
    private Long programCourseId;
    private String programCourseName;
    private Long programTermId;
    private String programTermName;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

