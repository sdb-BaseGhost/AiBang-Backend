package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitExerciseRequest {

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    /**
     * 用户答案列表 [{"exerciseId": 1, "answer": 0}, ...]
     */
    @NotNull(message = "答案不能为空")
    private List<ExerciseAnswer> answers;

    @Data
    public static class ExerciseAnswer {
        @NotNull(message = "练习题ID不能为空")
        private Long exerciseId;

        @NotNull(message = "答案不能为空")
        private Integer answer;
    }
}
