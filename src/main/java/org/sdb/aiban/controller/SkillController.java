package org.sdb.aiban.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.*;
import org.sdb.aiban.dto.response.*;
import org.sdb.aiban.service.SkillAdminService;
import org.sdb.aiban.service.SkillCategoryService;
import org.sdb.aiban.service.SkillService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "技能树管理", description = "技能分类、技能树、学习进度")
@RestController
@RequestMapping("/api/admin/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final SkillCategoryService skillCategoryService;
    private final SkillAdminService skillAdminService;

    // ==================== 学生端接口 ====================

    @Operation(summary = "获取技能树")
    @GetMapping("/tree")
    public Result<List<SkillTreeVO>> getSkillTree(
            @RequestParam(required = false) Long categoryId) {
        return Result.success(skillService.getSkillTree(categoryId));
    }

    @Operation(summary = "获取我的技能进度")
    @GetMapping("/user/progress")
    public Result<SkillProgressSummaryVO> getMyProgress(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(skillService.getMyProgress(userId));
    }

    @Operation(summary = "更新技能进度")
    @PutMapping("/user/progress")
    public Result<UpdateSkillProgressResponse> updateProgress(
            Authentication authentication,
            @Valid @RequestBody UpdateSkillProgressRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(skillService.updateProgress(userId, request));
    }

    // ==================== 管理员接口 - 技能分类 ====================

    @Operation(summary = "获取技能分类列表（管理版）")
    @GetMapping("/categories")
    public Result<List<SkillCategoryVO>> getCategories() {
        return Result.success(skillCategoryService.getCategories());
    }

    @Operation(summary = "创建技能分类")
    @PostMapping("/category")
    public Result<SkillCategoryVO> createCategory(
            @Valid @RequestBody CreateSkillCategoryRequest request) {
        return Result.success(skillCategoryService.createCategory(request));
    }

    @Operation(summary = "更新技能分类")
    @PutMapping("/category/{categoryId}")
    public Result<SkillCategoryVO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateSkillCategoryRequest request) {
        return Result.success(skillCategoryService.updateCategory(categoryId, request));
    }

    @Operation(summary = "删除技能分类")
    @DeleteMapping("/category/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        skillCategoryService.deleteCategory(categoryId);
        return Result.success();
    }

    // ==================== 管理员接口 - 技能管理 ====================

    @Operation(summary = "获取技能列表（管理版）")
    @GetMapping("/list")
    public Result<Page<SkillListVO>> getSkillList(SkillQueryRequest request) {
        return Result.success(skillAdminService.getSkillList(request));
    }

    @Operation(summary = "创建技能")
    @PostMapping
    public Result<SkillListVO> createSkill(
            @Valid @RequestBody CreateSkillRequest request) {
        return Result.success(skillAdminService.createSkill(request));
    }

    @Operation(summary = "更新技能")
    @PutMapping("/{skillId}")
    public Result<SkillListVO> updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody UpdateSkillRequest request) {
        return Result.success(skillAdminService.updateSkill(skillId, request));
    }

    @Operation(summary = "删除技能")
    @DeleteMapping("/{skillId}")
    public Result<Void> deleteSkill(@PathVariable Long skillId) {
        skillAdminService.deleteSkill(skillId);
        return Result.success();
    }

    @Operation(summary = "获取技能统计")
    @GetMapping("/stats")
    public Result<SkillStatsVO> getSkillStats() {
        return Result.success(skillAdminService.getSkillStats());
    }
}
