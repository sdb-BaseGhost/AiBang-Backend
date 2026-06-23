package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSkillProgressRequest {

    @NotNull(message = "技能ID不能为空")
    private Long skillId;

    @NotNull(message = "进度不能为空")
    @Min(value = 0, message = "进度最小为0")
    @Max(value = 100, message = "进度最大为100")
    private Integer progress;

    @Min(value = 0, message = "评级最小为0")
    @Max(value = 5, message = "评级最大为5")
    private Integer rating;

    private String note;
}
