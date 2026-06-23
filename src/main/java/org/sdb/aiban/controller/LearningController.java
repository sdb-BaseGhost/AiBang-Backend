package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.response.*;
import org.sdb.aiban.service.LearningService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "学习数据看板", description = "学习时长统计、仪表盘、周报月报")
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<LearningDashboardVO> getDashboard(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(learningService.getDashboard(userId));
    }

    @Operation(summary = "获取学习时间线")
    @GetMapping("/timeline")
    public Result<PageResult<LearningTimelineVO>> getTimeline(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(learningService.getTimeline(userId, page, size));
    }

    @Operation(summary = "获取周报")
    @GetMapping("/report/weekly")
    public Result<WeeklyReportVO> getWeeklyReport(
            Authentication authentication,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(learningService.getWeeklyReport(userId, startDate, endDate));
    }

    @Operation(summary = "获取月报")
    @GetMapping("/report/monthly")
    public Result<MonthlyReportVO> getMonthlyReport(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(learningService.getMonthlyReport(userId, year, month));
    }
}
