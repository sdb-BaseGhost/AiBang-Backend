package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.constant.UserConstants;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.common.validator.UserValidator;
import org.sdb.aiban.dto.request.LoginRequest;
import org.sdb.aiban.dto.request.RefreshTokenRequest;
import org.sdb.aiban.dto.request.RegisterRequest;
import org.sdb.aiban.dto.response.LoginResponse;
import org.sdb.aiban.dto.response.UserVO;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.mapper.UserMapper;
import org.sdb.aiban.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserValidator userValidator;

    public UserVO register(RegisterRequest request) {
        // 1. 校验用户名格式
        userValidator.validateUsername(request.getUsername());

        // 2. 校验密码强度
        userValidator.validatePassword(request.getPassword());

        // 3. 校验两次密码一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_MISMATCH);
        }

        // 4. 校验用户名唯一性
        SysUser existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (existingUser != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 5. 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail() != null ? request.getEmail() : "");
        user.setPhone(request.getPhone() != null ? request.getPhone() : "");
        user.setRole(UserConstants.ROLE_STUDENT);
        user.setStatus(UserConstants.STATUS_ACTIVE);
        user.setBio("");
        userMapper.insert(user);

        log.info("用户注册成功: {}", user.getUsername());

        return UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 校验账号状态
        if (UserConstants.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 生成 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功: {}", user.getUsername());

        UserVO userVO = UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(UserConstants.ACCESS_TOKEN_EXPIRATION / 1000)
                .user(userVO)
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // 1. 解析 refreshToken
        if (!jwtUtils.validateToken(request.getRefreshToken())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        var claims = jwtUtils.parseToken(request.getRefreshToken());
        String type = jwtUtils.getTokenType(claims);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 2. 查询用户
        Long userId = jwtUtils.getUserId(claims);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 生成新 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        UserVO userVO = UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(UserConstants.ACCESS_TOKEN_EXPIRATION / 1000)
                .user(userVO)
                .build();
    }

    public UserVO getCurrentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
}