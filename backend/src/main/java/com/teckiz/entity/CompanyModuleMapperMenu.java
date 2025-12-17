package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CompanyModuleMapperMenu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyModuleMapperMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_key", length = 50, nullable = false)
    private String menuKey;

    @Column(name = "route_name", length = 128)
    private String routeName;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean publicMenu = false;

    @Column(name = "menu_type", length = 128)
    private String menuType;

    @Column(name = "is_available_in_main_menu")
    @Builder.Default
    private Boolean availableInMainMenu = false;

    @Column(name = "is_available_in_footer_menu")
    @Builder.Default
    private Boolean availableInFooterMenu = false;

    @Column(name = "is_available_in_home_page")
    @Builder.Default
    private Boolean availableInHomePage = false;

    @Column(name = "is_home_page")
    @Builder.Default
    private Boolean homePage = false;

    @Column(name = "is_master")
    @Builder.Default
    private Boolean master = false;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "external_url", columnDefinition = "TEXT")
    private String externalUrl;

    @Column(name = "is_new_tab")
    @Builder.Default
    private Boolean newTab = false;

    @Column(name = "position")
    @Builder.Default
    private Integer position = 0;

    @Column(name = "sub_menu_position")
    @Builder.Default
    private Integer subMenuPosition = 0;

    @OneToMany(mappedBy = "mainMenu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyModuleMapperMenu> subMenus = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_module_mapper_id", nullable = false)
    private CompanyModuleMapper companyModuleMapper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_menu_id")
    private CompanyModuleMapperMenu mainMenu;

    @PrePersist
    protected void onCreate() {
        if (menuKey == null) {
            menuKey = UtilHelper.generateEntityKey();
        }
    }
}

