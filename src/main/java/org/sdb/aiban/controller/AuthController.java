package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.LoginRequest;
import org.sdb.aiban.dto.request.RefreshTokenRequest;
import org.sdb.aiban.dto.request.RegisterRequest;
import org.sdb.aiban.dto.response.LoginResponse;
import org.sdb.aiban.dto.response.UserVO;
import org.sdb.aiban.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "注册、登录、Token刷新")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO userVO = authService.register(request);
        return Result.success(userVO);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return Result.success(loginResponse);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.refreshToken(request);
        return Result.success(loginResponse);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserVO userVO = authService.getCurrentUser(userId);
        return Result.success(userVO);
    }
}