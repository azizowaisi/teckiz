package com.teckiz.controller.admin.website;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.repository.*;
import com.teckiz.service.ModuleAccessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/website/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebsiteDashboardController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebPageRepository webPageRepository;
    private final WebNewsRepository webNewsRepository;
    private final WebAlbumRepository webAlbumRepository;
    private final WebEventRepository webEventRepository;
    private final WebContactsRepository webContactsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Map<String, Object> stats = new HashMap<>();
        
        // Count pages
        long pageCount = webPageRepository.findByCompany(companyModuleMapper.getCompany()).size();
        stats.put("totalPages", pageCount);
        
        // Count news articles
        long newsCount = webNewsRepository.findByCompany(companyModuleMapper.getCompany()).size();
        stats.put("totalNews", newsCount);
        
        // Count published news
        long publishedNews = webNewsRepository.findByCompany(companyModuleMapper.getCompany())
                .stream()
                .filter(news -> Boolean.TRUE.equals(news.getPublished()) && 
                        !Boolean.TRUE.equals(news.getArchived()))
                .count();
        stats.put("publishedNews", publishedNews);
        
        // Count albums
        long albumCount = webAlbumRepository.findByCompany(companyModuleMapper.getCompany()).size();
        stats.put("totalAlbums", albumCount);
        
        // Count published albums
        long publishedAlbums = webAlbumRepository.findByCompany(companyModuleMapper.getCompany())
                .stream()
                .filter(album -> Boolean.TRUE.equals(album.getPublished()) && 
                        !Boolean.TRUE.equals(album.getArchived()))
                .count();
        stats.put("publishedAlbums", publishedAlbums);
        
        // Count events
        long eventCount = webEventRepository.findByCompany(companyModuleMapper.getCompany()).size();
        stats.put("totalEvents", eventCount);
        
        // Count published events
        long publishedEvents = webEventRepository.findByCompany(companyModuleMapper.getCompany())
                .stream()
                .filter(event -> Boolean.TRUE.equals(event.getPublished()) && 
                        !Boolean.TRUE.equals(event.getArchived()))
                .count();
        stats.put("publishedEvents", publishedEvents);
        
        // Count contacts
        long contactCount = webContactsRepository.findByCompany(companyModuleMapper.getCompany()).size();
        stats.put("totalContacts", contactCount);

        Map<String, Object> response = new HashMap<>();
        response.put("stats", stats);
        response.put("leftTab", "dashboard");
        return ResponseEntity.ok(response);
    }
}

