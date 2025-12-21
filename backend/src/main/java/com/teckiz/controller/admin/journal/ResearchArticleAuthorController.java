package com.teckiz.controller.admin.journal;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ResearchArticleAuthor;
import com.teckiz.repository.ResearchArticleAuthorRepository;
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
@RequestMapping("/journal/admin/authors")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchArticleAuthorController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchArticleAuthorRepository authorRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<ResearchArticleAuthor> authors = authorRepository.findByCompany(
                companyModuleMapper.getCompany()
        );

        // Filter by search if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            authors = authors.stream()
                    .filter(author -> 
                            (author.getName() != null && author.getName().toLowerCase().contains(searchLower)) ||
                            (author.getEmail() != null && author.getEmail().toLowerCase().contains(searchLower)) ||
                            (author.getFirstname() != null && author.getFirstname().toLowerCase().contains(searchLower)) ||
                            (author.getLastname() != null && author.getLastname().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, authors.size());
        List<ResearchArticleAuthor> paginatedAuthors = authors.subList(start, end);

        List<Map<String, Object>> authorResponses = paginatedAuthors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("authors", authorResponses);
        response.put("totalElements", authors.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) authors.size() / size));
        response.put("leftTab", "author");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{authorKey}")
    public ResponseEntity<Map<String, Object>> getAuthor(@PathVariable String authorKey) {
        moduleAccessManager.authenticateModule();
        
        return authorRepository.findByAuthorKey(authorKey)
                .map(author -> ResponseEntity.ok(mapToResponse(author)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAuthor(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        ResearchArticleAuthor author = ResearchArticleAuthor.builder()
                .company(companyModuleMapper.getCompany())
                .name(name)
                .firstname((String) request.get("firstname"))
                .lastname((String) request.get("lastname"))
                .email((String) request.get("email"))
                .phone((String) request.get("phone"))
                .role((String) request.get("role"))
                .description((String) request.get("description"))
                .url((String) request.get("url"))
                .orcid((String) request.get("orcid"))
                .linkedin((String) request.get("linkedin"))
                .researchGate((String) request.get("researchGate"))
                .twitter((String) request.get("twitter"))
                .facebook((String) request.get("facebook"))
                .instagram((String) request.get("instagram"))
                .country((String) request.get("country"))
                .thumbnail((String) request.get("thumbnail"))
                .build();

        author = authorRepository.save(author);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Author created successfully", "authorKey", author.getAuthorKey()));
    }

    @PutMapping("/{authorKey}")
    public ResponseEntity<?> updateAuthor(
            @PathVariable String authorKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchArticleAuthor author = authorRepository.findByAuthorKey(authorKey)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        if (!author.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            author.setName((String) request.get("name"));
        }
        if (request.get("firstname") != null) {
            author.setFirstname((String) request.get("firstname"));
        }
        if (request.get("lastname") != null) {
            author.setLastname((String) request.get("lastname"));
        }
        if (request.get("email") != null) {
            author.setEmail((String) request.get("email"));
        }
        if (request.get("phone") != null) {
            author.setPhone((String) request.get("phone"));
        }
        if (request.get("role") != null) {
            author.setRole((String) request.get("role"));
        }
        if (request.get("description") != null) {
            author.setDescription((String) request.get("description"));
        }
        if (request.get("orcid") != null) {
            author.setOrcid((String) request.get("orcid"));
        }
        // Update social media links
        if (request.get("linkedin") != null) {
            author.setLinkedin((String) request.get("linkedin"));
        }
        if (request.get("researchGate") != null) {
            author.setResearchGate((String) request.get("researchGate"));
        }
        if (request.get("twitter") != null) {
            author.setTwitter((String) request.get("twitter"));
        }
        if (request.get("facebook") != null) {
            author.setFacebook((String) request.get("facebook"));
        }
        if (request.get("instagram") != null) {
            author.setInstagram((String) request.get("instagram"));
        }

        author = authorRepository.save(author);

        return ResponseEntity.ok(Map.of("message", "Author updated successfully"));
    }

    @DeleteMapping("/{authorKey}")
    public ResponseEntity<?> deleteAuthor(@PathVariable String authorKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchArticleAuthor author = authorRepository.findByAuthorKey(authorKey)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        if (!author.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        author.setArchived(true);
        authorRepository.save(author);

        return ResponseEntity.ok(Map.of("message", "Author archived successfully"));
    }

    private Map<String, Object> mapToResponse(ResearchArticleAuthor author) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", author.getId());
        response.put("authorKey", author.getAuthorKey());
        response.put("name", author.getName());
        response.put("firstname", author.getFirstname());
        response.put("lastname", author.getLastname());
        response.put("email", author.getEmail());
        response.put("phone", author.getPhone());
        response.put("role", author.getRole());
        response.put("description", author.getDescription());
        response.put("orcid", author.getOrcid());
        response.put("linkedin", author.getLinkedin());
        response.put("researchGate", author.getResearchGate());
        response.put("twitter", author.getTwitter());
        response.put("facebook", author.getFacebook());
        response.put("instagram", author.getInstagram());
        response.put("country", author.getCountry());
        response.put("thumbnail", author.getThumbnail());
        response.put("archived", author.getArchived());
        return response;
    }
}

