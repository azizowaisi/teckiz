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
public class ProgramLevelRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private Integer position;
    private Boolean active;
    private Long programLevelTypeId;
}

