package com.teckiz.controller.admin.superadmin;

import com.teckiz.dto.AddRoleToCompanyRequest;
import com.teckiz.dto.CompanyRoleMapperResponse;
import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyRoleMapper;
import com.teckiz.entity.Role;
import com.teckiz.repository.CompanyRepository;
import com.teckiz.repository.CompanyRoleMapperRepository;
import com.teckiz.repository.RoleRepository;
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
@RequestMapping("/superadmin/company/{companyKey}/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyRoleController {

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final CompanyRoleMapperRepository companyRoleMapperRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCompanyRoles(@PathVariable String companyKey) {
        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        List<CompanyRoleMapper> roles = companyRoleMapperRepository.findByCompanyAndArchivedFalse(company);

        List<CompanyRoleMapperResponse> roleResponses = roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("company", Map.of(
                "id", company.getId(),
                "name", company.getName(),
                "companyKey", company.getCompanyKey()
        ));
        response.put("roles", roleResponses);
        response.put("leftTab", "roles");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addRoleToCompany(
            @PathVariable String companyKey,
            @RequestBody AddRoleToCompanyRequest request) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Role role = roleRepository.findByRoleKey(request.getRoleKey())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Check if role already exists for company
        if (companyRoleMapperRepository.findByCompanyAndRoleAndArchivedFalse(company, role).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Role already exists for this company"));
        }

        CompanyRoleMapper companyRoleMapper = CompanyRoleMapper.builder()
                .company(company)
                .role(role)
                .archived(false)
                .build();

        companyRoleMapperRepository.save(companyRoleMapper);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(companyRoleMapper));
    }

    @DeleteMapping("/{companyRoleKey}")
    public ResponseEntity<?> removeRoleFromCompany(
            @PathVariable String companyKey,
            @PathVariable String companyRoleKey) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyRoleMapper companyRoleMapper = companyRoleMapperRepository
                .findByCompanyRoleKeyAndArchivedFalse(companyRoleKey)
                .orElseThrow(() -> new RuntimeException("Company role mapper not found"));

        if (!companyRoleMapper.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Role mapper does not belong to this company"));
        }

        companyRoleMapper.setArchived(true);
        companyRoleMapperRepository.save(companyRoleMapper);

        return ResponseEntity.ok(Map.of("message", "Role removed from company successfully"));
    }

    private CompanyRoleMapperResponse mapToResponse(CompanyRoleMapper mapper) {
        return CompanyRoleMapperResponse.builder()
                .id(mapper.getId())
                .companyRoleKey(mapper.getCompanyRoleKey())
                .archived(mapper.getArchived())
                .companyId(mapper.getCompany().getId())
                .companyName(mapper.getCompany().getName())
                .roleId(mapper.getRole().getId())
                .roleName(mapper.getRole().getName())
                .roleType(mapper.getRole().getRole())
                .build();
    }
}

