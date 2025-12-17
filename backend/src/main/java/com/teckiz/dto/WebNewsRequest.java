package com.teckiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebNewsRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String shortDescription;
    private String description;
    private Boolean published;
    private LocalDateTime publishedAt;
    private Boolean carousel;
    private String embedCode;
    private Long posterId;
    private Long webNewsTypeId;
    private List<String> contactKeys;
}

