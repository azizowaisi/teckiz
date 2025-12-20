package com.teckiz.controller.admin.education;

import com.teckiz.dto.ProgramClassRequest;
import com.teckiz.dto.ProgramClassResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramClass;
import com.teckiz.entity.ProgramCourse;
import com.teckiz.entity.ProgramTerm;
import com.teckiz.repository.ProgramClassRepository;
import com.teckiz.repository.ProgramCourseRepository;
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
@RequestMapping("/education/admin/program-classes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ProgramClassController {

    private final ModuleAccessManager moduleAccessManager;
    private final ProgramClassRepository programClassRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final ProgramTermRepository programTermRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Long programCourseId,
            @RequestParam(required = false) Long programTermId) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProgramClass> programClasses;

        if (programCourseId != null && programTermId != null) {
            ProgramCourse programCourse = programCourseRepository.findById(programCourseId)
                    .orElseThrow(() -> new RuntimeException("Program course not found"));
            ProgramTerm programTerm = programTermRepository.findById(programTermId)
                    .orElseThrow(() -> new RuntimeException("Program term not found"));
            List<ProgramClass> classes = programClassRepository.findByCompanyAndProgramCourseAndProgramTerm(
                    companyModuleMapper.getCompany(), programCourse, programTerm);
            // Convert to page manually for consistency
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), classes.size());
            List<ProgramClass> pagedClasses = classes.subList(start, end);
            programClasses = new org.springframework.data.domain.PageImpl<>(pagedClasses, pageable, classes.size());
        } else if (programCourseId != null) {
            ProgramCourse programCourse = programCourseRepository.findById(programCourseId)
                    .orElseThrow(() -> new RuntimeException("Program course not found"));
            List<ProgramClass> classes = programClassRepository.findByCompanyAndProgramCourse(
                    companyModuleMapper.getCompany(), programCourse);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), classes.size());
            List<ProgramClass> pagedClasses = classes.subList(start, end);
            programClasses = new org.springframework.data.domain.PageImpl<>(pagedClasses, pageable, classes.size());
        } else if (programTermId != null) {
            ProgramTerm programTerm = programTermRepository.findById(programTermId)
                    .orElseThrow(() -> new RuntimeException("Program term not found"));
            List<ProgramClass> classes = programClassRepository.findByCompanyAndProgramTerm(
                    companyModuleMapper.getCompany(), programTerm);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), classes.size());
            List<ProgramClass> pagedClasses = classes.subList(start, end);
            programClasses = new org.springframework.data.domain.PageImpl<>(pagedClasses, pageable, classes.size());
        } else if (published != null && published) {
            programClasses = programClassRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        } else {
            programClasses = programClassRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<ProgramClassResponse> responses = programClasses.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("programClasses", responses);
        response.put("totalPages", programClasses.getTotalPages());
        response.put("totalElements", programClasses.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classKey}")
    public ResponseEntity<ProgramClassResponse> getProgramClass(@PathVariable String classKey) {
        moduleAccessManager.authenticateModule();

        return programClassRepository.findByClassKey(classKey)
                .map(clazz -> ResponseEntity.ok(mapToResponse(clazz)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProgramClass(@Valid @RequestBody ProgramClassRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramClass.ProgramClassBuilder builder = ProgramClass.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .room(request.getRoom())
                .schedule(request.getSchedule())
                .instructor(request.getInstructor())
                .capacity(request.getCapacity())
                .enrolled(0)
                .published(request.getPublished() != null ? request.getPublished() : false)
                .archived(false);

        if (request.getProgramCourseId() != null) {
            ProgramCourse programCourse = programCourseRepository.findById(request.getProgramCourseId())
                    .orElseThrow(() -> new RuntimeException("Program course not found"));
            builder.programCourse(programCourse);
        }

        if (request.getProgramTermId() != null) {
            ProgramTerm programTerm = programTermRepository.findById(request.getProgramTermId())
                    .orElseThrow(() -> new RuntimeException("Program term not found"));
            builder.programTerm(programTerm);
        }

        ProgramClass programClass = programClassRepository.save(builder.build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Program class created successfully", "classKey", programClass.getClassKey()));
    }

    @PutMapping("/{classKey}")
    public ResponseEntity<?> updateProgramClass(
            @PathVariable String classKey,
            @Valid @RequestBody ProgramClassRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramClass programClass = programClassRepository.findByClassKey(classKey)
                .orElseThrow(() -> new RuntimeException("Program class not found"));

        if (!programClass.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            programClass.setName(request.getName());
        }
        if (request.getDescription() != null) {
            programClass.setDescription(request.getDescription());
        }
        if (request.getRoom() != null) {
            programClass.setRoom(request.getRoom());
        }
        if (request.getSchedule() != null) {
            programClass.setSchedule(request.getSchedule());
        }
        if (request.getInstructor() != null) {
            programClass.setInstructor(request.getInstructor());
        }
        if (request.getCapacity() != null) {
            programClass.setCapacity(request.getCapacity());
        }
        if (request.getPublished() != null) {
            programClass.setPublished(request.getPublished());
        }
        if (request.getProgramCourseId() != null) {
            ProgramCourse programCourse = programCourseRepository.findById(request.getProgramCourseId())
                    .orElseThrow(() -> new RuntimeException("Program course not found"));
            programClass.setProgramCourse(programCourse);
        }
        if (request.getProgramTermId() != null) {
            ProgramTerm programTerm = programTermRepository.findById(request.getProgramTermId())
                    .orElseThrow(() -> new RuntimeException("Program term not found"));
            programClass.setProgramTerm(programTerm);
        }

        programClassRepository.save(programClass);

        return ResponseEntity.ok(Map.of("message", "Program class updated successfully"));
    }

    @DeleteMapping("/{classKey}")
    public ResponseEntity<?> deleteProgramClass(@PathVariable String classKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ProgramClass programClass = programClassRepository.findByClassKey(classKey)
                .orElseThrow(() -> new RuntimeException("Program class not found"));

        if (!programClass.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        programClass.setArchived(true);
        programClassRepository.save(programClass);

        return ResponseEntity.ok(Map.of("message", "Program class deleted successfully"));
    }

    private ProgramClassResponse mapToResponse(ProgramClass programClass) {
        return ProgramClassResponse.builder()
                .id(programClass.getId())
                .classKey(programClass.getClassKey())
                .name(programClass.getName())
                .description(programClass.getDescription())
                .room(programClass.getRoom())
                .schedule(programClass.getSchedule())
                .instructor(programClass.getInstructor())
                .capacity(programClass.getCapacity())
                .enrolled(programClass.getEnrolled())
                .published(programClass.getPublished())
                .archived(programClass.getArchived())
                .programCourseId(programClass.getProgramCourse() != null ? programClass.getProgramCourse().getId() : null)
                .programCourseName(programClass.getProgramCourse() != null ? programClass.getProgramCourse().getName() : null)
                .programTermId(programClass.getProgramTerm() != null ? programClass.getProgramTerm().getId() : null)
                .programTermName(programClass.getProgramTerm() != null ? programClass.getProgramTerm().getName() : null)
                .companyId(programClass.getCompany().getId())
                .companyName(programClass.getCompany().getName())
                .createdAt(programClass.getCreatedAt())
                .updatedAt(programClass.getUpdatedAt())
                .build();
    }
}

