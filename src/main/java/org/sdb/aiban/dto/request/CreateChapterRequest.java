package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChapterRequest {

    @NotNull(message = "技能ID不能为空")
    private Long skillId;

    @NotBlank(message = "章节标题不能为空")
    private String title;

    /**
     * 章节学习内容（Markdown格式）
     */
    private String content;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
