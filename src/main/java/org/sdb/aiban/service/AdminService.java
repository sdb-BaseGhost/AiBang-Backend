package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.ChangeRoleRequest;
import org.sdb.aiban.dto.request.ChangeStatusRequest;
import org.sdb.aiban.dto.request.UserQueryRequest;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserResponse> getUserList(UserQueryRequest request) {
        Page<SysUser> page = new Page<>(request.getPage(), request.getSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 模糊搜索用户名
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysUser::getUsername, request.getUsername());
        }

        // 筛选角色
        if (StringUtils.hasText(request.getRole())) {
            wrapper.eq(SysUser::getRole, request.getRole());
        }

        // 筛选状态
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(SysUser::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = userMapper.selectPage(page, wrapper);

        // 转换为 UserResponse
        Page<UserResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::buildUserResponse)
                .toList());

        return PageResult.from(responsePage);
    }

    /**
     * 获取用户详情
     */
    public UserResponse getUserDetail(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return buildUserResponse(user);
    }

    /**
     * 修改用户角色
     */
    public void changeUserRole(Long userId, ChangeRoleRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String oldRole = user.getRole();
        user.setRole(request.getRole());
        userMapper.updateById(user);

        log.info("管理员修改用户角色: {} -> {} -> {}", user.getUsername(), oldRole, request.getRole());
    }

    /**
     * 启用/禁用用户
     */
    public void changeUserStatus(Long userId, ChangeStatusRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        userMapper.updateById(user);

        log.info("管理员修改用户状态: {} -> {} -> {}", user.getUsername(), oldStatus, request.getStatus());
    }

    /**
     * 构建用户响应（包含完整信息，管理员可见）
     */
    private UserResponse buildUserResponse(SysUser user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone()) // 管理员看到完整手机号
                .role(user.getRole())
                .bio(user.getBio())
                .createTime(user.getCreateTime())
                .build();
    }
}
