package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LearningRecordVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long chapterId;
    private String chapterTitle;
    private String skillName;
    private Integer score;
    private LocalDateTime updateTime;
}
