package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DailyDurationRequest {

    @NotNull(message = "学习时长不能为空")
    @Min(value = 30, message = "学习时长只能选择 30/60/90/120 分钟")
    @Max(value = 120, message = "学习时长只能选择 30/60/90/120 分钟")
    private Integer durationMinutes;
}
