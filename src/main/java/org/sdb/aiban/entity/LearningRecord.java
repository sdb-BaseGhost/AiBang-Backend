package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learning_record")
public class LearningRecord extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    private String type;

    private String title;

    private Integer duration;

    private String detail;

    @TableField("skill_id")
    private Long skillId;
}
