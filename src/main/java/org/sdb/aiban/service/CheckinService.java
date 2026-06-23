package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.CheckinRequest;
import org.sdb.aiban.dto.response.CheckinRecordsVO;
import org.sdb.aiban.dto.response.CheckinResponse;
import org.sdb.aiban.entity.UserCheckin;
import org.sdb.aiban.mapper.UserCheckinMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final UserCheckinMapper userCheckinMapper;

    /**
     * 今日打卡
     */
    public CheckinResponse checkin(Long userId, CheckinRequest request) {
        LocalDate today = LocalDate.now();

        // 检查今天是否已打卡
        UserCheckin existing = userCheckinMapper.selectOne(
            new LambdaQueryWrapper<UserCheckin>()
                .eq(UserCheckin::getUserId, userId)
                .eq(UserCheckin::getCheckinDate, today));

        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "今天已经打卡过了");
        }

        // 计算连续打卡天数
        int streak = calculateStreak(userId, today);

        // 创建打卡记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setDuration(request.getDuration());
        checkin.setNote(request.getNote() != null ? request.getNote() : "");
        checkin.setStreak(streak);
        userCheckinMapper.insert(checkin);

        // 统计总打卡次数
        long totalCheckins = userCheckinMapper.selectCount(
            new LambdaQueryWrapper<UserCheckin>().eq(UserCheckin::getUserId, userId));

        log.info("用户打卡成功: userId={}, duration={}min, streak={}", userId, request.getDuration(), streak);

        return CheckinResponse.builder()
            .checkinId(checkin.getId())
            .date(today)
            .duration(request.getDuration())
            .streak(streak)
            .totalCheckins((int) totalCheckins)
            .build();
    }

    /**
     * 计算连续打卡天数
     */
    private int calculateStreak(Long userId, LocalDate today) {
        // 查找昨天是否有打卡记录
        LocalDate yesterday = today.minusDays(1);
        UserCheckin yesterdayCheckin = userCheckinMapper.selectOne(
            new LambdaQueryWrapper<UserCheckin>()
                .eq(UserCheckin::getUserId, userId)
                .eq(UserCheckin::getCheckinDate, yesterday));

        if (yesterdayCheckin != null) {
            // 昨天打卡了，连续天数+1
            return yesterdayCheckin.getStreak() + 1;
        } else {
            // 昨天没打卡，重新开始
            return 1;
        }
    }

    /**
     * 获取打卡记录
     */
    public CheckinRecordsVO getRecords(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<UserCheckin> records = userCheckinMapper.selectList(
            new LambdaQueryWrapper<UserCheckin>()
                .eq(UserCheckin::getUserId, userId)
                .ge(UserCheckin::getCheckinDate, startDate)
                .le(UserCheckin::getCheckinDate, endDate)
                .orderByAsc(UserCheckin::getCheckinDate));

        // 转换为VO
        List<CheckinRecordsVO.CheckinDay> checkinDays = records.stream()
            .map(r -> CheckinRecordsVO.CheckinDay.builder()
                .date(r.getCheckinDate())
                .duration(r.getDuration())
                .streak(r.getStreak())
                .build())
            .collect(Collectors.toList());

        // 统计
        int totalCheckins = records.size();
        int monthTotalMinutes = records.stream().mapToInt(UserCheckin::getDuration).sum();
        int currentStreak = calculateCurrentStreak(userId);
        int longestStreak = records.stream().mapToInt(UserCheckin::getStreak).max().orElse(0);

        return CheckinRecordsVO.builder()
            .records(checkinDays)
            .totalCheckins(totalCheckins)
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .monthTotalMinutes(monthTotalMinutes)
            .build();
    }

    /**
     * 计算当前连续打卡天数
     */
    private int calculateCurrentStreak(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            UserCheckin checkin = userCheckinMapper.selectOne(
                new LambdaQueryWrapper<UserCheckin>()
                    .eq(UserCheckin::getUserId, userId)
                    .eq(UserCheckin::getCheckinDate, checkDate));

            if (checkin != null) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }
}
