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
    FILE_TYPE_INVALID(1011, "文件类型不支持"),

    // AI辅导模块 2001-2099
    CHAT_SESSION_NOT_FOUND(2001, "会话不存在"),
    CHAT_SESSION_FORBIDDEN(2002, "无权访问该会话"),
    DIFY_SERVICE_ERROR(2003, "服务器繁忙，请稍后再试"),
    DIFY_TIMEOUT(2004, "AI响应超时，请稍后再试"),

    // 简历模块 2100-2199
    RESUME_NOT_FOUND(2100, "简历不存在"),
    RESUME_FORBIDDEN(2101, "无权访问该简历"),
    RESUME_ALREADY_ANALYZING(2102, "简历正在分析中"),
    RESUME_UPLOAD_FAILED(2103, "简历上传失败"),
    RESUME_PDF_PARSE_ERROR(2104, "PDF文件解析失败");

    private final int code;
    private final String message;
}