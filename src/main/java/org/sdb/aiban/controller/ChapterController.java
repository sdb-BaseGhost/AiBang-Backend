package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.CreateChapterRequest;
import org.sdb.aiban.dto.request.CreateExerciseRequest;
import org.sdb.aiban.dto.request.SubmitExerciseRequest;
import org.sdb.aiban.dto.request.UpdateChapterRequest;
import org.sdb.aiban.dto.response.ChapterVO;
import org.sdb.aiban.dto.response.ExerciseResultVO;
import org.sdb.aiban.dto.response.ExerciseVO;
import org.sdb.aiban.service.ChapterAdminService;
import org.sdb.aiban.service.ChapterService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "章节管理", description = "技能章节、练习题管理")
@RestController
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterAdminService chapterAdminService;

    // ==================== 学生端接口 ====================

    @Operation(summary = "获取技能的章节列表")
    @GetMapping("/skill/{skillId}")
    public Result<List<ChapterVO>> getChapters(
            Authentication authentication,
            @PathVariable Long skillId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chapterService.getChaptersWithProgress(userId, skillId));
    }

    @Operation(summary = "获取章节详情（含学习内容）")
    @GetMapping("/{chapterId}")
    public Result<ChapterVO> getChapterDetail(
            Authentication authentication,
            @PathVariable Long chapterId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chapterService.getChapterDetail(userId, chapterId));
    }

    @Operation(summary = "获取章节的练习题列表")
    @GetMapping("/{chapterId}/exercises")
    public Result<List<ExerciseVO>> getExercises(
            Authentication authentication,
            @PathVariable Long chapterId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chapterService.getExercises(userId, chapterId));
    }

    @Operation(summary = "提交练习答案")
    @PostMapping("/exercise/submit")
    public Result<ExerciseResultVO> submitExercise(
            Authentication authentication,
            @Valid @RequestBody SubmitExerciseRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chapterService.submitExercise(userId, request));
    }

    // ==================== 管理员接口 ====================

    @Operation(summary = "获取技能的章节列表（管理版）")
    @GetMapping("/admin/skill/{skillId}")
    public Result<List<ChapterVO>> getChaptersAdmin(@PathVariable Long skillId) {
        return Result.success(chapterAdminService.getChaptersBySkillId(skillId));
    }

    @Operation(summary = "创建章节")
    @PostMapping("/admin")
    public Result<ChapterVO> createChapter(@Valid @RequestBody CreateChapterRequest request) {
        return Result.success(chapterAdminService.createChapter(request));
    }

    @Operation(summary = "更新章节")
    @PutMapping("/admin/{chapterId}")
    public Result<ChapterVO> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody UpdateChapterRequest request) {
        return Result.success(chapterAdminService.updateChapter(chapterId, request));
    }

    @Operation(summary = "删除章节")
    @DeleteMapping("/admin/{chapterId}")
    public Result<Void> deleteChapter(@PathVariable Long chapterId) {
        chapterAdminService.deleteChapter(chapterId);
        return Result.success();
    }

    @Operation(summary = "添加练习题")
    @PostMapping("/admin/exercise")
    public Result<Void> addExercise(@Valid @RequestBody CreateExerciseRequest request) {
        chapterAdminService.addExercise(request);
        return Result.success();
    }

    @Operation(summary = "删除练习题")
    @DeleteMapping("/admin/exercise/{exerciseId}")
    public Result<Void> deleteExercise(@PathVariable Long exerciseId) {
        chapterAdminService.deleteExercise(exerciseId);
        return Result.success();
    }
}
