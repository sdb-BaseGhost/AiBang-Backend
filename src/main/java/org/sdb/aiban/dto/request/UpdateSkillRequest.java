package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSkillRequest {

    @Size(max = 100, message = "技能名称不能超过100个字符")
    private String name;

    private Long categoryId;

    private String level;

    @Size(max = 1000, message = "技能描述不能超过1000个字符")
    private String description;

    private Integer sortOrder;

    private Long parentSkillId;
}