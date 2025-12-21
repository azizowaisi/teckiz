package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.ProgramCourseResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramCourse;
import com.teckiz.entity.ProgramLevel;
import com.teckiz.repository.ProgramCourseRepository;
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
@Tag(name = "Public - ProgramCourse", description = "Public API endpoints for ProgramCourse")
@RequestMapping("/public/program-courses")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicProgramCourseController")
public class ProgramCourseController {

    private final WebsiteManager websiteManager;
    private final ProgramCourseRepository programCourseRepository;
    private final ProgramLevelRepository programLevelRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProgramCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long programLevelId) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());
        Page<ProgramCourse> programCourses;

        if (programLevelId != null) {
            ProgramLevel programLevel = programLevelRepository.findById(programLevelId)
                    .orElseThrow(() -> new RuntimeException("Program level not found"));
            programCourses = programCourseRepository.findByCompanyAndProgramLevelAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), programLevel, pageable);
        } else {
            programCourses = programCourseRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
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
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        ProgramCourse programCourse = programCourseRepository.findByCourseKey(courseKey)
                .orElseThrow(() -> new RuntimeException("Program course not found"));

        if (!programCourse.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(programCourse.getPublished()) || Boolean.TRUE.equals(programCourse.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(programCourse));
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

