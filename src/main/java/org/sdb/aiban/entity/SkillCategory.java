package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skill_category")
public class SkillCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标标识
     */
    private String icon;

    /**
     * 排序顺序，越大越靠前
     */
    private Integer sortOrder;

    /**
     * 分类描述
     */
    private String description;
}