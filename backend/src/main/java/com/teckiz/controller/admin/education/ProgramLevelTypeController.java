package com.teckiz.controller.admin.education;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.ProgramLevelTypeRequest;
import com.teckiz.dto.ProgramLevelTypeResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramLevelType;
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
@RequestMapping("/education/admin/program-level-types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ProgramLevelTypeController {

    private final ModuleAccessManager moduleAccessManager;
    private final ProgramLevelTypeRepository programLevelTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramLevelTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProgramLevelType> programLevelTypes = programLevelTypeRepository.findByCompany(
                companyModuleMapper.getCompany(), pageable);

        List<ProgramLevelTypeResponse> responses = programLevelTypes.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("programLevelTypes", responses);
        response.put("totalPages", programLevelTypes.getTotalPages());
        response.put("totalElements", programLevelTypes.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{typeKey}")
    public ResponseEntity<ProgramLevelTypeResponse> getProgramLevelType(@PathVariable String typeKey) {
        moduleAccessManager.authenticateModule();

        return programLevelTypeRepository.findByTypeKey(typeKey)
                .map(type -> ResponseEntity.ok(mapToResponse(type)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProgramLevelType(@Valid @RequestBody ProgramLevelTypeRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramLevelType programLevelType = ProgramLevelType.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        programLevelType = programLevelTypeRepository.save(programLevelType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Program level type created successfully", "typeKey", programLevelType.getTypeKey()));
    }

    @PutMapping("/{typeKey}")
    public ResponseEntity<?> updateProgramLevelType(
            @PathVariable String typeKey,
            @Valid @RequestBody ProgramLevelTypeRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramLevelType programLevelType = programLevelTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Program level type not found"));

        if (!programLevelType.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            programLevelType.setName(request.getName());
        }
        if (request.getDescription() != null) {
            programLevelType.setDescription(request.getDescription());
        }

        programLevelTypeRepository.save(programLevelType);

        return ResponseEntity.ok(Map.of("message", "Program level type updated successfully"));
    }

    @DeleteMapping("/{typeKey}")
    public ResponseEntity<?> deleteProgramLevelType(@PathVariable String typeKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramLevelType programLevelType = programLevelTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Program level type not found"));

        if (!programLevelType.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        programLevelTypeRepository.delete(programLevelType);

        return ResponseEntity.ok(Map.of("message", "Program level type deleted successfully"));
    }

    private ProgramLevelTypeResponse mapToResponse(ProgramLevelType programLevelType) {
        return ProgramLevelTypeResponse.builder()
                .id(programLevelType.getId())
                .typeKey(programLevelType.getTypeKey())
                .name(programLevelType.getName())
                .description(programLevelType.getDescription())
                .companyId(programLevelType.getCompany().getId())
                .companyName(programLevelType.getCompany().getName())
                .createdAt(programLevelType.getCreatedAt())
                .updatedAt(programLevelType.getUpdatedAt())
                .build();
    }
}

