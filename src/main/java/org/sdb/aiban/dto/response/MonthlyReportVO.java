package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MonthlyReportVO {
    private String period;
    private Double totalStudyHours;
    private Integer studyDays;
    private Double avgDailyHours;
    private Integer skillsCompleted;
    private Integer skillsInProgress;
    private List<CategoryHours> categoryBreakdown;
    private String summary;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CategoryHours {
        private String categoryName;
        private Double hours;
    }
}
