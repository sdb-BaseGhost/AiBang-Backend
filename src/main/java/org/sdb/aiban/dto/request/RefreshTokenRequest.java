package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}