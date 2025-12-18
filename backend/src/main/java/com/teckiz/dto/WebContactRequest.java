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
public class WebContactRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String role;
    private String description;
    private String url;
    private String email;
    private String phone;
    private String twitter;
    private String facebook;
    private String instagram;
    private String linkedin;
    private String researchGate;
    private Integer position;
    private String thumbnail;
    private List<String> contactTypeKeys; // contact type keys
}

