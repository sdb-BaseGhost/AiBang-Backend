package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_resume")
public class AiResume extends BaseEntity {
    private Long userId;
    private String title;
    private String originalContent;
    private String optimizedContent;
    private String status;  // UPLOADED, ANALYZING, ANALYZED, ANALYZE_FAILED
    private String errorMessage;
    private String taskId;
    private String workflowRunId;
    private Double elapsedTime;
    private Integer totalTokens;
}
