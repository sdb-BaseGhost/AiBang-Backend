package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skill")
public class Skill extends BaseEntity {

    /**
     * 技能名称
     */
    private String name;

    /**
     * 所属分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 难度级别：BEGINNER/INTERMEDIATE/ADVANCED/EXPERT
     */
    private String level;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 排序顺序，越大越靠前
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 父技能ID，用于构建树形结构
     */
    @TableField("parent_skill_id")
    private Long parentSkillId;
}