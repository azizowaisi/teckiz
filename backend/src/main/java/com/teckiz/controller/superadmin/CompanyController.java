package com.teckiz.controller.superadmin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.teckiz.dto.CompanyRequest;
import com.teckiz.dto.CompanyResponse;
import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.repository.CompanyModuleMapperRepository;
import com.teckiz.repository.CompanyRepository;
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
@RequestMapping("/superadmin/company")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "SuperAdmin - Companies", description = "SuperAdmin endpoints for managing companies")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final CompanyModuleMapperRepository companyModuleMapperRepository;

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List all companies", description = "Get list of all active companies")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Companies retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAllCompanies() {
        List<Company> companies = companyRepository.findAllActiveCompanies();
        List<CompanyResponse> companyResponses = companies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("companies", companyResponses);
        response.put("leftTab", "companies");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyKey}")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable String companyKey) {
        return companyRepository.findByCompanyKey(companyKey)
                .map(company -> ResponseEntity.ok(mapToResponse(company)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCompany(@RequestBody CompanyRequest request) {
        // Check if company name already exists
        if (companyRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This name is already used"));
        }

        Company company = Company.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .timeZone(request.getTimeZone())
                .stripeId(request.getStripeId())
                .active(request.getIsActive() != null ? request.getIsActive() : true)
                .lang(request.getLanguage())
                .build();

        company = companyRepository.save(company);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(company));
    }

    @PutMapping("/{companyKey}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update company", description = "Update an existing company")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<?> updateCompany(
            @PathVariable String companyKey,
            @RequestBody CompanyRequest request) {

        final Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if name is already used by another company
        companyRepository.findByName(request.getName())
                .ifPresent(otherCompany -> {
                    if (!otherCompany.getCompanyKey().equals(company.getCompanyKey())) {
                        throw new RuntimeException("This name is already used");
                    }
                });

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setCountry(request.getCountry());
        company.setTimeZone(request.getTimeZone());
        company.setStripeId(request.getStripeId());
        if (request.getIsActive() != null) {
            company.setActive(request.getIsActive());
        }
        company.setLang(request.getLanguage());

        // Update module mappers based on company active status
        List<CompanyModuleMapper> moduleMappers = companyModuleMapperRepository.findByCompany(company);
        final Boolean isActive = company.getActive();
        for (CompanyModuleMapper mapper : moduleMappers) {
            if (Boolean.TRUE.equals(isActive)) {
                mapper.setLive(true);
                mapper.setArchived(false);
            } else {
                mapper.setLive(false);
                mapper.setArchived(true);
            }
            companyModuleMapperRepository.save(mapper);
        }

        Company savedCompany = companyRepository.save(company);

        return ResponseEntity.ok(mapToResponse(savedCompany));
    }

    @DeleteMapping("/{companyKey}")
    public ResponseEntity<?> deleteCompany(@PathVariable String companyKey) {
        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (Boolean.TRUE.equals(company.getActive())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Company is active, de-activate first!"));
        }

        company.setArchived(true);
        companyRepository.save(company);

        return ResponseEntity.ok(Map.of("message", "Company archived successfully"));
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .companyKey(company.getCompanyKey())
                .name(company.getName())
                .slug(company.getSlug())
                .description(company.getDescription())
                .address(company.getAddress())
                .city(company.getCity())
                .country(company.getCountry())
                .timeZone(company.getTimeZone())
                .active(company.getActive())
                .archived(company.getArchived())
                .email(company.getEmail())
                .phone(company.getPhone())
                .lang(company.getLang())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private String generateSlug(String name) {
        if (name == null) {
            return null;
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

