package org.sdb.aiban.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinRecordsVO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> records;

    private Map<String, Integer> dailyDurations;

    private Integer totalCheckins;
    
    private Integer currentStreak;
    
    private Integer longestStreak;
}
