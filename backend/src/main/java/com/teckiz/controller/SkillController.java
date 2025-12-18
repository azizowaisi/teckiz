package com.teckiz.controller.admin.education;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Skill;
import com.teckiz.repository.SkillRepository;
import com.teckiz.service.ModuleAccessManager;
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
public class SkillController {

    private final ModuleAccessManager moduleAccessManager;
    private final SkillRepository skillRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listSkills() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<Skill> skills = skillRepository.findByCompanyAndArchivedFalse(
                companyModuleMapper.getCompany(), Sort.by("position").ascending());

        List<Map<String, Object>> skillResponses = skills.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("skills", skillResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{skillKey}")
    public ResponseEntity<Map<String, Object>> getSkill(@PathVariable String skillKey) {
        moduleAccessManager.authenticateModule();

        return skillRepository.findBySkillKey(skillKey)
                .map(skill -> ResponseEntity.ok(mapToResponse(skill)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createSkill(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        Skill skill = Skill.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .description((String) request.get("description"))
                .icon((String) request.get("icon"))
                .position(request.get("position") != null ?
                        ((Number) request.get("position")).intValue() : 0)
                .archived(false)
                .build();

        skill = skillRepository.save(skill);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Skill created successfully", "skillKey", skill.getSkillKey()));
    }

    @PutMapping("/{skillKey}")
    public ResponseEntity<?> updateSkill(
            @PathVariable String skillKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Skill skill = skillRepository.findBySkillKey(skillKey)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            skill.setName((String) request.get("name"));
        }
        if (request.get("description") != null) {
            skill.setDescription((String) request.get("description"));
        }
        if (request.get("icon") != null) {
            skill.setIcon((String) request.get("icon"));
        }
        if (request.get("position") != null) {
            skill.setPosition(((Number) request.get("position")).intValue());
        }
        if (request.get("archived") != null) {
            skill.setArchived((Boolean) request.get("archived"));
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

    private Map<String, Object> mapToResponse(Skill skill) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", skill.getId());
        response.put("skillKey", skill.getSkillKey());
        response.put("name", skill.getName());
        response.put("description", skill.getDescription());
        response.put("icon", skill.getIcon());
        response.put("position", skill.getPosition());
        response.put("archived", skill.getArchived());
        response.put("createdAt", skill.getCreatedAt());
        response.put("updatedAt", skill.getUpdatedAt());
        return response;
    }
}

