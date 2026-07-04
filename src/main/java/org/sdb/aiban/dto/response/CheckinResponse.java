package org.sdb.aiban.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Integer streak;

    private Integer durationMinutes;
}
