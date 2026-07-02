package org.sdb.aiban.dto.request;

import lombok.Data;

@Data
public class UpdateChapterRequest {

    private String title;

    private String content;

    private Integer sortOrder;

    /**
     * 状态：DRAFT/PUBLISHED
     */
    private String status;
}
