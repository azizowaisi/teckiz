package com.teckiz.controller.admin.education;

import com.teckiz.dto.ProgramTermRequest;
import com.teckiz.dto.ProgramTermResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramTerm;
import com.teckiz.repository.ProgramTermRepository;
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
@RequestMapping("/education/admin/program-terms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ProgramTermController {

    private final ModuleAccessManager moduleAccessManager;
    private final ProgramTermRepository programTermRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramTerms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<ProgramTerm> programTerms;

        if (active != null && active) {
            programTerms = programTermRepository.findByCompanyAndActiveTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        } else {
            programTerms = programTermRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<ProgramTermResponse> responses = programTerms.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("programTerms", responses);
        response.put("totalPages", programTerms.getTotalPages());
        response.put("totalElements", programTerms.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{termKey}")
    public ResponseEntity<ProgramTermResponse> getProgramTerm(@PathVariable String termKey) {
        moduleAccessManager.authenticateModule();

        return programTermRepository.findByTermKey(termKey)
                .map(term -> ResponseEntity.ok(mapToResponse(term)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProgramTerm(@Valid @RequestBody ProgramTermRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramTerm programTerm = ProgramTerm.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() != null ? request.getActive() : true)
                .archived(false)
                .build();

        programTerm = programTermRepository.save(programTerm);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Program term created successfully", "termKey", programTerm.getTermKey()));
    }

    @PutMapping("/{termKey}")
    public ResponseEntity<?> updateProgramTerm(
            @PathVariable String termKey,
            @Valid @RequestBody ProgramTermRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramTerm programTerm = programTermRepository.findByTermKey(termKey)
                .orElseThrow(() -> new RuntimeException("Program term not found"));

        if (!programTerm.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            programTerm.setName(request.getName());
        }
        if (request.getDescription() != null) {
            programTerm.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            programTerm.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            programTerm.setEndDate(request.getEndDate());
        }
        if (request.getActive() != null) {
            programTerm.setActive(request.getActive());
        }

        programTermRepository.save(programTerm);

        return ResponseEntity.ok(Map.of("message", "Program term updated successfully"));
    }

    @DeleteMapping("/{termKey}")
    public ResponseEntity<?> deleteProgramTerm(@PathVariable String termKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramTerm programTerm = programTermRepository.findByTermKey(termKey)
                .orElseThrow(() -> new RuntimeException("Program term not found"));

        if (!programTerm.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        programTerm.setArchived(true);
        programTermRepository.save(programTerm);

        return ResponseEntity.ok(Map.of("message", "Program term deleted successfully"));
    }

    private ProgramTermResponse mapToResponse(ProgramTerm programTerm) {
        return ProgramTermResponse.builder()
                .id(programTerm.getId())
                .termKey(programTerm.getTermKey())
                .name(programTerm.getName())
                .description(programTerm.getDescription())
                .startDate(programTerm.getStartDate())
                .endDate(programTerm.getEndDate())
                .active(programTerm.getActive())
                .archived(programTerm.getArchived())
                .companyId(programTerm.getCompany().getId())
                .companyName(programTerm.getCompany().getName())
                .createdAt(programTerm.getCreatedAt())
                .updatedAt(programTerm.getUpdatedAt())
                .build();
    }
}

