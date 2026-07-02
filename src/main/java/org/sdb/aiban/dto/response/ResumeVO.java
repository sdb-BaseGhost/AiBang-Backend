package org.sdb.aiban.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeVO {
    private Long resumeId;
    private String title;
    private String status;
    private String optimizedContent;  // Markdown格式
    private Double elapsedTime;
    private Integer totalTokens;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
