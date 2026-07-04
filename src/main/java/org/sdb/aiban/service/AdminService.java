package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.ChangeRoleRequest;
import org.sdb.aiban.dto.request.ChangeStatusRequest;
import org.sdb.aiban.dto.request.UserQueryRequest;
import org.sdb.aiban.dto.response.AdminDashboardVO;
import org.sdb.aiban.dto.response.LearningRecordVO;
import org.sdb.aiban.dto.response.UserImportVO;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.entity.DailyStudyDuration;
import org.sdb.aiban.entity.LearningRecord;
import org.sdb.aiban.entity.Skill;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.entity.UserSkillProgress;
import org.sdb.aiban.mapper.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final DailyStudyDurationMapper dailyStudyDurationMapper;
    private final UserSkillProgressMapper userSkillProgressMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiResumeMapper aiResumeMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserResponse> getUserList(UserQueryRequest request) {
        Page<SysUser> page = new Page<>(request.getPage(), request.getSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 模糊搜索用户名/昵称/邮箱
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword();
            wrapper.and(w -> w
                .like(SysUser::getUsername, kw)
                .or().like(SysUser::getNickname, kw)
                .or().like(SysUser::getEmail, kw));
        }

        // 筛选角色
        if (StringUtils.hasText(request.getRole())) {
            wrapper.eq(SysUser::getRole, request.getRole());
        }

        // 筛选状态
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(SysUser::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = userMapper.selectPage(page, wrapper);

        // 批量查询当前页用户的学习总时长（分钟→小时）
        List<Long> userIds = result.getRecords().stream()
            .map(SysUser::getId).collect(Collectors.toList());
        Map<Long, Double> studyHoursMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<DailyStudyDuration> durations = dailyStudyDurationMapper.selectList(
                new LambdaQueryWrapper<DailyStudyDuration>()
                    .in(DailyStudyDuration::getUserId, userIds));
            for (DailyStudyDuration d : durations) {
                studyHoursMap.merge(d.getUserId(),
                    d.getDurationMinutes() / 60.0, Double::sum);
            }
        }

        // 转换为 UserResponse
        Page<UserResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(user -> buildUserResponse(user, studyHoursMap))
                .toList());

        return PageResult.from(responsePage);
    }

    /**
     * 获取用户详情
     */
    public UserResponse getUserDetail(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return buildUserResponse(user);
    }

    /**
     * 修改用户角色
     */
    public void changeUserRole(Long userId, ChangeRoleRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String oldRole = user.getRole();
        user.setRole(request.getRole());
        userMapper.updateById(user);

        log.info("管理员修改用户角色: {} -> {} -> {}", user.getUsername(), oldRole, request.getRole());
    }

    /**
     * 启用/禁用用户
     */
    public void changeUserStatus(Long userId, ChangeStatusRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        userMapper.updateById(user);

        log.info("管理员修改用户状态: {} -> {} -> {}", user.getUsername(), oldStatus, request.getStatus());
    }

    /**
     * 重置用户密码
     */
    public String resetPassword(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String tempPassword = "Abc123456";
        user.setPassword(passwordEncoder.encode(tempPassword));
        userMapper.updateById(user);

        log.info("管理员重置用户密码: {}", user.getUsername());
        return tempPassword;
    }

    /**
     * 获取仪表盘统计数据
     * @param userId 可选，指定时只返回该用户的学习趋势
     */
    public AdminDashboardVO getDashboard(Long userId) {
        // 用户统计
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayNewUsers = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().ge(SysUser::getCreateTime, todayStart));
        long activeUsers = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, "ACTIVE"));

        // 学习统计
        List<LearningRecord> allRecords = learningRecordMapper.selectList(new LambdaQueryWrapper<>());
        double totalHours = allRecords.stream().mapToInt(LearningRecord::getDuration).sum() / 60.0;
        totalHours = Math.round(totalHours * 10.0) / 10.0;
        int todayMinutes = allRecords.stream()
            .filter(r -> r.getCreateTime().isAfter(todayStart))
            .mapToInt(LearningRecord::getDuration).sum();
        // 打卡统计已改为 Redis，这里暂时设为 0
        long totalCheckins = 0;

        // 技能统计
        List<Skill> rootSkills = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>().isNull(Skill::getParentSkillId));
        int totalSkills = rootSkills.size();

        List<UserSkillProgress> allProgress = userSkillProgressMapper.selectList(new LambdaQueryWrapper<>());

        // 平均完成率
        double avgCompletion = 0;
        if (!allProgress.isEmpty() && totalSkills > 0 && totalUsers > 0) {
            long completed = allProgress.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
            avgCompletion = Math.round(completed * 100.0 / (totalSkills * totalUsers) * 10.0) / 10.0;
        }

        // 技能完成率排行（按「该技能已完成用户数 / 总用户数」从高到低）
        Map<Long, Long> completedCountBySkill = allProgress.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .collect(Collectors.groupingBy(UserSkillProgress::getSkillId, Collectors.counting()));

        List<AdminDashboardVO.TopSkill> topSkills = rootSkills.stream()
            .map(skill -> {
                long done = completedCountBySkill.getOrDefault(skill.getId(), 0L);
                double rate = totalUsers > 0 ? Math.round(done * 1000.0 / totalUsers) / 10.0 : 0;
                return new AdminDashboardVO.TopSkill(skill.getName(), rate);
            })
            .sorted(Comparator.comparingDouble(AdminDashboardVO.TopSkill::getCompletionRate).reversed())
            .limit(10)
            .collect(Collectors.toList());

        // 每周趋势（最近7天）
        // 如果指定了 userId，从 daily_study_duration 按用户查询；否则沿用 learning_record 汇总
        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> userDurationMap = new HashMap<>();
        if (userId != null) {
            List<DailyStudyDuration> userDurations = dailyStudyDurationMapper.selectList(
                new LambdaQueryWrapper<DailyStudyDuration>()
                    .eq(DailyStudyDuration::getUserId, userId)
                    .ge(DailyStudyDuration::getStudyDate, today.minusDays(6))
                    .le(DailyStudyDuration::getStudyDate, today));
            for (DailyStudyDuration d : userDurations) {
                userDurationMap.put(d.getStudyDate(), d.getDurationMinutes());
            }
        }

        List<AdminDashboardVO.DailyTrend> weeklyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            int dayMinutes;
            long dayUsers = 0;
            if (userId != null) {
                dayMinutes = userDurationMap.getOrDefault(date, 0);
            } else {
                dayUsers = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().ge(SysUser::getCreateTime, dayStart).le(SysUser::getCreateTime, dayEnd));
                dayMinutes = allRecords.stream()
                    .filter(r -> !r.getCreateTime().isBefore(dayStart) && !r.getCreateTime().isAfter(dayEnd))
                    .mapToInt(LearningRecord::getDuration).sum();
            }

            // 统计当日 AI Token 消耗（聊天 + 简历）
            Long chatTokens = aiChatMessageMapper.selectDailyTokens(dayStart, dayEnd);
            Long resumeTokens = aiResumeMapper.selectDailyTokens(dayStart, dayEnd);
            long dayTokens = (chatTokens != null ? chatTokens : 0) + (resumeTokens != null ? resumeTokens : 0);

            weeklyTrend.add(AdminDashboardVO.DailyTrend.builder()
                .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .users(dayUsers)
                .hours((long) dayMinutes)
                .tokens(dayTokens)
                .build());
        }

        return AdminDashboardVO.builder()
            .userStats(AdminDashboardVO.UserStats.builder()
                .totalUsers(totalUsers).todayNewUsers(todayNewUsers).activeUsers(activeUsers).build())
            .learningStats(AdminDashboardVO.LearningStats.builder()
                .totalStudyHours(totalHours).todayStudyMinutes(todayMinutes).totalCheckins(totalCheckins).build())
            .skillStats(AdminDashboardVO.SkillStats.builder()
                .totalSkills(totalSkills).avgCompletionRate(avgCompletion).topSkills(topSkills).build())
            .weeklyTrend(weeklyTrend)
            .build();
    }

    private long getTotalUsers() {
        return userMapper.selectCount(new LambdaQueryWrapper<>());
    }

    /**
     * 分页查询已完成的学习记录
     */
    public PageResult<LearningRecordVO> getLearningRecords(Long userId, Integer page, Integer size) {
        Page<LearningRecordVO> pageParam = new Page<>(page, size);
        IPage<LearningRecordVO> result = userChapterProgressMapper.selectCompletedRecords(pageParam, userId);
        return PageResult.from(result);
    }

    /**
     * 批量导入用户
     */
    public UserImportVO importUsers(MultipartFile file) {
        List<UserImportVO.ImportFailure> failures = new ArrayList<>();
        int total = 0, success = 0, failed = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                total++;
                String username = getCellValue(row, 0);
                String password = getCellValue(row, 1);
                String nickname = getCellValue(row, 2);

                // 校验
                if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                    failed++;
                    failures.add(UserImportVO.ImportFailure.builder()
                        .row(i + 1).username(username).reason("用户名或密码为空").build());
                    continue;
                }

                // 检查用户名是否已存在
                Long exists = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
                if (exists > 0) {
                    failed++;
                    failures.add(UserImportVO.ImportFailure.builder()
                        .row(i + 1).username(username).reason("用户名已存在").build());
                    continue;
                }

                // 创建用户
                SysUser user = new SysUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
                user.setRole("STUDENT");
                user.setStatus("ACTIVE");
                userMapper.insert(user);
                success++;
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件读取失败");
        }

        log.info("批量导入用户: total={}, success={}, failed={}", total, success, failed);

        return UserImportVO.builder()
            .total(total).success(success).failed(failed).failures(failures).build();
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    /**
     * 导出用户数据
     */
    public Workbook exportUsers(String role, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) wrapper.eq(SysUser::getRole, role);
        if (StringUtils.hasText(status)) wrapper.eq(SysUser::getStatus, status);
        wrapper.orderByDesc(SysUser::getCreateTime);

        List<SysUser> users = userMapper.selectList(wrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("用户数据");

        // 表头
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "用户名", "昵称", "邮箱", "手机号", "角色", "状态", "创建时间"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        // 数据
        for (int i = 0; i < users.size(); i++) {
            SysUser user = users.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getNickname() != null ? user.getNickname() : "");
            row.createCell(3).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(5).setCellValue(user.getRole());
            row.createCell(6).setCellValue(user.getStatus());
            row.createCell(7).setCellValue(user.getCreateTime() != null ? user.getCreateTime().toString() : "");
        }

        return workbook;
    }

    /**
     * 构建用户响应（getUserDetail 单用户场景，无学习时长）
     */
    private UserResponse buildUserResponse(SysUser user) {
        return buildUserResponse(user, new HashMap<>());
    }

    /**
     * 构建用户响应（包含完整信息，管理员可见）
     */
    private UserResponse buildUserResponse(SysUser user, Map<Long, Double> studyHoursMap) {
        double hours = Math.round(studyHoursMap.getOrDefault(user.getId(), 0.0) * 10.0) / 10.0;
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone()) // 管理员看到完整手机号
                .role(user.getRole())
                .bio(user.getBio())
                .totalStudyHours(hours)
                .createTime(user.getCreateTime())
                .build();
    }
}
