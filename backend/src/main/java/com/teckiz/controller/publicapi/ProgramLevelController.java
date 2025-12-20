package com.teckiz.controller.publicapi;

import com.teckiz.dto.ProgramLevelResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramLevel;
import com.teckiz.repository.ProgramLevelRepository;
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
@RequestMapping("/public/program-levels")
@RequiredArgsConstructor
public class ProgramLevelController {

    private final WebsiteManager websiteManager;
    private final ProgramLevelRepository programLevelRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramLevels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());
        Page<ProgramLevel> programLevels = programLevelRepository.findByCompanyAndActiveTrueAndArchivedFalse(
                companyModuleMapper.getCompany(), pageable);

        List<ProgramLevelResponse> responses = programLevels.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("programLevels", responses);
        response.put("totalPages", programLevels.getTotalPages());
        response.put("totalElements", programLevels.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{levelKey}")
    public ResponseEntity<ProgramLevelResponse> getProgramLevel(@PathVariable String levelKey) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        ProgramLevel programLevel = programLevelRepository.findByLevelKey(levelKey)
                .orElseThrow(() -> new RuntimeException("Program level not found"));

        if (!programLevel.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(programLevel.getActive()) || Boolean.TRUE.equals(programLevel.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(programLevel));
    }

    private ProgramLevelResponse mapToResponse(ProgramLevel programLevel) {
        return ProgramLevelResponse.builder()
                .id(programLevel.getId())
                .levelKey(programLevel.getLevelKey())
                .name(programLevel.getName())
                .description(programLevel.getDescription())
                .position(programLevel.getPosition())
                .active(programLevel.getActive())
                .archived(programLevel.getArchived())
                .programLevelTypeId(programLevel.getProgramLevelType() != null ? programLevel.getProgramLevelType().getId() : null)
                .programLevelTypeName(programLevel.getProgramLevelType() != null ? programLevel.getProgramLevelType().getName() : null)
                .companyId(programLevel.getCompany().getId())
                .companyName(programLevel.getCompany().getName())
                .createdAt(programLevel.getCreatedAt())
                .updatedAt(programLevel.getUpdatedAt())
                .build();
    }
}

