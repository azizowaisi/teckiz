package com.teckiz.service;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Module;
import com.teckiz.exception.HostNotFoundException;
import com.teckiz.repository.CompanyModuleMapperRepository;
import com.teckiz.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModuleAccessManager {

    private final WebsiteManager websiteManager;
    private final CompanyModuleMapperRepository companyModuleMapperRepository;
    private final ModuleRepository moduleRepository;

    /**
     * Authenticate module access - checks host and user permissions
     */
    public CompanyModuleMapper authenticateModule() {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();
        
        // Check if user has access to this module
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized access!"
            );
        }

        return companyModuleMapper;
    }

    /**
     * Authenticate author access
     */
    public CompanyModuleMapper authenticateAuthor() {
        CompanyModuleMapper companyModuleMapper = authenticateModule();
        
        // Additional author-specific checks can be added here
        // For now, we rely on Spring Security role-based access
        
        return companyModuleMapper;
    }

    /**
     * Authenticate reviewer access
     */
    public CompanyModuleMapper authenticateReviewer() {
        CompanyModuleMapper companyModuleMapper = authenticateModule();
        
        // Additional reviewer-specific checks can be added here
        
        return companyModuleMapper;
    }

    /**
     * Authenticate user with specific module
     */
    public CompanyModuleMapper authenticateUser() {
        String host = getHost();
        String moduleKey = getParameter("moduleKey");

        Optional<CompanyModuleMapper> mapperOpt = companyModuleMapperRepository
                .findByHostAndArchivedFalseAndLiveTrue(host);

        if (mapperOpt.isEmpty()) {
            throw new HostNotFoundException();
        }

        CompanyModuleMapper companyModuleMapper = mapperOpt.get();
        Company company = companyModuleMapper.getCompany();

        if (company == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Company not found"
            );
        }

        if (moduleKey != null && !moduleKey.isEmpty()) {
            Module module = moduleRepository.findByModuleKeyAndArchivedFalse(moduleKey)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Module not found"
                    ));

            Optional<CompanyModuleMapper> moduleMapperOpt = companyModuleMapperRepository
                    .findByCompanyAndModuleAndArchivedFalse(company, module);

            if (moduleMapperOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Selected module not found for your company"
                );
            }

            return moduleMapperOpt.get();
        }

        return companyModuleMapper;
    }

    private String getHost() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            return attributes.getRequest().getServerName();
        }
        return null;
    }

    private String getParameter(String name) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            return attributes.getRequest().getParameter(name);
        }
        return null;
    }
}

