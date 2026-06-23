package org.sdb.aiban.common.constant;

import java.util.List;

public class UserConstants {

    // 用户名规则
    public static final int USERNAME_MIN_LENGTH = 4;
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    // 密码规则
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).+$";

    // 昵称规则
    public static final int NICKNAME_MAX_LENGTH = 20;

    // 个人简介规则
    public static final int BIO_MAX_LENGTH = 200;

    // 文件上传规则
    public static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024; // 5MB
    public static final List<String> AVATAR_ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    // 用户状态
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLED = "DISABLED";

    // 用户角色
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMIN = "ADMIN";

    // Token 过期时间
    public static final long ACCESS_TOKEN_EXPIRATION = 7200000; // 2小时
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7天
}