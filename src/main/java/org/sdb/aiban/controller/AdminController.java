package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.ChangeRoleRequest;
import org.sdb.aiban.dto.request.ChangeStatusRequest;
import org.sdb.aiban.dto.request.UserQueryRequest;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.service.AdminService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员接口", description = "用户管理、角色修改、状态控制")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
}
