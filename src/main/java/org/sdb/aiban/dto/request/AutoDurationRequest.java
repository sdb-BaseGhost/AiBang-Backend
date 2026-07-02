package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AutoDurationRequest {

    @Min(value = 1, message = "时长必须大于0")
    @Max(value = 120, message = "时长不能超过120分钟")
    private Integer duration;

    @NotBlank(message = "页面类型不能为空")
    private String pageType;

    private String enterTime;

    private String leaveTime;
}