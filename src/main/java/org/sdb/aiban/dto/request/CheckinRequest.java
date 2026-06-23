package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinRequest {

    @NotNull(message = "学习时长不能为空")
    @Min(value = 1, message = "学习时长至少1分钟")
    @Max(value = 480, message = "学习时长不能超过8小时")
    private Integer duration;

    private String note;
}
