package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterVO {

    private Long chapterId;

    private Long skillId;

    private String title;

    /**
     * 章节学习内容（Markdown格式）
     */
    private String content;

    private Integer sortOrder;

    private String status;

    /**
     * 练习题数量
     */
    private Integer exerciseCount;

    /**
     * 用户状态：NOT_STARTED/IN_PROGRESS/COMPLETED
     */
    private String userStatus;

    private LocalDateTime createTime;
}
