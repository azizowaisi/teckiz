package com.teckiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalMessageRequest {

    private String title;
    private String message;
    private String principalName;
    private String principalImage;
    private Boolean published;
}

