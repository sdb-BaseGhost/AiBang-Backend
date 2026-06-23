package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "STUDENT|ADMIN", message = "角色只能是 STUDENT 或 ADMIN")
    private String role;
}
