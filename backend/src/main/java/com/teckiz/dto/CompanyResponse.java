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
public class CompanyResponse {

    private Long id;
    private String companyKey;
    private String name;
    private String slug;
    private String description;
    private String address;
    private String city;
    private String country;
    private String timeZone;
    private Boolean active;
    private Boolean archived;
    private String email;
    private String phone;
    private String lang;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

