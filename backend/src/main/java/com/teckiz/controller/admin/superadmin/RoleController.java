package com.teckiz.controller.admin.superadmin;

import com.teckiz.entity.Role;
import com.teckiz.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/superadmin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listRoles(
            @RequestParam(required = false) Boolean companyRole) {

        List<Role> roles;
        if (companyRole != null) {
            if (companyRole) {
                roles = roleRepository.findByCompanyRoleTrue();
            } else {
                roles = roleRepository.findAll().stream()
                        .filter(role -> !Boolean.TRUE.equals(role.getCompanyRole()))
                        .collect(Collectors.toList());
            }
        } else {
            roles = roleRepository.findAll(Sort.by("name").ascending());
        }

        List<Map<String, Object>> roleResponses = roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("roles", roleResponses);
        response.put("leftTab", "roles");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roleKey}")
    public ResponseEntity<Map<String, Object>> getRole(@PathVariable String roleKey) {
        return roleRepository.findByRoleKey(roleKey)
                .map(role -> ResponseEntity.ok(mapToResponse(role)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String role = (String) request.get("role");
        
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }
        
        if (role == null || role.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Role is required"));
        }

        // Check if role already exists
        if (roleRepository.findAll().stream()
                .anyMatch(r -> role.equals(r.getRole()))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Role already exists"));
        }

        Role newRole = Role.builder()
                .name(name)
                .role(role)
                .description((String) request.get("description"))
                .companyRole(request.get("companyRole") != null ?
                        (Boolean) request.get("companyRole") : true)
                .build();

        newRole = roleRepository.save(newRole);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Role created successfully", "roleKey", newRole.getRoleKey()));
    }

    @PutMapping("/{roleKey}")
    public ResponseEntity<?> updateRole(
            @PathVariable String roleKey,
            @RequestBody Map<String, Object> request) {

        Role role = roleRepository.findByRoleKey(roleKey)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (request.get("name") != null) {
            role.setName((String) request.get("name"));
        }
        if (request.get("role") != null) {
            String newRoleValue = (String) request.get("role");
            final Long currentRoleId = role.getId();
            // Check if new role value already exists (excluding current role)
            if (roleRepository.findAll().stream()
                    .anyMatch(r -> !r.getId().equals(currentRoleId) && newRoleValue.equals(r.getRole()))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Role already exists"));
            }
            role.setRole(newRoleValue);
        }
        if (request.get("description") != null) {
            role.setDescription((String) request.get("description"));
        }
        if (request.get("companyRole") != null) {
            role.setCompanyRole((Boolean) request.get("companyRole"));
        }

        role = roleRepository.save(role);

        return ResponseEntity.ok(Map.of("message", "Role updated successfully"));
    }

    @DeleteMapping("/{roleKey}")
    public ResponseEntity<?> deleteRole(@PathVariable String roleKey) {
        Role role = roleRepository.findByRoleKey(roleKey)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Check if role is in use
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot delete role that is assigned to users"));
        }

        roleRepository.delete(role);

        return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
    }

    private Map<String, Object> mapToResponse(Role role) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", role.getId());
        response.put("roleKey", role.getRoleKey());
        response.put("name", role.getName());
        response.put("role", role.getRole());
        response.put("companyRole", role.getCompanyRole());
        response.put("description", role.getDescription());
        if (role.getUsers() != null) {
            response.put("userCount", role.getUsers().size());
        }
        return response;
    }
}

