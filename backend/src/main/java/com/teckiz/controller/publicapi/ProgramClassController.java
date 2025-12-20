package com.teckiz.controller.publicapi;

import com.teckiz.dto.ProgramClassResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramClass;
import com.teckiz.entity.ProgramCourse;
import com.teckiz.entity.ProgramTerm;
import com.teckiz.repository.ProgramClassRepository;
import com.teckiz.repository.ProgramCourseRepository;
import com.teckiz.repository.ProgramTermRepository;
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
@RequestMapping("/public/program-classes")
@RequiredArgsConstructor
public class ProgramClassController {

    private final WebsiteManager websiteManager;
    private final ProgramClassRepository programClassRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final ProgramTermRepository programTermRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long programCourseId,
            @RequestParam(required = false) Long programTermId) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProgramClass> programClasses;

        if (programCourseId != null && programTermId != null) {
            ProgramCourse programCourse = programCourseRepository.findById(programCourseId)
                    .orElseThrow(() -> new RuntimeException("Program course not found"));
            ProgramTerm programTerm = programTermRepository.findById(programTermId)
                    .orElseThrow(() -> new RuntimeException("Program term not found"));
            List<ProgramClass> classes = programClassRepository.findByCompanyAndProgramCourseAndProgramTerm(
                    companyModuleMapper.getCompany(), programCourse, programTerm);
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
        } else {
            programClasses = programClassRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
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
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        ProgramClass programClass = programClassRepository.findByClassKey(classKey)
                .orElseThrow(() -> new RuntimeException("Program class not found"));

        if (!programClass.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(programClass.getPublished()) || Boolean.TRUE.equals(programClass.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(programClass));
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

