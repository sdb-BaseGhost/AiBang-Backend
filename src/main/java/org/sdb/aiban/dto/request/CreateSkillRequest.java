package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSkillRequest {

    @NotBlank(message = "技能名称不能为空")
    @Size(max = 100, message = "技能名称不能超过100个字符")
    private String name;

    @NotNull(message = "所属分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "难度级别不能为空")
    private String level;

    @Size(max = 1000, message = "技能描述不能超过1000个字符")
    private String description;

    private Integer sortOrder;

    private Long parentSkillId;
}