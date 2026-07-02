package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SkillStatsVO {
    private Integer totalCategories;
    private Integer totalSkills;
    private Double avgCompletionRate;
    private List<TopSkill> topCompletedSkills;
    private List<TopSkill> leastCompletedSkills;
    private List<CategoryStat> categoryStats;

    @Data
    @Builder
    @AllArgsConstructor
    public static class TopSkill {
        private String skillName;
        private Double completionRate;
        private Integer userCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CategoryStat {
        private String categoryName;
        private Integer skillCount;
        private Double avgCompletion;
    }
}