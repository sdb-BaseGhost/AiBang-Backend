package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.dto.response.*;
import org.sdb.aiban.entity.LearningRecord;
import org.sdb.aiban.entity.SkillChapter;
import org.sdb.aiban.entity.UserChapterProgress;
import org.sdb.aiban.entity.UserSkillProgress;
import org.sdb.aiban.mapper.LearningRecordMapper;
import org.sdb.aiban.mapper.SkillChapterMapper;
import org.sdb.aiban.mapper.UserChapterProgressMapper;
import org.sdb.aiban.mapper.UserSkillProgressMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningRecordMapper learningRecordMapper;
    private final UserSkillProgressMapper userSkillProgressMapper;
    private final SkillChapterMapper skillChapterMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final SkillService skillService;

    /**
     * 创建学习记录
     */
    public void createRecord(Long userId, String type, String title, int duration, String detail, Long skillId) {
        LearningRecord record = new LearningRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setTitle(title);
        record.setDuration(duration);
        record.setDetail(detail != null ? detail : "");
        record.setSkillId(skillId);
        learningRecordMapper.insert(record);
        log.info("创建学习记录: userId={}, type={}, title={}, duration={}min", userId, type, title, duration);
    }

    /**
     * 获取仪表盘数据
     */
    public LearningDashboardVO getDashboard(Long userId) {
        // 总学习时长
        List<LearningRecord> allRecords = learningRecordMapper.selectList(
            new LambdaQueryWrapper<LearningRecord>().eq(LearningRecord::getUserId, userId));
        double totalHours = allRecords.stream().mapToInt(LearningRecord::getDuration).sum() / 60.0;
        totalHours = Math.round(totalHours * 10.0) / 10.0;

        // 今日学习时长
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int todayMinutes = allRecords.stream()
            .filter(r -> r.getCreateTime().isAfter(todayStart))
            .mapToInt(LearningRecord::getDuration).sum();

        // 技能进度统计
        List<UserSkillProgress> progressList = userSkillProgressMapper.selectList(
            new LambdaQueryWrapper<UserSkillProgress>().eq(UserSkillProgress::getUserId, userId));
        Set<Long> completedSkills = progressList.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .map(UserSkillProgress::getSkillId).collect(Collectors.toSet());
        int totalRootSkills = 11; // 硬编码的顶级技能数
        double skillProgress = totalRootSkills > 0 ? Math.round(completedSkills.size() * 100.0 / totalRootSkills * 10.0) / 10.0 : 0;

        // 最近7天每日学习时长
        List<LearningDashboardVO.DailyStudyHours> weeklyHours = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            int minutes = allRecords.stream()
                .filter(r -> !r.getCreateTime().isBefore(dayStart) && !r.getCreateTime().isAfter(dayEnd))
                .mapToInt(LearningRecord::getDuration).sum();
            weeklyHours.add(LearningDashboardVO.DailyStudyHours.builder()
                .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .hours(Math.round(minutes / 60.0 * 10.0) / 10.0)
                .build());
        }

        // 各分类进度（从数据库动态计算）
        List<LearningDashboardVO.CategoryProgress> categoryProgress = skillService.getCategoryProgressList(userId);

        return LearningDashboardVO.builder()
            .summary(LearningDashboardVO.DashboardSummary.builder()
                .totalStudyHours(totalHours)
                .todayStudyMinutes(todayMinutes)
                .completedSkills(completedSkills.size())
                .totalSkills(totalRootSkills)
                .skillProgress(skillProgress)
                .build())
            .weeklyStudyHours(weeklyHours)
            .categoryProgress(categoryProgress)
            .build();
    }

    /**
     * 获取学习时间线
     */
    public PageResult<LearningTimelineVO> getTimeline(Long userId, int page, int size) {
        // 1. 查询学习记录
        List<LearningRecord> learningRecords = learningRecordMapper.selectList(
            new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .orderByDesc(LearningRecord::getCreateTime));

        List<LearningTimelineVO> learningItems = learningRecords.stream()
            .map(r -> LearningTimelineVO.builder()
                .id(r.getId())
                .type(r.getType())
                .title(r.getTitle())
                .duration(r.getDuration())
                .detail(r.getDetail())
                .createTime(r.getCreateTime())
                .build())
            .toList();

        // 2. 查询章节进度记录
        List<UserChapterProgress> chapterProgressList = userChapterProgressMapper.selectList(
            new LambdaQueryWrapper<UserChapterProgress>()
                .eq(UserChapterProgress::getUserId, userId)
                .eq(UserChapterProgress::getStatus, "COMPLETED")
                .orderByDesc(UserChapterProgress::getCreateTime));

        // 3. 查询章节标题
        List<Long> chapterIds = chapterProgressList.stream()
            .map(UserChapterProgress::getChapterId)
            .toList();

        Map<Long, String> chapterTitleMap = new HashMap<>();
        if (!chapterIds.isEmpty()) {
            List<SkillChapter> chapters = skillChapterMapper.selectList(
                new LambdaQueryWrapper<SkillChapter>()
                    .in(SkillChapter::getId, chapterIds));
            for (SkillChapter chapter : chapters) {
                chapterTitleMap.put(chapter.getId(), chapter.getTitle());
            }
        }

        List<LearningTimelineVO> chapterItems = chapterProgressList.stream()
            .map(cp -> {
                String chapterTitle = chapterTitleMap.getOrDefault(cp.getChapterId(), "未知章节");
                return LearningTimelineVO.builder()
                    .id(cp.getId())
                    .type("CHAPTER_STUDY")
                    .title(chapterTitle)
                    .duration(cp.getScore())  // 分数放在duration字段
                    .detail("")
                    .createTime(cp.getCreateTime())
                    .build();
            })
            .toList();

        // 4. 合并所有记录
        List<LearningTimelineVO> allItems = new ArrayList<>();
        allItems.addAll(learningItems);
        allItems.addAll(chapterItems);

        // 5. 按时间排序（最新的在前）
        allItems.sort((a, b) -> {
            if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 6. 手动分页
        long total = allItems.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allItems.size());

        List<LearningTimelineVO> pageItems;
        if (fromIndex >= allItems.size()) {
            pageItems = new ArrayList<>();
        } else {
            pageItems = allItems.subList(fromIndex, toIndex);
        }

        Page<LearningTimelineVO> responsePage = new Page<>(page, size, total);
        responsePage.setPages(totalPages);
        responsePage.setRecords(pageItems);

        return PageResult.from(responsePage);
    }

    /**
     * 获取周报
     */
    public WeeklyReportVO getWeeklyReport(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

        List<LearningRecord> records = learningRecordMapper.selectList(
            new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .ge(LearningRecord::getCreateTime, start)
                .le(LearningRecord::getCreateTime, end));

        double totalHours = records.stream().mapToInt(LearningRecord::getDuration).sum() / 60.0;
        totalHours = Math.round(totalHours * 10.0) / 10.0;

        Set<LocalDate> studyDays = records.stream()
            .map(r -> r.getCreateTime().toLocalDate())
            .collect(Collectors.toSet());

        double avgDaily = studyDays.isEmpty() ? 0 : Math.round(totalHours / studyDays.size() * 10.0) / 10.0;

        // 统计技能进度变化
        List<UserSkillProgress> progressList = userSkillProgressMapper.selectList(
            new LambdaQueryWrapper<UserSkillProgress>().eq(UserSkillProgress::getUserId, userId));
        int completed = (int) progressList.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        int inProgress = (int) progressList.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();

        // 找出最常学习的技能
        String topSkill = records.stream()
            .filter(r -> r.getSkillId() != null)
            .collect(Collectors.groupingBy(LearningRecord::getSkillId, Collectors.summingInt(LearningRecord::getDuration)))
            .entrySet().stream().max(Map.Entry.comparingByValue())
            .map(e -> skillService.getSkillName(e.getKey()))
            .orElse("无");

        List<String> recommendations = List.of(
            "保持每天学习的习惯",
            "可以尝试做一个小项目来巩固已学技能"
        );

        return WeeklyReportVO.builder()
            .period(startDate + " ~ " + endDate)
            .totalStudyHours(totalHours)
            .studyDays(studyDays.size())
            .avgDailyHours(avgDaily)
            .skillsCompleted(completed)
            .skillsInProgress(inProgress)
            .topSkill(topSkill)
            .recommendations(recommendations)
            .build();
    }

    /**
     * 获取月报
     */
    public MonthlyReportVO getMonthlyReport(Long userId, int year, int month) {
        LocalDate startLocalDate = LocalDate.of(year, month, 1);
        LocalDate endLocalDate = startLocalDate.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime start = LocalDateTime.of(startLocalDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endLocalDate, LocalTime.MAX);

        List<LearningRecord> records = learningRecordMapper.selectList(
            new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .ge(LearningRecord::getCreateTime, start)
                .le(LearningRecord::getCreateTime, end));

        double totalHours = records.stream().mapToInt(LearningRecord::getDuration).sum() / 60.0;
        totalHours = Math.round(totalHours * 10.0) / 10.0;

        Set<LocalDate> studyDays = records.stream()
            .map(r -> r.getCreateTime().toLocalDate())
            .collect(Collectors.toSet());

        double avgDaily = studyDays.isEmpty() ? 0 : Math.round(totalHours / studyDays.size() * 10.0) / 10.0;

        List<UserSkillProgress> progressList = userSkillProgressMapper.selectList(
            new LambdaQueryWrapper<UserSkillProgress>().eq(UserSkillProgress::getUserId, userId));
        int completed = (int) progressList.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        int inProgress = (int) progressList.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();

        // 各分类学习时长
        List<MonthlyReportVO.CategoryHours> categoryBreakdown = List.of(
            MonthlyReportVO.CategoryHours.builder().categoryName("编程语言").hours(52.0).build(),
            MonthlyReportVO.CategoryHours.builder().categoryName("前端开发").hours(28.5).build(),
            MonthlyReportVO.CategoryHours.builder().categoryName("数据库").hours(35.0).build(),
            MonthlyReportVO.CategoryHours.builder().categoryName("AI/机器学习").hours(13.0).build()
        );

        String summary = totalHours > 0
            ? String.format("本月学习状态良好，共学习%.1f小时，完成了%d项技能。", totalHours, completed)
            : "本月还没有学习记录，开始记录你的学习吧！";

        return MonthlyReportVO.builder()
            .period(year + "年" + month + "月")
            .totalStudyHours(totalHours)
            .studyDays(studyDays.size())
            .avgDailyHours(avgDaily)
            .skillsCompleted(completed)
            .skillsInProgress(inProgress)
            .categoryBreakdown(categoryBreakdown)
            .summary(summary)
            .build();
    }
}
