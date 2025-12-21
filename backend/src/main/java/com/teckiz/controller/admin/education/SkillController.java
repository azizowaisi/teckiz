package com.teckiz.controller.admin.education;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.SkillRequest;
import com.teckiz.dto.SkillResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Skill;
import com.teckiz.repository.SkillRepository;
import com.teckiz.service.ModuleAccessManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/education/admin/skills")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
@org.springframework.stereotype.Component("adminSkillController")
public class SkillController {

    private final ModuleAccessManager moduleAccessManager;
    private final SkillRepository skillRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listSkills() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<Skill> skills = skillRepository.findByCompanyAndArchivedFalse(
                companyModuleMapper.getCompany(), Sort.by("position").ascending());

        List<SkillResponse> skillResponses = skills.stream()
                .map(this::mapToResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("skills", skillResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{skillKey}")
    public ResponseEntity<SkillResponse> getSkill(@PathVariable String skillKey) {
        moduleAccessManager.authenticateModule();

        return skillRepository.findBySkillKey(skillKey)
                .map(skill -> ResponseEntity.ok(mapToResponse(skill)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createSkill(@Valid @RequestBody SkillRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Skill skill = Skill.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .position(request.getPosition() != null ? request.getPosition() : 0)
                .archived(false)
                .build();

        skill = skillRepository.save(skill);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Skill created successfully", "skillKey", skill.getSkillKey()));
    }

    @PutMapping("/{skillKey}")
    public ResponseEntity<?> updateSkill(
            @PathVariable String skillKey,
            @Valid @RequestBody SkillRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Skill skill = skillRepository.findBySkillKey(skillKey)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            skill.setName(request.getName());
        }
        if (request.getDescription() != null) {
            skill.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            skill.setIcon(request.getIcon());
        }
        if (request.getPosition() != null) {
            skill.setPosition(request.getPosition());
        }

        skill = skillRepository.save(skill);

        return ResponseEntity.ok(Map.of("message", "Skill updated successfully"));
    }

    @DeleteMapping("/{skillKey}")
    public ResponseEntity<?> deleteSkill(@PathVariable String skillKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Skill skill = skillRepository.findBySkillKey(skillKey)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        skill.setArchived(true);
        skillRepository.save(skill);

        return ResponseEntity.ok(Map.of("message", "Skill deleted successfully"));
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

