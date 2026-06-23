package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class WeeklyReportVO {
    private String period;
    private Double totalStudyHours;
    private Integer studyDays;
    private Double avgDailyHours;
    private Integer skillsCompleted;
    private Integer skillsInProgress;
    private String topSkill;
    private List<String> recommendations;
}
