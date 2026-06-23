package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 20, message = "昵称不超过20字")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    @Size(max = 200, message = "个人简介不超过200字")
    private String bio;
}