package com.teckiz.controller.admin.superadmin;

import com.teckiz.dto.AddModuleToCompanyRequest;
import com.teckiz.dto.CompanyModuleMapperResponse;
import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Module;
import com.teckiz.repository.CompanyModuleMapperRepository;
import com.teckiz.repository.CompanyRepository;
import com.teckiz.repository.ModuleRepository;
import com.teckiz.service.ModuleHelperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/superadmin/company/{companyKey}/modules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyModuleController {

    private final CompanyRepository companyRepository;
    private final ModuleRepository moduleRepository;
    private final CompanyModuleMapperRepository companyModuleMapperRepository;
    private final ModuleHelperService moduleHelperService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCompanyModules(@PathVariable String companyKey) {
        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        List<CompanyModuleMapper> modules = companyModuleMapperRepository.findByCompany(company);

        List<CompanyModuleMapperResponse> moduleResponses = modules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("company", Map.of(
                "id", company.getId(),
                "name", company.getName(),
                "companyKey", company.getCompanyKey()
        ));
        response.put("modules", moduleResponses);
        response.put("leftTab", "modules");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addModuleToCompany(
            @PathVariable String companyKey,
            @RequestBody AddModuleToCompanyRequest request) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Module module = moduleRepository.findByModuleKeyAndArchivedFalse(request.getModuleKey())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // Check if module already exists for company
        if (companyModuleMapperRepository.findByCompanyAndModuleAndArchivedFalse(company, module).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Module already exists for this company"));
        }

        CompanyModuleMapper companyModuleMapper = CompanyModuleMapper.builder()
                .company(company)
                .module(module)
                .live(true)
                .archived(false)
                .build();

        companyModuleMapper = companyModuleMapperRepository.save(companyModuleMapper);

        // Add default menus for the module
        moduleHelperService.addMenuToModule(companyModuleMapper);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(companyModuleMapper));
    }

    @DeleteMapping("/{moduleMapperKey}")
    public ResponseEntity<?> removeModuleFromCompany(
            @PathVariable String companyKey,
            @PathVariable String moduleMapperKey) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyModuleMapper companyModuleMapper = companyModuleMapperRepository
                .findByModuleMapperKeyAndArchivedFalse(moduleMapperKey)
                .orElseThrow(() -> new RuntimeException("Module mapper not found"));

        if (!companyModuleMapper.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Module mapper does not belong to this company"));
        }

        companyModuleMapper.setArchived(true);
        companyModuleMapper.setLive(false);
        companyModuleMapperRepository.save(companyModuleMapper);

        return ResponseEntity.ok(Map.of("message", "Module removed from company successfully"));
    }

    private CompanyModuleMapperResponse mapToResponse(CompanyModuleMapper mapper) {
        return CompanyModuleMapperResponse.builder()
                .id(mapper.getId())
                .moduleMapperKey(mapper.getModuleMapperKey())
                .directory(mapper.getDirectory())
                .email(mapper.getEmail())
                .host(mapper.getHost())
                .live(mapper.getLive())
                .master(mapper.getMaster())
                .archived(mapper.getArchived())
                .header(mapper.getHeader())
                .companyId(mapper.getCompany().getId())
                .companyName(mapper.getCompany().getName())
                .moduleId(mapper.getModule().getId())
                .moduleName(mapper.getModule().getName())
                .moduleType(mapper.getModule().getType())
                .createdAt(mapper.getCreatedAt())
                .updatedAt(mapper.getUpdatedAt())
                .build();
    }
}

