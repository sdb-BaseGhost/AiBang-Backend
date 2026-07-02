package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSkillCategoryRequest {

    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    @Size(max = 50, message = "图标标识不能超过50个字符")
    private String icon;

    private Integer sortOrder;

    @Size(max = 500, message = "分类描述不能超过500个字符")
    private String description;
}