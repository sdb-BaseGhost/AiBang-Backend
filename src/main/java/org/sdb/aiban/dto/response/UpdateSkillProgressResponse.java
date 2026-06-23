package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdateSkillProgressResponse {
    private Long skillId;
    private String skillName;
    private Integer progress;
    private String status;
    private Double overallProgress;
}
