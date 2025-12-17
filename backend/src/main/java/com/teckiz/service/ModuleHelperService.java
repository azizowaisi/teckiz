package com.teckiz.service;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.CompanyModuleMapperMenu;
import com.teckiz.entity.Module;
import com.teckiz.repository.CompanyModuleMapperMenuRepository;
import com.teckiz.repository.CompanyModuleMapperRepository;
import com.teckiz.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleHelperService {

    private final CompanyModuleMapperRepository companyModuleMapperRepository;
    private final ModuleRepository moduleRepository;
    private final CompanyModuleMapperMenuRepository menuRepository;

    public Map<String, String> getAllModules() {
        List<Module> modules = moduleRepository.findByArchivedFalse();
        return modules.stream()
                .collect(Collectors.toMap(
                        Module::getName,
                        Module::getModuleKey,
                        (existing, replacement) -> existing
                ));
    }

    public Map<String, String> getCompanyModuleList(Company company) {
        List<Module> allModules = moduleRepository.findByArchivedFalse();
        List<CompanyModuleMapper> companyModules = companyModuleMapperRepository.findByCompany(company);

        Map<String, String> companyModuleNames = companyModules.stream()
                .map(CompanyModuleMapper::getModule)
                .map(Module::getName)
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toMap(name -> name, name -> name));

        return allModules.stream()
                .filter(module -> !companyModuleNames.containsKey(module.getName()))
                .collect(Collectors.toMap(
                        Module::getName,
                        Module::getModuleKey,
                        (existing, replacement) -> existing
                ));
    }

    @Transactional
    public void addMenuToModule(CompanyModuleMapper companyModuleMapper) {
        Module websiteModule = moduleRepository.findByName(Module.WEBSITE)
                .orElse(null);

        if (websiteModule == null) {
            return;
        }

        CompanyModuleMapper websiteModuleMapper = companyModuleMapperRepository
                .findByCompanyAndModuleAndArchivedFalse(
                        companyModuleMapper.getCompany(),
                        websiteModule
                )
                .orElse(null);

        if (websiteModuleMapper == null) {
            return;
        }

        String moduleType = companyModuleMapper.getModule().getType();

        if (Module.EDUCATION.equals(moduleType)) {
            createEducationModuleMenus(websiteModuleMapper);
            return;
        }

        if (Module.JOURNAL.equals(moduleType)) {
            createJournalModuleMenus(websiteModuleMapper);
            return;
        }

        if (Module.JOURNAL_INDEX.equals(moduleType)) {
            createJournalIndexMenus(websiteModuleMapper);
            return;
        }

        createWebsiteModuleMenus(companyModuleMapper);
    }

    private void createJournalModuleMenus(CompanyModuleMapper companyModuleMapper) {
        createMenu(companyModuleMapper, "JOURNAL_ARCHIVES", "/journal/archives", 6);
        createMenu(companyModuleMapper, "JOURNAL_COMING", "/journal/coming", 7);
        createMenu(companyModuleMapper, "JOURNAL_PAGE", "/journal/page", 8);
        createMenu(companyModuleMapper, "JOURNAL_CURRENT", "/journal/current", 9);
    }

    private void createEducationModuleMenus(CompanyModuleMapper companyModuleMapper) {
        createMenu(companyModuleMapper, "ALUMNI", "/alumni", 6);
        createMenu(companyModuleMapper, "PROGRAMS", "/programs", 7);
        createMenu(companyModuleMapper, "FACILITIES", "/facilities", 8);
    }

    private void createWebsiteModuleMenus(CompanyModuleMapper companyModuleMapper) {
        createMenu(companyModuleMapper, "NEWS", "/news", 1);
        createMenu(companyModuleMapper, "EVENTS", "/events", 2);
        createMenu(companyModuleMapper, "NEWSSUBSCRIPTION", "/news-subscription", 3);
        createMenu(companyModuleMapper, "ALBUM", "/album", 4);
        createMenu(companyModuleMapper, "ABOUTUS", "/about-us", 5);
    }

    private void createJournalIndexMenus(CompanyModuleMapper companyModuleMapper) {
        createMenu(companyModuleMapper, "JOURNAL_INDEX_REGISTRATION", "/journal-index/registration", 6);
        createMenu(companyModuleMapper, "JOURNAL_INDEX_SEARCH", "/journal-index/search", 7);
    }

    @Transactional
    private void createMenu(CompanyModuleMapper companyModuleMapper, String menuType, String menuUrl, int position) {
        // Check if menu already exists
        if (menuRepository.findByCompanyModuleMapperAndMenuType(companyModuleMapper, menuType).isPresent()) {
            return;
        }

        CompanyModuleMapperMenu menu = CompanyModuleMapperMenu.builder()
                .companyModuleMapper(companyModuleMapper)
                .name(menuType)
                .menuType(menuType)
                .routeName(menuUrl)
                .availableInMainMenu(true)
                .availableInFooterMenu(true)
                .availableInHomePage(true)
                .publicMenu(true)
                .position(position)
                .build();

        menuRepository.save(menu);
    }
}

