package com.teckiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebPageRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String shortDescription;
    private String description;
    private String thumbnail;
    private Long posterId;
    private List<String> contactKeys; // contact keys
}

