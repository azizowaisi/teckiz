package com.teckiz.controller.superadmin;

import com.teckiz.dto.AddUserToCompanyRequest;
import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyRoleMapper;
import com.teckiz.entity.Role;
import com.teckiz.entity.User;
import com.teckiz.entity.UserCompanyRole;
import com.teckiz.repository.CompanyRepository;
import com.teckiz.repository.CompanyRoleMapperRepository;
import com.teckiz.repository.UserCompanyRoleRepository;
import com.teckiz.repository.UserRepository;
import com.teckiz.service.UserHelperService;
import com.teckiz.util.UtilHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/superadmin/company/{companyKey}/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyUserController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final UserCompanyRoleRepository userCompanyRoleRepository;
    private final CompanyRoleMapperRepository companyRoleMapperRepository;
    private final UserHelperService userHelperService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCompanyUsers(
            @PathVariable String companyKey,
            @RequestParam(required = false) String searchKey) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        List<UserCompanyRole> userCompanyRoles = userCompanyRoleRepository.findCompanyUsers(company, searchKey);

        List<Map<String, Object>> users = userCompanyRoles.stream()
                .map(ucr -> {
                    Map<String, Object> userMap = new HashMap<>();
                    User user = ucr.getUser();
                    userMap.put("id", user.getId());
                    userMap.put("email", user.getEmail());
                    userMap.put("name", user.getName());
                    userMap.put("active", ucr.getActive());
                    userMap.put("role", ucr.getCompanyRoleMapper().getRole().getName());
                    return userMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("company", Map.of(
                "id", company.getId(),
                "name", company.getName(),
                "companyKey", company.getCompanyKey()
        ));
        response.put("users", users);
        response.put("leftTab", "users");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addUserToCompany(
            @PathVariable String companyKey,
            @RequestBody AddUserToCompanyRequest request) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyRoleMapper companyRoleMapper = companyRoleMapperRepository.findByRoleKey(request.getRole())
                .orElseThrow(() -> new RuntimeException("Company Role was not found"));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> createTempUser(request.getEmail(), request.getEmail()));

        // Check if user already exists in company
        if (userCompanyRoleRepository.findByCompanyAndUser(company, user).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User already exists in this company"));
        }

        // Check if user is super admin
        Set<String> userRoles = user.getRoleNames();
        if (userRoles.contains(Role.ROLE_SUPER_ADMIN)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User already has access"));
        }

        userHelperService.addUserToCompany(
                company,
                companyRoleMapper,
                user,
                request.getModules() != null ? request.getModules() : List.of()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User added to company successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeUserFromCompany(
            @PathVariable String companyKey,
            @PathVariable Long userId) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean deleted = userHelperService.deleteUserFromCompany(company, user);

        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User removed from company successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to remove user from company"));
        }
    }

    private User createTempUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .isEnabled(true)
                .isSuperAdmin(false)
                .isDeactive(false)
                .isPasswordTemporary(true)
                .build();

        // Generate password
        String password = UtilHelper.generatePassword();
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }
}

