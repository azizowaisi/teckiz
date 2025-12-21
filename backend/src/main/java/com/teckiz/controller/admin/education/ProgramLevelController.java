package com.teckiz.controller.admin.education;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.ProgramLevelRequest;
import com.teckiz.dto.ProgramLevelResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramLevel;
import com.teckiz.entity.ProgramLevelType;
import com.teckiz.repository.ProgramLevelRepository;
import com.teckiz.repository.ProgramLevelTypeRepository;
import com.teckiz.service.ModuleAccessManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/education/admin/program-levels")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ProgramLevelController {

    private final ModuleAccessManager moduleAccessManager;
    private final ProgramLevelRepository programLevelRepository;
    private final ProgramLevelTypeRepository programLevelTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramLevels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());
        Page<ProgramLevel> programLevels;

        if (active != null && active) {
            programLevels = programLevelRepository.findByCompanyAndActiveTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        } else {
            programLevels = programLevelRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

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
        moduleAccessManager.authenticateModule();

        return programLevelRepository.findByLevelKey(levelKey)
                .map(level -> ResponseEntity.ok(mapToResponse(level)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProgramLevel(@Valid @RequestBody ProgramLevelRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramLevel.ProgramLevelBuilder builder = ProgramLevel.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .position(request.getPosition())
                .active(request.getActive() != null ? request.getActive() : true)
                .archived(false);

        if (request.getProgramLevelTypeId() != null) {
            ProgramLevelType programLevelType = programLevelTypeRepository.findById(request.getProgramLevelTypeId())
                    .orElseThrow(() -> new RuntimeException("Program level type not found"));
            builder.programLevelType(programLevelType);
        }

        ProgramLevel programLevel = programLevelRepository.save(builder.build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Program level created successfully", "levelKey", programLevel.getLevelKey()));
    }

    @PutMapping("/{levelKey}")
    public ResponseEntity<?> updateProgramLevel(
            @PathVariable String levelKey,
            @Valid @RequestBody ProgramLevelRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramLevel programLevel = programLevelRepository.findByLevelKey(levelKey)
                .orElseThrow(() -> new RuntimeException("Program level not found"));

        if (!programLevel.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            programLevel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            programLevel.setDescription(request.getDescription());
        }
        if (request.getPosition() != null) {
            programLevel.setPosition(request.getPosition());
        }
        if (request.getActive() != null) {
            programLevel.setActive(request.getActive());
        }
        if (request.getProgramLevelTypeId() != null) {
            ProgramLevelType programLevelType = programLevelTypeRepository.findById(request.getProgramLevelTypeId())
                    .orElseThrow(() -> new RuntimeException("Program level type not found"));
            programLevel.setProgramLevelType(programLevelType);
        }

        programLevelRepository.save(programLevel);

        return ResponseEntity.ok(Map.of("message", "Program level updated successfully"));
    }

    @DeleteMapping("/{levelKey}")
    public ResponseEntity<?> deleteProgramLevel(@PathVariable String levelKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramLevel programLevel = programLevelRepository.findByLevelKey(levelKey)
                .orElseThrow(() -> new RuntimeException("Program level not found"));

        if (!programLevel.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        programLevel.setArchived(true);
        programLevelRepository.save(programLevel);

        return ResponseEntity.ok(Map.of("message", "Program level deleted successfully"));
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

