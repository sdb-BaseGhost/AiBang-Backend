package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.CheckinRequest;
import org.sdb.aiban.dto.response.CheckinRecordsVO;
import org.sdb.aiban.dto.response.CheckinResponse;
import org.sdb.aiban.service.CheckinService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学习打卡", description = "每日打卡、打卡记录")
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @Operation(summary = "今日打卡")
    @PostMapping
    public Result<CheckinResponse> checkin(
            Authentication authentication,
            @Valid @RequestBody CheckinRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(checkinService.checkin(userId, request));
    }

    @Operation(summary = "获取打卡记录")
    @GetMapping("/records")
    public Result<CheckinRecordsVO> getRecords(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(checkinService.getRecords(userId, year, month));
    }
}
