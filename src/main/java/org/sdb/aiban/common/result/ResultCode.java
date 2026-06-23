package org.sdb.aiban.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部异常"),

    // 用户模块 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已被注册"),
    PASSWORD_ERROR(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    USERNAME_INVALID(1005, "用户名格式不正确"),
    PASSWORD_INVALID(1006, "密码格式不正确"),
    PASSWORD_MISMATCH(1007, "两次输入的密码不一致"),
    OLD_PASSWORD_ERROR(1008, "原密码不正确"),
    SAME_PASSWORD(1009, "新密码不能与原密码相同"),
    FILE_TOO_LARGE(1010, "文件大小超过限制"),
    FILE_TYPE_INVALID(1011, "文件类型不支持");

    private final int code;
    private final String message;
}