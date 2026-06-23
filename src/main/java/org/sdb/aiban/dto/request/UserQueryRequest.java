package org.sdb.aiban.dto.request;

import lombok.Data;

@Data
public class UserQueryRequest {
    private String username;
    private String role;
    private String status;
    private Integer page = 1;
    private Integer size = 10;
}
