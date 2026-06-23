package org.sdb.aiban.common.validator;

import org.sdb.aiban.common.constant.UserConstants;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UserValidator {

    public void validateUsername(String username) {
        if (username == null || username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new BusinessException(ResultCode.USERNAME_INVALID, "用户名长度4-20位");
        }
        if (!username.matches(UserConstants.USERNAME_PATTERN)) {
            throw new BusinessException(ResultCode.USERNAME_INVALID, "用户名只允许字母、数字、下划线");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new BusinessException(ResultCode.PASSWORD_INVALID, "密码长度6-20位");
        }
        if (!password.matches(UserConstants.PASSWORD_PATTERN)) {
            throw new BusinessException(ResultCode.PASSWORD_INVALID, "密码必须包含字母和数字");
        }
    }

    public void validateAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的文件");
        }
        if (file.getSize() > UserConstants.AVATAR_MAX_SIZE) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE, "文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !UserConstants.AVATAR_ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_INVALID, "只支持jpg、png、gif格式");
        }
    }
}