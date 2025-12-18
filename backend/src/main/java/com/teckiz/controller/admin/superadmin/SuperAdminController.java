package com.teckiz.controller.admin.superadmin;

import com.teckiz.entity.Module;
import com.teckiz.entity.User;
import com.teckiz.repository.ModuleRepository;
import com.teckiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    @GetMapping("/index")
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> response = new HashMap<>();
        response.put("leftTab", "overall");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/superadmin")
    public ResponseEntity<Map<String, Object>> superAdmin() {
        List<User> superAdminList = userRepository.findSuperAdminList();
        Map<String, Object> response = new HashMap<>();
        response.put("leftTab", "superadmin");
        response.put("superAdminList", superAdminList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/modules")
    public ResponseEntity<Map<String, Object>> modules() {
        List<Module> modules = moduleRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("modules", modules);
        response.put("leftTab", "modules");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> users() {
        List<User> users = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("leftTab", "users");
        return ResponseEntity.ok(response);
    }
}

