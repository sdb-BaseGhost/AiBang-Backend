package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CheckinRecordsVO {
    private List<CheckinDay> records;
    private Integer totalCheckins;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer monthTotalMinutes;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CheckinDay {
        private LocalDate date;
        private Integer duration;
        private Integer streak;
    }
}
