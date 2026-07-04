package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.ChangeRoleRequest;
import org.sdb.aiban.dto.request.ChangeStatusRequest;
import org.sdb.aiban.dto.request.UserQueryRequest;
import org.sdb.aiban.dto.response.AdminDashboardVO;
import org.sdb.aiban.dto.response.LearningRecordVO;
import org.sdb.aiban.dto.response.UserImportVO;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.service.AdminService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "管理员接口", description = "仪表盘、用户管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> getDashboard(
            @RequestParam(required = false) Long userId) {
        return Result.success(adminService.getDashboard(userId));
    }

    @Operation(summary = "获取学习记录", description = "分页查询已完成的章节学习记录，支持按用户筛选")
    @GetMapping("/learning-records")
    public Result<PageResult<LearningRecordVO>> getLearningRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.getLearningRecords(userId, page, size));
    }

    @Operation(summary = "获取用户列表", description = "分页查询用户，支持按用户名、角色、状态筛选")
    @GetMapping("/users")
    public Result<PageResult<UserResponse>> getUserList(UserQueryRequest request) {
        PageResult<UserResponse> result = adminService.getUserList(request);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/users/{id}")
    public Result<UserResponse> getUserDetail(@PathVariable Long id) {
        UserResponse user = adminService.getUserDetail(id);
        return Result.success(user);
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/users/{id}/role")
    public Result<Void> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request) {
        adminService.changeUserRole(id, request);
        return Result.success();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> changeUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        adminService.changeUserStatus(id, request);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/users/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        String tempPassword = adminService.resetPassword(id);
        return Result.success(tempPassword);
    }

    @Operation(summary = "批量导入用户")
    @PostMapping("/user/import")
    public Result<UserImportVO> importUsers(@RequestParam("file") MultipartFile file) {
        UserImportVO result = adminService.importUsers(file);
        return Result.success(result);
    }

    @Operation(summary = "导出用户数据")
    @GetMapping("/user/export")
    public void exportUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws IOException {
        Workbook workbook = adminService.exportUsers(role, status);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
            "attachment;filename=" + URLEncoder.encode("用户数据.xlsx", StandardCharsets.UTF_8));
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
