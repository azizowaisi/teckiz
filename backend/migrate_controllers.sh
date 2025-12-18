#!/bin/bash
# Script to reorganize controllers into folder structure
# Run from backend directory

CONTROLLER_DIR="src/main/java/com/teckiz/controller"
cd "$CONTROLLER_DIR" || exit

echo "Reorganizing controllers..."

# Create new folder structure (already created, but ensure it exists)
mkdir -p admin/website admin/journal admin/education admin/superadmin public

# Function to move and update package
move_controller() {
    local file=$1
    local target_dir=$2
    local package=$3
    
    if [ -f "$file" ]; then
        echo "Moving $file to $target_dir/"
        mv "$file" "$target_dir/"
        
        # Update package declaration
        sed -i '' "s/^package com\.teckiz\.controller;$/package com.teckiz.controller.$package;/" "$target_dir/$file"
        echo "  ✓ Updated package to: com.teckiz.controller.$package"
    fi
}

# Website Admin Controllers
echo -e "\n=== Moving Website Admin Controllers ==="
move_controller "WebPageController.java" "admin/website" "admin.website"
move_controller "WebNewsController.java" "admin/website" "admin.website"
move_controller "WebNewsTypeController.java" "admin/website" "admin.website"
move_controller "WebAlbumController.java" "admin/website" "admin.website"
move_controller "WebEventController.java" "admin/website" "admin.website"
move_controller "WebContactsController.java" "admin/website" "admin.website"
move_controller "WebContactTypeController.java" "admin/website" "admin.website"
move_controller "WebRelatedMediaController.java" "admin/website" "admin.website"
move_controller "CompanyModuleMapperMenuController.java" "admin/website" "admin.website"
move_controller "WebsiteDashboardController.java" "admin/website" "admin.website"
move_controller "WebSubscriberController.java" "admin/website" "admin.website"
move_controller "WebWidgetController.java" "admin/website" "admin.website"

# Journal Admin Controllers
echo -e "\n=== Moving Journal Admin Controllers ==="
move_controller "ResearchJournalController.java" "admin/journal" "admin.journal"
move_controller "ResearchJournalVolumeController.java" "admin/journal" "admin.journal"
move_controller "ResearchArticleController.java" "admin/journal" "admin.journal"
move_controller "ResearchArticleAuthorController.java" "admin/journal" "admin.journal"
move_controller "ResearchArticleReviewerController.java" "admin/journal" "admin.journal"
move_controller "ResearchArticleTypeController.java" "admin/journal" "admin.journal"

# Education Admin Controllers
echo -e "\n=== Moving Education Admin Controllers ==="
move_controller "FacilityController.java" "admin/education" "admin.education"
move_controller "StoryController.java" "admin/education" "admin.education"
move_controller "StoryTypeController.java" "admin/education" "admin.education"
move_controller "SkillController.java" "admin/education" "admin.education"
move_controller "PrincipalMessageController.java" "admin/education" "admin.education"

# SuperAdmin Controllers
echo -e "\n=== Moving SuperAdmin Controllers ==="
move_controller "SuperAdminController.java" "admin/superadmin" "admin.superadmin"
move_controller "CompanyController.java" "admin/superadmin" "admin.superadmin"
move_controller "CompanyUserController.java" "admin/superadmin" "admin.superadmin"
move_controller "CompanyModuleController.java" "admin/superadmin" "admin.superadmin"
move_controller "CompanyRoleController.java" "admin/superadmin" "admin.superadmin"
move_controller "RoleController.java" "admin/superadmin" "admin.superadmin"

# Public Controllers
echo -e "\n=== Moving Public Controllers ==="
move_controller "PublicWebPageController.java" "public" "public"
move_controller "PublicWebNewsController.java" "public" "public"
move_controller "PublicWebAlbumController.java" "public" "public"
move_controller "PublicWebEventController.java" "public" "public"
move_controller "PublicResearchArticleController.java" "public" "public"

echo -e "\n✅ Controller reorganization complete!"
echo "Note: AuthController.java remains in root controller directory"

