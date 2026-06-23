package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class CheckinResponse {
    private Long checkinId;
    private LocalDate date;
    private Integer duration;
    private Integer streak;
    private Integer totalCheckins;
}
