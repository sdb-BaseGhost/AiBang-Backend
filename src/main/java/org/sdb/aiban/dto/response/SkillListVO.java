package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class SkillListVO {
    private Long skillId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String level;
    private String levelLabel;
    private String description;
    private Integer userCount;
    private Double completionRate;
    private LocalDateTime createTime;
}