package org.sdb.aiban.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.response.CheckinRecordsVO;
import org.sdb.aiban.dto.response.CheckinResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinRedisService {

    private final StringRedisTemplate redisTemplate;
    private final DailyStudyDurationService dailyStudyDurationService;

    private static final String BITMAP_KEY_PREFIX = "checkin:bitmap:";
    private static final String STREAK_KEY_PREFIX = "checkin:streak:";

    /**
     * 每日打卡
     */
    public CheckinResponse checkin(Long userId) {
        LocalDate today = LocalDate.now();
        String bitmapKey = getBitmapKey(userId, today.getYear(), today.getMonthValue());
        int dayOffset = today.getDayOfMonth() - 1;

        log.info("开始打卡: userId={}, bitmapKey={}, dayOffset={}", userId, bitmapKey, dayOffset);

        // 检查是否已打卡
        Boolean alreadyCheckedIn = redisTemplate.opsForValue().getBit(bitmapKey, dayOffset);
        log.info("检查打卡状态: alreadyCheckedIn={}", alreadyCheckedIn);
        if (Boolean.TRUE.equals(alreadyCheckedIn)) {
            log.warn("用户已打卡，拒绝重复打卡: userId={}", userId);
            throw new BusinessException(ResultCode.BAD_REQUEST, "今日已打卡");
        }

        // 设置打卡（Bitmap）
        redisTemplate.opsForValue().setBit(bitmapKey, dayOffset, true);
        log.info("设置打卡记录成功");

        // 设置过期时间（下个月1号）
        java.time.Duration expireTime = getExpireTime(today);
        redisTemplate.expire(bitmapKey, expireTime);
        log.info("设置过期时间: expireTime={}秒", expireTime.getSeconds());

        // 计算连续打卡天数
        int streak = calculateStreak(userId);

        log.info("用户打卡成功: userId={}, streak={}", userId, streak);

        return CheckinResponse.builder()
                .date(today)
                .streak(streak)
                .build();
    }

    /**
     * 打卡 + 保存每日学习时长
     */
    public CheckinResponse checkinWithDailyDuration(Long userId, int durationMinutes) {
        LocalDate today = LocalDate.now();
        String bitmapKey = getBitmapKey(userId, today.getYear(), today.getMonthValue());
        int dayOffset = today.getDayOfMonth() - 1;

        // 检查是否已打卡
        Boolean alreadyCheckedIn = redisTemplate.opsForValue().getBit(bitmapKey, dayOffset);
        if (Boolean.TRUE.equals(alreadyCheckedIn)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "今日已打卡");
        }

        // 设置打卡（Bitmap）
        redisTemplate.opsForValue().setBit(bitmapKey, dayOffset, true);

        // 设置过期时间
        java.time.Duration expireTime = getExpireTime(today);
        redisTemplate.expire(bitmapKey, expireTime);

        // 保存每日学习时长
        dailyStudyDurationService.save(userId, today, durationMinutes);

        // 计算连续打卡天数
        int streak = calculateStreak(userId);

        log.info("用户打卡+学习时长: userId={}, streak={}, duration={}min", userId, streak, durationMinutes);

        return CheckinResponse.builder()
                .date(today)
                .streak(streak)
                .durationMinutes(durationMinutes)
                .build();
    }

    /**
     * 查询今日是否打卡
     */
    public boolean hasCheckedInToday(Long userId) {
        LocalDate today = LocalDate.now();
        String bitmapKey = getBitmapKey(userId, today.getYear(), today.getMonthValue());
        int dayOffset = today.getDayOfMonth() - 1;

        Boolean isCheckedIn = redisTemplate.opsForValue().getBit(bitmapKey, dayOffset);
        log.info("查询打卡状态: userId={}, bitmapKey={}, dayOffset={}, result={}", userId, bitmapKey, dayOffset, isCheckedIn);
        return Boolean.TRUE.equals(isCheckedIn);
    }

    /**
     * 获取月度打卡记录
     */
    public CheckinRecordsVO getRecords(Long userId, int year, int month) {
        String bitmapKey = getBitmapKey(userId, year, month);
        List<LocalDate> dates = new ArrayList<>();

        // 获取当月天数
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        // 遍历当月每一天
        for (int day = 1; day <= daysInMonth; day++) {
            Boolean isCheckedIn = redisTemplate.opsForValue().getBit(bitmapKey, day - 1);
            if (Boolean.TRUE.equals(isCheckedIn)) {
                dates.add(LocalDate.of(year, month, day));
            }
        }

        // 统计信息
        int totalCheckins = dates.size();
        int currentStreak = calculateStreak(userId);
        int longestStreak = calculateLongestStreak(userId, year, month);

        // 查询每日学习时长
        java.util.Map<String, Integer> dailyDurations = dailyStudyDurationService.getMonthlyDurations(userId, year, month);

        return CheckinRecordsVO.builder()
                .records(dates)
                .dailyDurations(dailyDurations)
                .totalCheckins(totalCheckins)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .build();
    }

    /**
     * 计算连续打卡天数（从今天往前）
     */
    private int calculateStreak(Long userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            String bitmapKey = getBitmapKey(userId, date.getYear(), date.getMonthValue());
            int offset = date.getDayOfMonth() - 1;

            Boolean isCheckedIn = redisTemplate.opsForValue().getBit(bitmapKey, offset);
            if (Boolean.TRUE.equals(isCheckedIn)) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 计算当月最长连续打卡天数
     */
    private int calculateLongestStreak(Long userId, int year, int month) {
        String bitmapKey = getBitmapKey(userId, year, month);
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        int longestStreak = 0;
        int currentStreak = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            Boolean isCheckedIn = redisTemplate.opsForValue().getBit(bitmapKey, day - 1);
            if (Boolean.TRUE.equals(isCheckedIn)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return longestStreak;
    }

    /**
     * 生成 Bitmap Key
     */
    private String getBitmapKey(Long userId, int year, int month) {
        return BITMAP_KEY_PREFIX + userId + ":" + String.format("%d%02d", year, month);
    }

    /**
     * 计算过期时间（下个月1号）
     */
    private java.time.Duration getExpireTime(LocalDate date) {
        LocalDate nextMonthFirstDay = date.withDayOfMonth(1).plusMonths(1);
        return java.time.Duration.between(
                java.time.Instant.now(),
                nextMonthFirstDay.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        );
    }
}
