package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResultVO {

    /**
     * 章节是否全部答对
     */
    private Boolean allCorrect;

    /**
     * 正确题数
     */
    private Integer correctCount;

    /**
     * 总题数
     */
    private Integer totalCount;

    /**
     * 技能最新进度（百分比）
     */
    private Integer skillProgress;

    /**
     * 每题的详细结果
     */
    private List<ExerciseDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseDetail {
        private Long exerciseId;
        private Integer userAnswer;
        private Integer correctAnswer;
        private Boolean isCorrect;
        private String explanation;
    }
}
