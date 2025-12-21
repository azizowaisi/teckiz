package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.PrincipalMessageResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.PrincipalMessage;
import com.teckiz.repository.PrincipalMessageRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Public - PrincipalMessage", description = "Public API endpoints for PrincipalMessage")
@RequestMapping("/public/principal-message")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicPrincipalMessageController")
public class PrincipalMessageController {

    private final WebsiteManager websiteManager;
    private final PrincipalMessageRepository principalMessageRepository;

    @GetMapping
    public ResponseEntity<PrincipalMessageResponse> getPrincipalMessage() {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        PrincipalMessage principalMessage = principalMessageRepository
                .findByCompanyAndPublishedTrue(companyModuleMapper.getCompany())
                .orElse(null);

        if (principalMessage == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(principalMessage));
    }

    private PrincipalMessageResponse mapToResponse(PrincipalMessage principalMessage) {
        return PrincipalMessageResponse.builder()
                .id(principalMessage.getId())
                .messageKey(principalMessage.getMessageKey())
                .title(principalMessage.getTitle())
                .message(principalMessage.getMessage())
                .principalImage(principalMessage.getPrincipalImage())
                .principalName(principalMessage.getPrincipalName())
                .published(principalMessage.getPublished())
                .companyId(principalMessage.getCompany().getId())
                .companyName(principalMessage.getCompany().getName())
                .createdAt(principalMessage.getCreatedAt())
                .updatedAt(principalMessage.getUpdatedAt())
                .build();
    }
}

