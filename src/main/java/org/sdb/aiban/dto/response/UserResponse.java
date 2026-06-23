package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String role;
    private String bio;
    private LocalDateTime createTime;
}