package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.UpdateSkillProgressRequest;
import org.sdb.aiban.dto.response.SkillCategoryVO;
import org.sdb.aiban.dto.response.SkillProgressSummaryVO;
import org.sdb.aiban.dto.response.SkillTreeVO;
import org.sdb.aiban.dto.response.UpdateSkillProgressResponse;
import org.sdb.aiban.service.SkillService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学生端-技能树", description = "学生端技能树、学习进度")
@RestController
@RequestMapping("/api/skill")
@RequiredArgsConstructor
public class StudentSkillController {

    private final SkillService skillService;

    @Operation(summary = "获取技能分类列表")
    @GetMapping("/categories")
    public Result<List<SkillCategoryVO>> getCategories() {
        return Result.success(skillService.getCategories());
    }

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
}

