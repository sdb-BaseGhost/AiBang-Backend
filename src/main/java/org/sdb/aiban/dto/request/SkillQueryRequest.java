package org.sdb.aiban.dto.request;

import lombok.Data;

@Data
public class SkillQueryRequest {
    private Long categoryId;
    private String keyword;
    private String level;
    private Integer page = 1;
    private Integer size = 10;
}