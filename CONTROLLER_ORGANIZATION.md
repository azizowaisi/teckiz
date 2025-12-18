# Controller Organization Structure

## New Folder Structure

```
controller/
├── admin/
│   ├── website/          # Website module admin controllers
│   ├── journal/          # Journal/Research module admin controllers
│   ├── education/        # Education module admin controllers
│   └── superadmin/       # SuperAdmin controllers
├── publicapi/            # Public-facing controllers (no auth required)
└── AuthController.java   # Authentication (root level)
```

## Controller Mapping

### Admin → Website (`controller/admin/website/`)
- `WebPageController.java`
- `WebNewsController.java`
- `WebNewsTypeController.java`
- `WebAlbumController.java`
- `WebEventController.java`
- `WebContactsController.java`
- `WebContactTypeController.java`
- `WebRelatedMediaController.java`
- `CompanyModuleMapperMenuController.java`
- `WebsiteDashboardController.java`
- `WebSubscriberController.java`
- `WebWidgetController.java`

**Package:** `package com.teckiz.controller.admin.website;`

### Admin → Journal (`controller/admin/journal/`)
- `ResearchJournalController.java`
- `ResearchJournalVolumeController.java`
- `ResearchArticleController.java`
- `ResearchArticleAuthorController.java`
- `ResearchArticleReviewerController.java`
- `ResearchArticleTypeController.java`

**Package:** `package com.teckiz.controller.admin.journal;`

### Admin → Education (`controller/admin/education/`)
- `FacilityController.java`
- `StoryController.java`
- `StoryTypeController.java`
- `SkillController.java`
- `PrincipalMessageController.java`

**Package:** `package com.teckiz.controller.admin.education;`

### Admin → SuperAdmin (`controller/admin/superadmin/`)
- `SuperAdminController.java`
- `CompanyController.java`
- `CompanyUserController.java`
- `CompanyModuleController.java`
- `CompanyRoleController.java`
- `RoleController.java`

**Package:** `package com.teckiz.controller.admin.superadmin;`

### Public (`controller/public/`)
- `PublicWebPageController.java`
- `PublicWebNewsController.java`
- `PublicWebAlbumController.java`
- `PublicWebEventController.java`
- `PublicResearchArticleController.java`

**Package:** `package com.teckiz.controller.publicapi;`

### Root Level
- `AuthController.java` (stays in root or can move to `controller/auth/`)

**Package:** `package com.teckiz.controller;` or `package com.teckiz.controller.auth;`

## URL Paths (Unchanged)

The `@RequestMapping` annotations remain the same:
- `/website/admin/*` - Website admin
- `/journal/admin/*` - Journal admin
- `/education/admin/*` - Education admin
- `/superadmin/*` - SuperAdmin
- `/public/*` - Public APIs
- `/auth/*` - Authentication

## Migration Steps

1. Create folder structure
2. Move controllers to appropriate folders
3. Update package declarations in each file
4. Update imports in any files that reference these controllers
5. Test compilation

## Benefits

- ✅ Better organization matching Symfony structure
- ✅ Easier to navigate and maintain
- ✅ Clear separation of concerns
- ✅ Matches URL path structure
- ✅ Scalable for future modules

