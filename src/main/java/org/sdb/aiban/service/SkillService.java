package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.UpdateSkillProgressRequest;
import org.sdb.aiban.dto.response.*;
import org.sdb.aiban.entity.UserSkillProgress;
import org.sdb.aiban.mapper.UserSkillProgressMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final UserSkillProgressMapper userSkillProgressMapper;

    private static final List<Map<String, Object>> CATEGORIES = List.of(
        Map.of("id", 1L, "name", "编程语言", "icon", "code", "sortOrder", 1),
        Map.of("id", 2L, "name", "前端开发", "icon", "web", "sortOrder", 2),
        Map.of("id", 3L, "name", "后端开发", "icon", "server", "sortOrder", 3),
        Map.of("id", 4L, "name", "数据库", "icon", "database", "sortOrder", 4),
        Map.of("id", 5L, "name", "AI / 机器学习", "icon", "robot", "sortOrder", 5)
    );

    private static final List<Map<String, Object>> SKILLS = List.of(
        Map.of("id", 1L, "name", "Java", "categoryId", 1L, "level", "BEGINNER", "parentId", 0L),
        Map.of("id", 11L, "name", "Java 基础语法", "categoryId", 1L, "level", "BEGINNER", "parentId", 1L),
        Map.of("id", 12L, "name", "Java 面向对象", "categoryId", 1L, "level", "BEGINNER", "parentId", 1L),
        Map.of("id", 13L, "name", "Java 集合框架", "categoryId", 1L, "level", "INTERMEDIATE", "parentId", 1L),
        Map.of("id", 14L, "name", "Java IO/NIO", "categoryId", 1L, "level", "INTERMEDIATE", "parentId", 1L),
        Map.of("id", 15L, "name", "Java 并发编程", "categoryId", 1L, "level", "ADVANCED", "parentId", 1L),
        Map.of("id", 2L, "name", "Python", "categoryId", 1L, "level", "BEGINNER", "parentId", 0L),
        Map.of("id", 21L, "name", "Python 基础语法", "categoryId", 1L, "level", "BEGINNER", "parentId", 2L),
        Map.of("id", 22L, "name", "Python 数据结构", "categoryId", 1L, "level", "BEGINNER", "parentId", 2L),
        Map.of("id", 23L, "name", "Python 面向对象", "categoryId", 1L, "level", "INTERMEDIATE", "parentId", 2L),
        Map.of("id", 3L, "name", "HTML/CSS", "categoryId", 2L, "level", "BEGINNER", "parentId", 0L),
        Map.of("id", 31L, "name", "HTML5 语义化", "categoryId", 2L, "level", "BEGINNER", "parentId", 3L),
        Map.of("id", 32L, "name", "CSS3 布局", "categoryId", 2L, "level", "BEGINNER", "parentId", 3L),
        Map.of("id", 4L, "name", "JavaScript", "categoryId", 2L, "level", "BEGINNER", "parentId", 0L),
        Map.of("id", 41L, "name", "JS 基础语法", "categoryId", 2L, "level", "BEGINNER", "parentId", 4L),
        Map.of("id", 42L, "name", "DOM 操作", "categoryId", 2L, "level", "BEGINNER", "parentId", 4L),
        Map.of("id", 43L, "name", "ES6+ 新特性", "categoryId", 2L, "level", "INTERMEDIATE", "parentId", 4L),
        Map.of("id", 5L, "name", "Vue.js", "categoryId", 2L, "level", "INTERMEDIATE", "parentId", 0L),
        Map.of("id", 51L, "name", "Vue3 组合式API", "categoryId", 2L, "level", "INTERMEDIATE", "parentId", 5L),
        Map.of("id", 52L, "name", "Pinia 状态管理", "categoryId", 2L, "level", "INTERMEDIATE", "parentId", 5L),
        Map.of("id", 6L, "name", "Spring Boot", "categoryId", 3L, "level", "INTERMEDIATE", "parentId", 0L),
        Map.of("id", 61L, "name", "Spring Boot 基础", "categoryId", 3L, "level", "INTERMEDIATE", "parentId", 6L),
        Map.of("id", 62L, "name", "Spring Security", "categoryId", 3L, "level", "ADVANCED", "parentId", 6L),
        Map.of("id", 63L, "name", "MyBatis-Plus", "categoryId", 3L, "level", "INTERMEDIATE", "parentId", 6L),
        Map.of("id", 7L, "name", "RESTful API 设计", "categoryId", 3L, "level", "INTERMEDIATE", "parentId", 0L),
        Map.of("id", 71L, "name", "HTTP 协议基础", "categoryId", 3L, "level", "BEGINNER", "parentId", 7L),
        Map.of("id", 72L, "name", "RESTful 规范", "categoryId", 3L, "level", "INTERMEDIATE", "parentId", 7L),
        Map.of("id", 8L, "name", "MySQL", "categoryId", 4L, "level", "BEGINNER", "parentId", 0L),
        Map.of("id", 81L, "name", "SQL 基础", "categoryId", 4L, "level", "BEGINNER", "parentId", 8L),
        Map.of("id", 82L, "name", "索引优化", "categoryId", 4L, "level", "INTERMEDIATE", "parentId", 8L),
        Map.of("id", 83L, "name", "事务与锁", "categoryId", 4L, "level", "ADVANCED", "parentId", 8L),
        Map.of("id", 9L, "name", "Redis", "categoryId", 4L, "level", "INTERMEDIATE", "parentId", 0L),
        Map.of("id", 91L, "name", "Redis 数据结构", "categoryId", 4L, "level", "INTERMEDIATE", "parentId", 9L),
        Map.of("id", 92L, "name", "Redis 缓存策略", "categoryId", 4L, "level", "ADVANCED", "parentId", 9L),
        Map.of("id", 10L, "name", "机器学习基础", "categoryId", 5L, "level", "BEGINNER", "parentId", 0L),
        Map.of("id", 101L, "name", "监督学习", "categoryId", 5L, "level", "BEGINNER", "parentId", 10L),
        Map.of("id", 102L, "name", "无监督学习", "categoryId", 5L, "level", "INTERMEDIATE", "parentId", 10L),
        Map.of("id", 110L, "name", "深度学习", "categoryId", 5L, "level", "ADVANCED", "parentId", 0L)
    );

    private static final Map<String, String> LEVEL_LABELS = Map.of(
        "BEGINNER", "入门", "INTERMEDIATE", "进阶", "ADVANCED", "高级", "EXPERT", "专家"
    );

    public List<SkillCategoryVO> getCategories() {
        return CATEGORIES.stream().map(c -> {
            long categoryId = (Long) c.get("id");
            long skillCount = SKILLS.stream()
                .filter(s -> ((Long) s.get("parentId")) == 0L && ((Long) s.get("categoryId")) == categoryId)
                .count();
            return SkillCategoryVO.builder()
                .categoryId((Long) c.get("id")).name((String) c.get("name"))
                .icon((String) c.get("icon")).sortOrder((Integer) c.get("sortOrder"))
                .skillCount((int) skillCount).build();
        }).collect(Collectors.toList());
    }

    public List<SkillTreeVO> getSkillTree(Long categoryId) {
        List<Map<String, Object>> rootSkills = SKILLS.stream()
            .filter(s -> ((Long) s.get("parentId")) == 0L)
            .filter(s -> categoryId == null || ((Long) s.get("categoryId")).equals(categoryId))
            .sorted(Comparator.comparing(s -> (Long) s.get("id")))
            .collect(Collectors.toList());
        return rootSkills.stream().map(this::buildSkillTree).collect(Collectors.toList());
    }

    private SkillTreeVO buildSkillTree(Map<String, Object> skill) {
        Long skillId = (Long) skill.get("id");
        String level = (String) skill.get("level");
        Long categoryId = (Long) skill.get("categoryId");
        List<SkillTreeVO> children = SKILLS.stream()
            .filter(s -> skillId.equals(s.get("parentId")))
            .sorted(Comparator.comparing(s -> (Long) s.get("id")))
            .map(this::buildSkillTree).collect(Collectors.toList());
        return SkillTreeVO.builder().skillId(skillId).name((String) skill.get("name"))
            .categoryId(categoryId).categoryName(getCategoryName(categoryId))
            .level(level).levelLabel(LEVEL_LABELS.getOrDefault(level, level))
            .children(children).build();
    }

    private String getCategoryName(Long categoryId) {
        return CATEGORIES.stream().filter(c -> ((Long) c.get("id")).equals(categoryId))
            .map(c -> (String) c.get("name")).findFirst().orElse("");
    }

    public SkillProgressSummaryVO getMyProgress(Long userId) {
        List<UserSkillProgress> progressList = userSkillProgressMapper.selectList(
            new LambdaQueryWrapper<UserSkillProgress>().eq(UserSkillProgress::getUserId, userId));
        Map<Long, UserSkillProgress> progressMap = progressList.stream()
            .collect(Collectors.toMap(UserSkillProgress::getSkillId, p -> p));
        int totalSkills = (int) SKILLS.stream().filter(s -> ((Long) s.get("parentId")) == 0L).count();
        int completedCount = 0, inProgressCount = 0, notStartedCount = 0;
        List<SkillProgressVO> list = new ArrayList<>();
        for (Map<String, Object> skill : SKILLS) {
            if (((Long) skill.get("parentId")) != 0L) continue;
            Long skillId = (Long) skill.get("id");
            UserSkillProgress progress = progressMap.get(skillId);
            if (progress != null) {
                switch (progress.getStatus()) {
                    case "COMPLETED" -> completedCount++;
                    case "IN_PROGRESS" -> inProgressCount++;
                    default -> notStartedCount++;
                }
                list.add(SkillProgressVO.builder().skillId(skillId).skillName((String) skill.get("name"))
                    .categoryName(getCategoryName((Long) skill.get("categoryId")))
                    .status(progress.getStatus()).progress(progress.getProgress())
                    .rating(progress.getRating()).updateTime(progress.getUpdateTime()).build());
            } else { notStartedCount++; }
        }
        double op = totalSkills > 0 ? Math.round(completedCount * 100.0 / totalSkills * 10.0) / 10.0 : 0;
        return SkillProgressSummaryVO.builder().totalSkills(totalSkills).completedCount(completedCount)
            .inProgressCount(inProgressCount).notStartedCount(notStartedCount)
            .overallProgress(op).skillProgressList(list).build();
    }

    public UpdateSkillProgressResponse updateProgress(Long userId, UpdateSkillProgressRequest request) {
        Long skillId = request.getSkillId();
        Integer progress = request.getProgress();
        Map<String, Object> skill = SKILLS.stream().filter(s -> ((Long) s.get("id")).equals(skillId))
            .findFirst().orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "技能不存在"));
        String status;
        if (progress >= 100) { status = "COMPLETED"; progress = 100; }
        else if (progress > 0) { status = "IN_PROGRESS"; }
        else { status = "NOT_STARTED"; }
        UserSkillProgress existing = userSkillProgressMapper.selectOne(
            new LambdaQueryWrapper<UserSkillProgress>()
                .eq(UserSkillProgress::getUserId, userId).eq(UserSkillProgress::getSkillId, skillId));
        if (existing != null) {
            existing.setProgress(progress); existing.setStatus(status);
            existing.setRating(request.getRating() != null ? request.getRating() : existing.getRating());
            existing.setNote(request.getNote() != null ? request.getNote() : existing.getNote());
            userSkillProgressMapper.updateById(existing);
        } else {
            UserSkillProgress np = new UserSkillProgress();
            np.setUserId(userId); np.setSkillId(skillId); np.setProgress(progress); np.setStatus(status);
            np.setRating(request.getRating() != null ? request.getRating() : 0);
            np.setNote(request.getNote() != null ? request.getNote() : "");
            userSkillProgressMapper.insert(np);
        }
        double op = calculateOverallProgress(userId);
        return UpdateSkillProgressResponse.builder().skillId(skillId).skillName((String) skill.get("name"))
            .progress(progress).status(status).overallProgress(op).build();
    }

    private double calculateOverallProgress(Long userId) {
        long total = SKILLS.stream().filter(s -> ((Long) s.get("parentId")) == 0L).count();
        if (total == 0) return 0;
        long completed = userSkillProgressMapper.selectCount(
            new LambdaQueryWrapper<UserSkillProgress>()
                .eq(UserSkillProgress::getUserId, userId).eq(UserSkillProgress::getStatus, "COMPLETED"));
        return Math.round(completed * 100.0 / total * 10.0) / 10.0;
    }

    public String getSkillName(Long skillId) {
        return SKILLS.stream().filter(s -> ((Long) s.get("id")).equals(skillId))
            .map(s -> (String) s.get("name")).findFirst().orElse("未知技能");
    }
}
