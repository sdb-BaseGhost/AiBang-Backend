package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chapter_exercise")
public class ChapterExercise extends BaseEntity {

    /**
     * 所属章节ID
     */
    @TableField("chapter_id")
    private Long chapterId;

    /**
     * 题目内容
     */
    private String question;

    /**
     * 选项列表 ["选项A", "选项B", "选项C", "选项D"]
     */
    private String options;

    /**
     * 正确答案索引（0-3）
     */
    private Integer answer;

    /**
     * 答案解析
     */
    private String explanation;

    /**
     * 排序顺序，越小越靠前
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
