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
public class ExerciseVO {

    private Long exerciseId;

    private String question;

    /**
     * 选项列表
     */
    private List<String> options;

    /**
     * 用户提交答案后才返回
     */
    private Integer correctAnswer;

    /**
     * 用户选择的答案
     */
    private Integer userAnswer;

    /**
     * 答案解析（用户提交答案后才返回）
     */
    private String explanation;

    private Integer sortOrder;
}
