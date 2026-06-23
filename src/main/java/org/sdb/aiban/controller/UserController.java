package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.ChangePasswordRequest;
import org.sdb.aiban.dto.request.UpdateProfileRequest;
import org.sdb.aiban.dto.response.FileUploadResponse;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理", description = "个人资料、密码修改、头像上传")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse userResponse = userService.updateProfile(userId, request);
        return Result.success(userResponse);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<FileUploadResponse> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        FileUploadResponse response = userService.uploadAvatar(userId, file);
        return Result.success(response);
    }
}