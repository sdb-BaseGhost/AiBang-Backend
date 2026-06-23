package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AdminDashboardVO {
    private UserStats userStats;
    private LearningStats learningStats;
    private SkillStats skillStats;
    private List<DailyTrend> weeklyTrend;

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserStats {
        private Long totalUsers;
        private Long todayNewUsers;
        private Long activeUsers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class LearningStats {
        private Double totalStudyHours;
        private Integer todayStudyMinutes;
        private Long totalCheckins;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class SkillStats {
        private Integer totalSkills;
        private Double avgCompletionRate;
        private List<TopSkill> topSkills;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class TopSkill {
        private String skillName;
        private Double completionRate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;
        private Long users;
        private Long hours;
    }
}
