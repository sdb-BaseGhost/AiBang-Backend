package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class SkillProgressVO {
    private Long skillId;
    private String skillName;
    private String categoryName;
    private String status;
    private Integer progress;
    private Integer rating;
    private LocalDateTime updateTime;
}
