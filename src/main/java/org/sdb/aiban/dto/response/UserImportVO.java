package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserImportVO {
    private Integer total;
    private Integer success;
    private Integer failed;
    private List<ImportFailure> failures;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ImportFailure {
        private Integer row;
        private String username;
        private String reason;
    }
}
