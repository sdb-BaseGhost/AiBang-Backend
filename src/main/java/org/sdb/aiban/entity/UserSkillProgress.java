package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_skill_progress")
public class UserSkillProgress extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("skill_id")
    private Long skillId;

    private Integer progress;

    private String status;

    private Integer rating;

    private String note;
}
