package com.teckiz.dto;

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
public class WebPageResponse {

    private Long id;
    private String pageKey;
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
    private String thumbnail;
    private Long posterId;
    private Long companyId;
    private String companyName;
    private Long companyModuleMapperId;
    private List<ContactInfo> contacts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private Long id;
        private String name;
        private String email;
        private String role;
    }
}

