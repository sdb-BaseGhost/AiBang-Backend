package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}