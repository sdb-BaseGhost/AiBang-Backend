package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_study_duration")
public class DailyStudyDuration extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("study_date")
    private LocalDate studyDate;

    @TableField("duration_minutes")
    private Integer durationMinutes;
}
