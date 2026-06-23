package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FileUploadResponse {
    private String url;
    private String filename;
}