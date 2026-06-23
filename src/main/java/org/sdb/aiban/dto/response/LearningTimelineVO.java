package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LearningTimelineVO {
    private Long id;
    private String type;
    private String title;
    private Integer duration;
    private String detail;
    private LocalDateTime createTime;
}
