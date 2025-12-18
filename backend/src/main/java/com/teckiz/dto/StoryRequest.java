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
public class StoryRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String thumbnail;
    private Boolean published;
    private String storyTypeKey; // Story type key
}

