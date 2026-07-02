package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_chapter_progress")
public class UserChapterProgress extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 章节ID
     */
    @TableField("chapter_id")
    private Long chapterId;

    /**
     * 练习得分（100分制）
     */
    private Integer score;

    /**
     * 状态：NOT_STARTED-未开始，COMPLETED-已完成
     */
    private String status;
}
