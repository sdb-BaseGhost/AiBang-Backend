package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SkillCategoryVO {
    private Long categoryId;
    private String name;
    private String icon;
    private Integer sortOrder;
    private Integer skillCount;
}
