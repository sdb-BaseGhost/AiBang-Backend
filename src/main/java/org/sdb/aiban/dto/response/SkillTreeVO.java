package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SkillTreeVO {
    private Long skillId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String level;
    private String levelLabel;
    private List<SkillTreeVO> children;
}
