package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateExerciseRequest {

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @NotBlank(message = "题目内容不能为空")
    private String question;

    @NotEmpty(message = "选项不能为空")
    private List<String> options;

    @NotNull(message = "正确答案不能为空")
    private Integer answer;

    /**
     * 答案解析
     */
    private String explanation;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
