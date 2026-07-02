package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinRecordsVO {
    
    private List<LocalDate> records;
    
    private Integer totalCheckins;
    
    private Integer currentStreak;
    
    private Integer longestStreak;
}
