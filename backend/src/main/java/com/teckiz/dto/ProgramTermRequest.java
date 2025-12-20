package com.teckiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramTermRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
}

