package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.AutoDurationRequest;
import org.sdb.aiban.dto.request.DailyDurationRequest;
import org.sdb.aiban.dto.response.CheckinRecordsVO;
import org.sdb.aiban.dto.response.CheckinResponse;
import org.sdb.aiban.service.CheckinRedisService;
import org.sdb.aiban.service.DailyStudyDurationService;
import org.sdb.aiban.service.LearningService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "学习打卡", description = "每日打卡、打卡记录")
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinRedisService checkinRedisService;
    private final LearningService learningService;
    private final DailyStudyDurationService dailyStudyDurationService;

    @Operation(summary = "每日打卡")
    @PostMapping
    public Result<CheckinResponse> checkin(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(checkinRedisService.checkin(userId));
    }

    @Operation(summary = "打卡+选择学习时长")
    @PostMapping("/daily")
    public Result<CheckinResponse> checkinWithDailyDuration(
            Authentication authentication,
            @Valid @RequestBody DailyDurationRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(checkinRedisService.checkinWithDailyDuration(userId, request.getDurationMinutes()));
    }

    @Operation(summary = "查询今日打卡状态")
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStatus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean hasCheckedIn = checkinRedisService.hasCheckedInToday(userId);
        Integer todayDuration = null;
        if (hasCheckedIn) {
            todayDuration = dailyStudyDurationService.getTodayDuration(userId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("hasCheckedIn", hasCheckedIn);
        result.put("todayDurationMinutes", todayDuration != null ? todayDuration : 0);
        return Result.success(result);
    }

    @Operation(summary = "获取月度打卡记录")
    @GetMapping("/records")
    public Result<CheckinRecordsVO> getRecords(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(checkinRedisService.getRecords(userId, year, month));
    }

    @Operation(summary = "上报自动学习时长")
    @PostMapping("/auto-duration")
    public Result<Map<String, Object>> reportAutoDuration(
            Authentication authentication,
            @Valid @RequestBody AutoDurationRequest request) {
        Long userId = (Long) authentication.getPrincipal();

        String title = "SKILL_TREE".equals(request.getPageType())
                ? "技能树页面停留"
                : "技能详情页面停留";

        String detail = String.format("页面类型: %s, 进入: %s, 离开: %s",
                request.getPageType(),
                request.getEnterTime() != null ? request.getEnterTime() : "",
                request.getLeaveTime() != null ? request.getLeaveTime() : "");

        learningService.createRecord(userId, "PAGE_STAY", title, request.getDuration(), detail, null);

        return Result.success(Map.of(
                "date", LocalDate.now().toString(),
                "duration", request.getDuration()
        ));
    }
}
