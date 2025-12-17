package com.teckiz.service;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyRoleMapper;
import com.teckiz.entity.Role;
import com.teckiz.entity.User;
import com.teckiz.entity.UserCompanyModule;
import com.teckiz.entity.UserCompanyRole;
import com.teckiz.repository.CompanyModuleMapperRepository;
import com.teckiz.repository.UserCompanyModuleRepository;
import com.teckiz.repository.UserCompanyRoleRepository;
import com.teckiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserHelperService {

    private final UserCompanyRoleRepository userCompanyRoleRepository;
    private final UserCompanyModuleRepository userCompanyModuleRepository;
    private final CompanyModuleMapperRepository companyModuleMapperRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void addUserToCompany(Company company, CompanyRoleMapper companyRoleMapper, User user, List<String> moduleKeys) {
        UserCompanyRole userCompanyRole = UserCompanyRole.builder()
                .company(company)
                .companyRoleMapper(companyRoleMapper)
                .user(user)
                .build();

        // Check if user has an active role, if not, make this one active
        userCompanyRoleRepository.findByUserAndActiveTrue(user)
                .ifPresentOrElse(
                        existing -> userCompanyRole.setActive(false),
                        () -> {
                            userCompanyRole.setActive(true);
                            // Update user roles
                            Set<String> roles = new HashSet<>();
                            roles.add(companyRoleMapper.getRole().getRole());
                            user.setRoles(roles.toString());
                        }
                );

        // Add modules
        for (String moduleKey : moduleKeys) {
            companyModuleMapperRepository.findByModuleMapperKeyAndArchivedFalse(moduleKey)
                    .ifPresent(companyModuleMapper -> {
                        UserCompanyModule userCompanyModule = UserCompanyModule.builder()
                                .company(company)
                                .user(user)
                                .active(true)
                                .companyModuleMapper(companyModuleMapper)
                                .build();
                        userCompanyModuleRepository.save(userCompanyModule);
                    });
        }

        userCompanyRoleRepository.save(userCompanyRole);
    }

    @Transactional
    public boolean updateUserCompanyRole(Company company, String companyRoleKey, CompanyRoleMapper companyRole, User user, List<String> moduleKeys) {
        UserCompanyRole userCompanyRole = userCompanyRoleRepository.findByCompanyAndUser(company, user)
                .orElse(null);

        if (userCompanyRole == null) {
            return false;
        }

        userCompanyRole.setCompanyRoleMapper(companyRole);
        if (Boolean.TRUE.equals(userCompanyRole.getActive())) {
            Set<String> roles = new HashSet<>();
            roles.add(companyRole.getRole().getRole());
            user.setRoles(roles.toString());
        }

        // Remove existing modules
        List<UserCompanyModule> existingModules = userCompanyModuleRepository.findByUserAndCompany(user, company);
        userCompanyModuleRepository.deleteAll(existingModules);

        // If not admin, save and return
        if (!Role.ROLE_COMPANY_ADMIN.equals(companyRole.getRole().getRole())) {
            userCompanyRoleRepository.save(userCompanyRole);
            return true;
        }

        // Add new modules for admin
        for (String moduleKey : moduleKeys) {
            companyModuleMapperRepository.findByModuleMapperKeyAndArchivedFalse(moduleKey)
                    .ifPresent(companyModuleMapper -> {
                        UserCompanyModule userCompanyModule = UserCompanyModule.builder()
                                .company(company)
                                .user(user)
                                .active(true)
                                .companyModuleMapper(companyModuleMapper)
                                .build();
                        userCompanyModuleRepository.save(userCompanyModule);
                    });
        }

        userCompanyRoleRepository.save(userCompanyRole);
        return true;
    }

    @Transactional
    public boolean deleteUserFromCompany(Company company, User user) {
        UserCompanyRole userCompanyRole = userCompanyRoleRepository.findByCompanyAndUser(company, user)
                .orElse(null);

        if (userCompanyRole == null) {
            return false;
        }

        // Remove modules
        List<UserCompanyModule> userCompanyModules = userCompanyModuleRepository.findByUserAndCompany(user, company);
        userCompanyModuleRepository.deleteAll(userCompanyModules);

        // Remove user company role
        userCompanyRoleRepository.delete(userCompanyRole);

        return afterDelete(user);
    }

    @Transactional
    public boolean afterDelete(User user) {
        UserCompanyRole userActiveRole = userCompanyRoleRepository.findByUserAndActiveTrue(user)
                .orElse(null);

        if (userActiveRole != null) {
            Set<String> roles = new HashSet<>();
            roles.add(userActiveRole.getCompanyRoleMapper().getRole().getRole());
            user.setRoles(roles.toString());
            userRepository.save(user);
            return true;
        }

        // If no active role, delete user and profile
        userRepository.delete(user);
        return true;
    }
}

