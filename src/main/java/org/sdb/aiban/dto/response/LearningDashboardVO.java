package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LearningDashboardVO {
    private DashboardSummary summary;
    private List<DailyStudyHours> weeklyStudyHours;
    private List<CategoryProgress> categoryProgress;

    @Data
    @Builder
    @AllArgsConstructor
    public static class DashboardSummary {
        private Double totalStudyHours;
        private Integer todayStudyMinutes;
        private Integer completedSkills;
        private Integer totalSkills;
        private Double skillProgress;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class DailyStudyHours {
        private String date;
        private Double hours;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CategoryProgress {
        private String categoryName;
        private Double progress;
    }
}
