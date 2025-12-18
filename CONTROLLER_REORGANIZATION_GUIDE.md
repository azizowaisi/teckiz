# Controller Reorganization Guide

## Overview

Controllers are being reorganized from a flat structure to a hierarchical folder structure that matches the Symfony organization and URL paths.

## Current Structure (Flat)
```
controller/
├── WebPageController.java
├── PublicWebPageController.java
├── ResearchJournalController.java
├── FacilityController.java
├── SuperAdminController.java
└── ... (all controllers in one folder)
```

## New Structure (Hierarchical)
```
controller/
├── admin/
│   ├── website/          # Website module admin controllers
│   │   ├── WebPageController.java
│   │   ├── WebNewsController.java
│   │   ├── WebAlbumController.java
│   │   └── ...
│   ├── journal/          # Journal/Research module admin controllers
│   │   ├── ResearchJournalController.java
│   │   ├── ResearchArticleController.java
│   │   └── ...
│   ├── education/        # Education module admin controllers
│   │   ├── FacilityController.java
│   │   ├── StoryController.java
│   │   └── ...
│   └── superadmin/       # SuperAdmin controllers
│       ├── SuperAdminController.java
│       ├── CompanyController.java
│       └── ...
├── publicapi/            # Public-facing controllers (public is reserved keyword)
│   ├── PublicWebPageController.java
│   ├── PublicWebNewsController.java
│   └── ...
└── AuthController.java   # Authentication (root level)
```

## Package Structure

### Before
```java
package com.teckiz.controller;
```

### After
```java
// Website Admin
package com.teckiz.controller.admin.website;

// Journal Admin
package com.teckiz.controller.admin.journal;

// Education Admin
package com.teckiz.controller.admin.education;

// SuperAdmin
package com.teckiz.controller.admin.superadmin;

// Public API (public is reserved keyword in Java)
package com.teckiz.controller.publicapi;

// Auth (root)
package com.teckiz.controller;
```

## URL Paths (Unchanged)

The `@RequestMapping` annotations remain exactly the same:
- `/website/admin/*` → `controller/admin/website/`
- `/journal/admin/*` → `controller/admin/journal/`
- `/education/admin/*` → `controller/admin/education/`
- `/superadmin/*` → `controller/admin/superadmin/`
- `/public/*` → `controller/publicapi/`
- `/auth/*` → `controller/` (root)

## Migration Steps

### Option 1: Automated Script
```bash
cd backend
./migrate_controllers.sh
```

### Option 2: Manual Migration
1. Create folder structure (already done)
2. Move controllers to appropriate folders
3. Update package declarations in each file
4. Update any imports that reference these controllers
5. Test compilation

## Controller Mapping

### Website Admin (`controller/admin/website/`)
- WebPageController
- WebNewsController
- WebNewsTypeController
- WebAlbumController
- WebEventController
- WebContactsController
- WebContactTypeController
- WebRelatedMediaController
- CompanyModuleMapperMenuController
- WebsiteDashboardController
- WebSubscriberController
- WebWidgetController

### Journal Admin (`controller/admin/journal/`)
- ResearchJournalController
- ResearchJournalVolumeController
- ResearchArticleController
- ResearchArticleAuthorController
- ResearchArticleReviewerController
- ResearchArticleTypeController

### Education Admin (`controller/admin/education/`)
- FacilityController
- StoryController
- StoryTypeController
- SkillController
- PrincipalMessageController

### SuperAdmin (`controller/admin/superadmin/`)
- SuperAdminController
- CompanyController
- CompanyUserController
- CompanyModuleController
- CompanyRoleController
- RoleController

### Public API (`controller/publicapi/`)
- WebPageController (public API version)
- WebNewsController (public API version)
- WebAlbumController (public API version)
- WebEventController (public API version)
- ResearchArticleController (public API version)

**Note:** Class names don't include "Public" prefix - the package name already indicates they're public APIs, making naming consistent with admin controllers.

## Benefits

✅ **Better Organization** - Matches Symfony structure  
✅ **Easier Navigation** - Clear module separation  
✅ **Scalable** - Easy to add new modules  
✅ **Maintainable** - Logical grouping  
✅ **URL Alignment** - Folder structure matches URL paths  

## Notes

- URL paths remain unchanged (no breaking changes)
- All `@RequestMapping` annotations stay the same
- Only package declarations change
- Spring Boot will automatically scan subpackages

