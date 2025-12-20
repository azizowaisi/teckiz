package com.teckiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryTypeResponse {

    private Long id;
    private String typeKey;
    private String name;
    private String description;
    private Long companyId;
    private String companyName;
}

