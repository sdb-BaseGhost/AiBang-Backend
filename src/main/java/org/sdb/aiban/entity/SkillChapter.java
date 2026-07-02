package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skill_chapter")
public class SkillChapter extends BaseEntity {

    /**
     * 所属技能ID
     */
    @TableField("skill_id")
    private Long skillId;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节学习内容（Markdown格式）
     */
    private String content;

    /**
     * 排序顺序，越小越靠前
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：DRAFT-草稿，PUBLISHED-已发布
     */
    private String status;
}
