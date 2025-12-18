package com.teckiz.controller.admin.website;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.CompanyModuleMapperMenu;
import com.teckiz.repository.CompanyModuleMapperMenuRepository;
import com.teckiz.service.ModuleAccessManager;
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
@RequestMapping("/website/admin/menus")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class CompanyModuleMapperMenuController {

    private final ModuleAccessManager moduleAccessManager;
    private final CompanyModuleMapperMenuRepository menuRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listMenus(
            @RequestParam(required = false) String menuType) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<CompanyModuleMapperMenu> menus;
        if (menuType != null && !menuType.isEmpty()) {
            menus = menuRepository.findByCompanyModuleMapperOrderByPositionAsc(companyModuleMapper)
                    .stream()
                    .filter(menu -> menuType.equals(menu.getMenuType()))
                    .collect(Collectors.toList());
        } else {
            menus = menuRepository.findByCompanyModuleMapperOrderByPositionAsc(companyModuleMapper);
        }

        List<Map<String, Object>> menuResponses = menus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("menus", menuResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/main-menu")
    public ResponseEntity<Map<String, Object>> getMainMenu() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<CompanyModuleMapperMenu> menus = menuRepository
                .findByCompanyModuleMapperAndAvailableInMainMenuTrueAndPublicMenuTrueOrderByPositionAsc(
                        companyModuleMapper);

        List<Map<String, Object>> menuResponses = menus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("menus", menuResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/footer-menu")
    public ResponseEntity<Map<String, Object>> getFooterMenu() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<CompanyModuleMapperMenu> menus = menuRepository
                .findByCompanyModuleMapperAndPublicMenuTrueAndMasterFalseAndAvailableInFooterMenuTrueOrderByPositionAsc(
                        companyModuleMapper);

        List<Map<String, Object>> menuResponses = menus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("menus", menuResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{menuKey}")
    public ResponseEntity<Map<String, Object>> getMenu(@PathVariable String menuKey) {
        moduleAccessManager.authenticateModule();

        return menuRepository.findByMenuKey(menuKey)
                .map(menu -> ResponseEntity.ok(mapToResponse(menu)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMenu(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        CompanyModuleMapperMenu menu = CompanyModuleMapperMenu.builder()
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .routeName((String) request.get("routeName"))
                .menuType((String) request.get("menuType"))
                .publicMenu(request.get("publicMenu") != null ?
                        (Boolean) request.get("publicMenu") : false)
                .availableInMainMenu(request.get("availableInMainMenu") != null ?
                        (Boolean) request.get("availableInMainMenu") : false)
                .availableInFooterMenu(request.get("availableInFooterMenu") != null ?
                        (Boolean) request.get("availableInFooterMenu") : false)
                .availableInHomePage(request.get("availableInHomePage") != null ?
                        (Boolean) request.get("availableInHomePage") : false)
                .homePage(request.get("homePage") != null ?
                        (Boolean) request.get("homePage") : false)
                .master(request.get("master") != null ?
                        (Boolean) request.get("master") : false)
                .thumbnail((String) request.get("thumbnail"))
                .externalUrl((String) request.get("externalUrl"))
                .newTab(request.get("newTab") != null ?
                        (Boolean) request.get("newTab") : false)
                .position(request.get("position") != null ?
                        ((Number) request.get("position")).intValue() : 0)
                .subMenuPosition(request.get("subMenuPosition") != null ?
                        ((Number) request.get("subMenuPosition")).intValue() : 0)
                .build();

        // Set parent menu if provided
        if (request.get("mainMenuKey") != null) {
            menuRepository.findByMenuKey((String) request.get("mainMenuKey"))
                    .ifPresent(menu::setMainMenu);
        }

        menu = menuRepository.save(menu);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Menu created successfully", "menuKey", menu.getMenuKey()));
    }

    @PutMapping("/{menuKey}")
    public ResponseEntity<?> updateMenu(
            @PathVariable String menuKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        CompanyModuleMapperMenu menu = menuRepository.findByMenuKey(menuKey)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        if (!menu.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            menu.setName((String) request.get("name"));
        }
        if (request.get("routeName") != null) {
            menu.setRouteName((String) request.get("routeName"));
        }
        if (request.get("menuType") != null) {
            menu.setMenuType((String) request.get("menuType"));
        }
        if (request.get("publicMenu") != null) {
            menu.setPublicMenu((Boolean) request.get("publicMenu"));
        }
        if (request.get("availableInMainMenu") != null) {
            menu.setAvailableInMainMenu((Boolean) request.get("availableInMainMenu"));
        }
        if (request.get("availableInFooterMenu") != null) {
            menu.setAvailableInFooterMenu((Boolean) request.get("availableInFooterMenu"));
        }
        if (request.get("availableInHomePage") != null) {
            menu.setAvailableInHomePage((Boolean) request.get("availableInHomePage"));
        }
        if (request.get("homePage") != null) {
            menu.setHomePage((Boolean) request.get("homePage"));
        }
        if (request.get("position") != null) {
            menu.setPosition(((Number) request.get("position")).intValue());
        }
        if (request.get("subMenuPosition") != null) {
            menu.setSubMenuPosition(((Number) request.get("subMenuPosition")).intValue());
        }
        if (request.get("thumbnail") != null) {
            menu.setThumbnail((String) request.get("thumbnail"));
        }
        if (request.get("externalUrl") != null) {
            menu.setExternalUrl((String) request.get("externalUrl"));
        }
        if (request.get("newTab") != null) {
            menu.setNewTab((Boolean) request.get("newTab"));
        }
        if (request.get("mainMenuKey") != null) {
            menuRepository.findByMenuKey((String) request.get("mainMenuKey"))
                    .ifPresent(menu::setMainMenu);
        }

        menu = menuRepository.save(menu);

        return ResponseEntity.ok(Map.of("message", "Menu updated successfully"));
    }

    @DeleteMapping("/{menuKey}")
    public ResponseEntity<?> deleteMenu(@PathVariable String menuKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        CompanyModuleMapperMenu menu = menuRepository.findByMenuKey(menuKey)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        if (!menu.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        menuRepository.delete(menu);

        return ResponseEntity.ok(Map.of("message", "Menu deleted successfully"));
    }

    private Map<String, Object> mapToResponse(CompanyModuleMapperMenu menu) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", menu.getId());
        response.put("menuKey", menu.getMenuKey());
        response.put("name", menu.getName());
        response.put("routeName", menu.getRouteName());
        response.put("menuType", menu.getMenuType());
        response.put("publicMenu", menu.getPublicMenu());
        response.put("availableInMainMenu", menu.getAvailableInMainMenu());
        response.put("availableInFooterMenu", menu.getAvailableInFooterMenu());
        response.put("availableInHomePage", menu.getAvailableInHomePage());
        response.put("homePage", menu.getHomePage());
        response.put("master", menu.getMaster());
        response.put("thumbnail", menu.getThumbnail());
        response.put("externalUrl", menu.getExternalUrl());
        response.put("newTab", menu.getNewTab());
        response.put("position", menu.getPosition());
        response.put("subMenuPosition", menu.getSubMenuPosition());

        if (menu.getMainMenu() != null) {
            response.put("mainMenu", Map.of(
                    "menuKey", menu.getMainMenu().getMenuKey(),
                    "name", menu.getMainMenu().getName()
            ));
        }

        if (menu.getSubMenus() != null && !menu.getSubMenus().isEmpty()) {
            response.put("subMenus", menu.getSubMenus().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }
}

