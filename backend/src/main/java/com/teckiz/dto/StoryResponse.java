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
public class StoryResponse {

    private Long id;
    private String storyKey;
    private String title;
    private String description;
    private String thumbnail;
    private Boolean published;
    private Boolean archived;
    private Long companyId;
    private String companyName;
    private Long storyTypeId;
    private String storyTypeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

