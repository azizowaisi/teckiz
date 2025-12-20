package com.teckiz.controller.admin.education;

import com.teckiz.dto.ProgramCourseRequest;
import com.teckiz.dto.ProgramCourseResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramCourse;
import com.teckiz.entity.ProgramLevel;
import com.teckiz.repository.ProgramCourseRepository;
import com.teckiz.repository.ProgramLevelRepository;
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
@RequestMapping("/education/admin/program-courses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ProgramCourseController {

    private final ModuleAccessManager moduleAccessManager;
    private final ProgramCourseRepository programCourseRepository;
    private final ProgramLevelRepository programLevelRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Long programLevelId) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());
        Page<ProgramCourse> programCourses;

        if (programLevelId != null) {
            ProgramLevel programLevel = programLevelRepository.findById(programLevelId)
                    .orElseThrow(() -> new RuntimeException("Program level not found"));
            if (published != null && published) {
                programCourses = programCourseRepository.findByCompanyAndProgramLevelAndPublishedTrueAndArchivedFalse(
                        companyModuleMapper.getCompany(), programLevel, pageable);
            } else {
                programCourses = programCourseRepository.findByCompanyAndProgramLevel(
                        companyModuleMapper.getCompany(), programLevel, pageable);
            }
        } else if (published != null && published) {
            programCourses = programCourseRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        } else {
            programCourses = programCourseRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<ProgramCourseResponse> responses = programCourses.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("programCourses", responses);
        response.put("totalPages", programCourses.getTotalPages());
        response.put("totalElements", programCourses.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{courseKey}")
    public ResponseEntity<ProgramCourseResponse> getProgramCourse(@PathVariable String courseKey) {
        moduleAccessManager.authenticateModule();

        return programCourseRepository.findByCourseKey(courseKey)
                .map(course -> ResponseEntity.ok(mapToResponse(course)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProgramCourse(@Valid @RequestBody ProgramCourseRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramCourse.ProgramCourseBuilder builder = ProgramCourse.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .credits(request.getCredits())
                .position(request.getPosition())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .archived(false);

        if (request.getProgramLevelId() != null) {
            ProgramLevel programLevel = programLevelRepository.findById(request.getProgramLevelId())
                    .orElseThrow(() -> new RuntimeException("Program level not found"));
            builder.programLevel(programLevel);
        }

        ProgramCourse programCourse = programCourseRepository.save(builder.build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Program course created successfully", "courseKey", programCourse.getCourseKey()));
    }

    @PutMapping("/{courseKey}")
    public ResponseEntity<?> updateProgramCourse(
            @PathVariable String courseKey,
            @Valid @RequestBody ProgramCourseRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramCourse programCourse = programCourseRepository.findByCourseKey(courseKey)
                .orElseThrow(() -> new RuntimeException("Program course not found"));

        if (!programCourse.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            programCourse.setName(request.getName());
        }
        if (request.getCode() != null) {
            programCourse.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            programCourse.setDescription(request.getDescription());
        }
        if (request.getCredits() != null) {
            programCourse.setCredits(request.getCredits());
        }
        if (request.getPosition() != null) {
            programCourse.setPosition(request.getPosition());
        }
        if (request.getPublished() != null) {
            programCourse.setPublished(request.getPublished());
        }
        if (request.getProgramLevelId() != null) {
            ProgramLevel programLevel = programLevelRepository.findById(request.getProgramLevelId())
                    .orElseThrow(() -> new RuntimeException("Program level not found"));
            programCourse.setProgramLevel(programLevel);
        }

        programCourseRepository.save(programCourse);

        return ResponseEntity.ok(Map.of("message", "Program course updated successfully"));
    }

    @DeleteMapping("/{courseKey}")
    public ResponseEntity<?> deleteProgramCourse(@PathVariable String courseKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramCourse programCourse = programCourseRepository.findByCourseKey(courseKey)
                .orElseThrow(() -> new RuntimeException("Program course not found"));

        if (!programCourse.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        programCourse.setArchived(true);
        programCourseRepository.save(programCourse);

        return ResponseEntity.ok(Map.of("message", "Program course deleted successfully"));
    }

    private ProgramCourseResponse mapToResponse(ProgramCourse programCourse) {
        return ProgramCourseResponse.builder()
                .id(programCourse.getId())
                .courseKey(programCourse.getCourseKey())
                .name(programCourse.getName())
                .code(programCourse.getCode())
                .description(programCourse.getDescription())
                .credits(programCourse.getCredits())
                .position(programCourse.getPosition())
                .published(programCourse.getPublished())
                .archived(programCourse.getArchived())
                .programLevelId(programCourse.getProgramLevel() != null ? programCourse.getProgramLevel().getId() : null)
                .programLevelName(programCourse.getProgramLevel() != null ? programCourse.getProgramLevel().getName() : null)
                .companyId(programCourse.getCompany().getId())
                .companyName(programCourse.getCompany().getName())
                .createdAt(programCourse.getCreatedAt())
                .updatedAt(programCourse.getUpdatedAt())
                .build();
    }
}

