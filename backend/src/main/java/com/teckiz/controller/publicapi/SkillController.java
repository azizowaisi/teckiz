package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.SkillResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Skill;
import com.teckiz.repository.SkillRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Public - Skill", description = "Public API endpoints for Skill")
@RequestMapping("/public/skills")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicSkillController")
public class SkillController {

    private final WebsiteManager websiteManager;
    private final SkillRepository skillRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listSkills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());
        Page<Skill> skills = skillRepository.findByCompanyAndArchivedFalse(
                companyModuleMapper.getCompany(), pageable);

        List<SkillResponse> responses = skills.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("skills", responses);
        response.put("totalPages", skills.getTotalPages());
        response.put("totalElements", skills.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{skillKey}")
    public ResponseEntity<SkillResponse> getSkill(@PathVariable String skillKey) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Skill skill = skillRepository.findBySkillKey(skillKey)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (Boolean.TRUE.equals(skill.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(skill));
    }

    private SkillResponse mapToResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .skillKey(skill.getSkillKey())
                .name(skill.getName())
                .description(skill.getDescription())
                .icon(skill.getIcon())
                .position(skill.getPosition())
                .archived(skill.getArchived())
                .companyId(skill.getCompany().getId())
                .companyName(skill.getCompany().getName())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}

