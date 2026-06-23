package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SkillProgressSummaryVO {
    private Integer totalSkills;
    private Integer completedCount;
    private Integer inProgressCount;
    private Integer notStartedCount;
    private Double overallProgress;
    private List<SkillProgressVO> skillProgressList;
}
