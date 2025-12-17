package com.teckiz.service;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Module;
import com.teckiz.entity.PasswordSecrecy;
import com.teckiz.exception.HostNotFoundException;
import com.teckiz.repository.CompanyModuleMapperRepository;
import com.teckiz.repository.ModuleRepository;
import com.teckiz.repository.PasswordSecrecyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebsiteManager {

    private final CompanyModuleMapperRepository companyModuleMapperRepository;
    private final ModuleRepository moduleRepository;
    private final PasswordSecrecyRepository passwordSecrecyRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

    private String getHeader(String name) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            return attributes.getRequest().getHeader(name);
        }
        return null;
    }

    private String getRemoteAddr() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }

    /**
     * Check authentication by host and return CompanyModuleMapper
     * @return CompanyModuleMapper if valid, throws HostNotFoundException otherwise
     */
    public CompanyModuleMapper checkAuthentication() {
        String host = getHost();

        Optional<CompanyModuleMapper> mapperOpt = companyModuleMapperRepository
                .findByHostAndArchivedFalseAndLiveTrue(host);

        if (mapperOpt.isEmpty()) {
            throw new HostNotFoundException();
        }

        CompanyModuleMapper companyModuleMapper = mapperOpt.get();
        Company company = companyModuleMapper.getCompany();

        if (company == null || !Boolean.TRUE.equals(company.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Your company is not active"
            );
        }

        return companyModuleMapper;
    }

    /**
     * Check authentication for RJ Indexing module
     */
    public CompanyModuleMapper websiteRJIndexingAuthentication() {
        String host = getHost();

        Optional<CompanyModuleMapper> mapperOpt = companyModuleMapperRepository
                .findByHostAndArchivedFalseAndLiveTrue(host);

        if (mapperOpt.isEmpty()) {
            throw new HostNotFoundException();
        }

        CompanyModuleMapper companyModuleMapper = mapperOpt.get();
        Company company = companyModuleMapper.getCompany();

        if (company == null || !Boolean.TRUE.equals(company.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Your company is not active"
            );
        }

        Optional<Module> journalIndexModuleOpt = moduleRepository.findAll().stream()
                .filter(m -> Module.JOURNAL_INDEX.equals(m.getType()))
                .findFirst();

        if (journalIndexModuleOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Module not found"
            );
        }

        Module journalIndexModule = journalIndexModuleOpt.get();
        Optional<CompanyModuleMapper> journalModuleMapperOpt = companyModuleMapperRepository
                .findByCompanyAndModuleAndArchivedFalse(company, journalIndexModule);

        if (journalModuleMapperOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Your company is not registered for research journal indexing module"
            );
        }

        return journalModuleMapperOpt.get();
    }

    /**
     * Security check for journal index with secret key
     */
    public Optional<PasswordSecrecy> securityCheckForJournalIndex(CompanyModuleMapper companyModuleMapper) {
        String secretKey = getParameter("secretKey");

        if (secretKey == null || secretKey.isEmpty()) {
            return Optional.empty();
        }

        Optional<PasswordSecrecy> passwordSecrecyOpt = passwordSecrecyRepository.findBySecretKey(secretKey);

        if (passwordSecrecyOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordSecrecy passwordSecrecy = passwordSecrecyOpt.get();
        CompanyModuleMapper secrecyMapper = passwordSecrecy.getCompanyModuleMapper();

        if (secrecyMapper == null ||
                !companyModuleMapper.getModuleMapperKey().equals(secrecyMapper.getModuleMapperKey())) {
            return Optional.empty();
        }

        if (!"website_index_journal_application_form".equals(passwordSecrecy.getPath())) {
            return Optional.empty();
        }

        return Optional.of(passwordSecrecy);
    }

    /**
     * Check journal authentication
     */
    public CompanyModuleMapper checkJournalAuthentication() {
        String host = getHost();

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

        Optional<Module> journalModuleOpt = moduleRepository.findAll().stream()
                .filter(m -> Module.JOURNAL.equals(m.getName()))
                .findFirst();

        if (journalModuleOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Module not found"
            );
        }

        Module journalModule = journalModuleOpt.get();
        Optional<CompanyModuleMapper> journalModuleMapperOpt = companyModuleMapperRepository
                .findByCompanyAndModuleAndArchivedFalse(company, journalModule);

        if (journalModuleMapperOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Your company is not registered for research journal module"
            );
        }

        return journalModuleMapperOpt.get();
    }

    /**
     * Check security link validity (30 minutes expiration)
     */
    @Transactional
    public Optional<PasswordSecrecy> checkSecurityLink() {
        String secretKey = getParameter("secretKey");

        if (secretKey == null || secretKey.isEmpty()) {
            return Optional.empty();
        }

        Optional<PasswordSecrecy> passwordSecrecyOpt = passwordSecrecyRepository.findBySecretKey(secretKey);

        if (passwordSecrecyOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordSecrecy passwordSecrecy = passwordSecrecyOpt.get();
        LocalDateTime createdAt = passwordSecrecy.getCreatedAt();
        LocalDateTime expirationTime = createdAt.plusMinutes(30);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(expirationTime)) {
            passwordSecrecyRepository.delete(passwordSecrecy);
            return Optional.empty();
        }

        return Optional.of(passwordSecrecy);
    }

    /**
     * Get user IP address from request
     */
    public String getUserIP() {
        String ip = getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }
}

