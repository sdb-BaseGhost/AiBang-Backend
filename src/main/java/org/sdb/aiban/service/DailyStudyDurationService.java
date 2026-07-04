package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.entity.DailyStudyDuration;
import org.sdb.aiban.mapper.DailyStudyDurationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStudyDurationService {

    private final DailyStudyDurationMapper dailyStudyDurationMapper;

    /**
     * 保存每日学习时长（打卡时调用，每天只能写一次）
     */
    public void save(Long userId, LocalDate studyDate, int durationMinutes) {
        DailyStudyDuration record = new DailyStudyDuration();
        record.setUserId(userId);
        record.setStudyDate(studyDate);
        record.setDurationMinutes(durationMinutes);
        dailyStudyDurationMapper.insert(record);
        log.info("保存每日学习时长: userId={}, date={}, duration={}min", userId, studyDate, durationMinutes);
    }

    /**
     * 查询今日学习时长
     */
    public Integer getTodayDuration(Long userId) {
        DailyStudyDuration record = dailyStudyDurationMapper.selectOne(
            new LambdaQueryWrapper<DailyStudyDuration>()
                .eq(DailyStudyDuration::getUserId, userId)
                .eq(DailyStudyDuration::getStudyDate, LocalDate.now()));
        return record != null ? record.getDurationMinutes() : null;
    }

    /**
     * 查询指定月份每天的学习时长（用于日历显示）
     */
    public Map<String, Integer> getMonthlyDurations(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<DailyStudyDuration> records = dailyStudyDurationMapper.selectList(
            new LambdaQueryWrapper<DailyStudyDuration>()
                .eq(DailyStudyDuration::getUserId, userId)
                .ge(DailyStudyDuration::getStudyDate, startDate)
                .le(DailyStudyDuration::getStudyDate, endDate));

        Map<String, Integer> map = new LinkedHashMap<>();
        for (DailyStudyDuration r : records) {
            map.put(r.getStudyDate().toString(), r.getDurationMinutes());
        }
        return map;
    }
}
