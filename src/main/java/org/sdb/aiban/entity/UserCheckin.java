package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_checkin")
public class UserCheckin extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("checkin_date")
    private LocalDate checkinDate;

    private Integer duration;

    private String note;

    private Integer streak;
}
