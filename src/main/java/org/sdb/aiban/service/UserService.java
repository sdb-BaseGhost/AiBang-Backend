package org.sdb.aiban.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.common.validator.UserValidator;
import org.sdb.aiban.dto.request.ChangePasswordRequest;
import org.sdb.aiban.dto.request.UpdateProfileRequest;
import org.sdb.aiban.dto.response.FileUploadResponse;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新非空字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userMapper.updateById(user);

        return buildUserResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 校验新密码强度
        userValidator.validatePassword(request.getNewPassword());

        // 校验新密码与原密码不同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.SAME_PASSWORD);
        }

        // 校验两次密码一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_MISMATCH);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("用户修改密码成功: {}", user.getUsername());
    }

    public FileUploadResponse uploadAvatar(Long userId, MultipartFile file) {
        // 校验文件
        userValidator.validateAvatar(file);

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        try {
            // 生成存储路径
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String fileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
            String relativePath = "uploads/avatars/" + userId + "/" + datePath + "/" + fileName;

            // 创建目录
            Path basePath = Paths.get("./uploads/avatars/" + userId + "/" + datePath);
            Files.createDirectories(basePath);

            // 保存文件
            Path filePath = basePath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 更新用户头像
            user.setAvatar("/" + relativePath);
            userMapper.updateById(user);

            log.info("用户上传头像成功: {}", user.getUsername());

            return FileUploadResponse.builder()
                    .url("/" + relativePath)
                    .filename(fileName)
                    .build();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0 ? filename.substring(dotIndex) : ".jpg";
    }

    private UserResponse buildUserResponse(SysUser user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(maskPhone(user.getPhone()))
                .role(user.getRole())
                .bio(user.getBio())
                .createTime(user.getCreateTime())
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}