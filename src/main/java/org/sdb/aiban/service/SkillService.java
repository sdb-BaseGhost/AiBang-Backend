package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.UpdateSkillProgressRequest;
import org.sdb.aiban.dto.response.*;
import org.sdb.aiban.entity.Skill;
import org.sdb.aiban.entity.SkillCategory;
import org.sdb.aiban.entity.UserSkillProgress;
import org.sdb.aiban.mapper.SkillCategoryMapper;
import org.sdb.aiban.mapper.SkillMapper;
import org.sdb.aiban.mapper.UserSkillProgressMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SkillService {

    private final UserSkillProgressMapper userSkillProgressMapper;
    private final SkillMapper skillMapper;
    private final SkillCategoryMapper skillCategoryMapper;

    @Lazy
    @Autowired
    private LearningService learningService;

    public SkillService(UserSkillProgressMapper userSkillProgressMapper,
                        SkillMapper skillMapper,
                        SkillCategoryMapper skillCategoryMapper) {
        this.userSkillProgressMapper = userSkillProgressMapper;
        this.skillMapper = skillMapper;
        this.skillCategoryMapper = skillCategoryMapper;
    }

    private static final Map<String, String> LEVEL_LABELS = Map.of(
        "BEGINNER", "入门", "INTERMEDIATE", "进阶", "ADVANCED", "高级", "EXPERT", "专家"
    );

    public List<SkillCategoryVO> getCategories() {
        List<SkillCategory> categories = skillCategoryMapper.selectList(
            new LambdaQueryWrapper<SkillCategory>()
                .orderByAsc(SkillCategory::getSortOrder)
                .orderByAsc(SkillCategory::getId));
        return categories.stream().map(cat -> {
            Long skillCount = skillMapper.selectCount(
                new LambdaQueryWrapper<Skill>()
                    .eq(Skill::getCategoryId, cat.getId())
                    .isNull(Skill::getParentSkillId));
            return SkillCategoryVO.builder()
                .categoryId(cat.getId())
                .name(cat.getName())
                .icon(cat.getIcon())
                .sortOrder(cat.getSortOrder())
                .skillCount(skillCount.intValue())
                .build();
        }).collect(Collectors.toList());
    }

    // ==================== Skill Tree ====================

    public List<SkillTreeVO> getSkillTree(Long categoryId) {
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Skill::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(Skill::getSortOrder).orderByAsc(Skill::getId);
        List<Skill> allSkills = skillMapper.selectList(wrapper);

        Map<Long, String> categoryNameMap = new HashMap<>();
        List<SkillCategory> cats = skillCategoryMapper.selectList(null);
        for (SkillCategory cat : cats) {
            categoryNameMap.put(cat.getId(), cat.getName());
        }

        List<Skill> rootSkills = allSkills.stream()
            .filter(s -> s.getParentSkillId() == null || s.getParentSkillId() == 0)
            .collect(Collectors.toList());

        return rootSkills.stream()
            .map(root -> buildTree(root, allSkills, categoryNameMap))
            .collect(Collectors.toList());
    }

    private SkillTreeVO buildTree(Skill skill, List<Skill> allSkills, Map<Long, String> categoryNameMap) {
        List<SkillTreeVO> children = allSkills.stream()
            .filter(s -> skill.getId().equals(s.getParentSkillId()))
            .map(child -> buildTree(child, allSkills, categoryNameMap))
            .collect(Collectors.toList());

        String level = skill.getLevel() != null ? skill.getLevel() : "BEGINNER";
        Long catId = skill.getCategoryId();
        String catName = categoryNameMap.getOrDefault(catId, "");

        return SkillTreeVO.builder()
            .skillId(skill.getId())
            .name(skill.getName())
            .categoryId(catId)
            .categoryName(catName)
            .level(level)
            .levelLabel(LEVEL_LABELS.getOrDefault(level, level))
            .children(children)
            .build();
    }

    // ==================== User Progress ====================

    public SkillProgressSummaryVO getMyProgress(Long userId) {
        List<Skill> rootSkills = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>()
                .isNull(Skill::getParentSkillId)
                .orderByAsc(Skill::getSortOrder));
        int totalSkills = rootSkills.size();

        List<UserSkillProgress> progressList = userSkillProgressMapper.selectList(
            new LambdaQueryWrapper<UserSkillProgress>().eq(UserSkillProgress::getUserId, userId));
        Map<Long, UserSkillProgress> progressMap = progressList.stream()
            .collect(Collectors.toMap(UserSkillProgress::getSkillId, p -> p));

        Map<Long, String> categoryNameMap = new HashMap<>();
        List<SkillCategory> cats = skillCategoryMapper.selectList(null);
        for (SkillCategory cat : cats) {
            categoryNameMap.put(cat.getId(), cat.getName());
        }

        int completedCount = 0, inProgressCount = 0, notStartedCount = 0;
        List<SkillProgressVO> list = new ArrayList<>();

        for (Skill skill : rootSkills) {
            UserSkillProgress progress = progressMap.get(skill.getId());
            if (progress != null) {
                switch (progress.getStatus()) {
                    case "COMPLETED" -> completedCount++;
                    case "IN_PROGRESS" -> inProgressCount++;
                    default -> notStartedCount++;
                }
                list.add(SkillProgressVO.builder()
                    .skillId(skill.getId())
                    .skillName(skill.getName())
                    .categoryName(categoryNameMap.getOrDefault(skill.getCategoryId(), ""))
                    .status(progress.getStatus())
                    .progress(progress.getProgress())
                    .rating(progress.getRating())
                    .updateTime(progress.getUpdateTime())
                    .build());
            } else {
                notStartedCount++;
            }
        }

        double overallProgress = totalSkills > 0
            ? Math.round(completedCount * 100.0 / totalSkills * 10.0) / 10.0 : 0;

        return SkillProgressSummaryVO.builder()
            .totalSkills(totalSkills)
            .completedCount(completedCount)
            .inProgressCount(inProgressCount)
            .notStartedCount(notStartedCount)
            .overallProgress(overallProgress)
            .skillProgressList(list)
            .build();
    }

    public UpdateSkillProgressResponse updateProgress(Long userId, UpdateSkillProgressRequest request) {
        Long skillId = request.getSkillId();
        Integer progress = request.getProgress();

        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能不存在");
        }

        String status;
        if (progress >= 100) { status = "COMPLETED"; progress = 100; }
        else if (progress > 0) { status = "IN_PROGRESS"; }
        else { status = "NOT_STARTED"; }

        UserSkillProgress existing = userSkillProgressMapper.selectOne(
            new LambdaQueryWrapper<UserSkillProgress>()
                .eq(UserSkillProgress::getUserId, userId)
                .eq(UserSkillProgress::getSkillId, skillId));

        if (existing != null) {
            existing.setProgress(progress);
            existing.setStatus(status);
            if (request.getRating() != null) existing.setRating(request.getRating());
            if (request.getNote() != null) existing.setNote(request.getNote());
            userSkillProgressMapper.updateById(existing);
        } else {
            UserSkillProgress np = new UserSkillProgress();
            np.setUserId(userId);
            np.setSkillId(skillId);
            np.setProgress(progress);
            np.setStatus(status);
            np.setRating(request.getRating() != null ? request.getRating() : 0);
            np.setNote(request.getNote() != null ? request.getNote() : "");
            userSkillProgressMapper.insert(np);
        }

        double op = calculateOverallProgress(userId);

        if (request.getDuration() != null && request.getDuration() > 0) {
            learningService.createRecord(userId, "SKILL_STUDY",
                "学习了 " + skill.getName(), request.getDuration(),
                request.getNote(), skillId);
        }

        return UpdateSkillProgressResponse.builder()
            .skillId(skillId)
            .skillName(skill.getName())
            .progress(progress)
            .status(status)
            .overallProgress(op)
            .build();
    }

    private double calculateOverallProgress(Long userId) {
        Long total = skillMapper.selectCount(
            new LambdaQueryWrapper<Skill>().isNull(Skill::getParentSkillId));
        if (total == 0) return 0;
        Long completed = userSkillProgressMapper.selectCount(
            new LambdaQueryWrapper<UserSkillProgress>()
                .eq(UserSkillProgress::getUserId, userId)
                .eq(UserSkillProgress::getStatus, "COMPLETED"));
        return Math.round(completed * 100.0 / total * 10.0) / 10.0;
    }

    public String getSkillName(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        return skill != null ? skill.getName() : "未知技能";
    }

    /**
     * 获取每个技能分类的完成率进度
     * 计算公式：已完成技能数 / 总技能数 × 100%
     */
    public List<LearningDashboardVO.CategoryProgress> getCategoryProgressList(Long userId) {
        // 1. 查询所有技能分类
        List<SkillCategory> categories = skillCategoryMapper.selectList(
            new LambdaQueryWrapper<SkillCategory>()
                .orderByAsc(SkillCategory::getSortOrder)
                .orderByAsc(SkillCategory::getId));

        // 2. 查询用户已完成的技能ID集合
        List<UserSkillProgress> completedList = userSkillProgressMapper.selectList(
            new LambdaQueryWrapper<UserSkillProgress>()
                .eq(UserSkillProgress::getUserId, userId)
                .eq(UserSkillProgress::getStatus, "COMPLETED"));
        Set<Long> completedSkillIds = completedList.stream()
            .map(UserSkillProgress::getSkillId)
            .collect(Collectors.toSet());

        // 3. 对于每个分类，计算完成率
        List<LearningDashboardVO.CategoryProgress> result = new ArrayList<>();
        for (SkillCategory category : categories) {
            // 查询该分类下的所有根技能数量
            Long totalSkills = skillMapper.selectCount(
                new LambdaQueryWrapper<Skill>()
                    .eq(Skill::getCategoryId, category.getId())
                    .isNull(Skill::getParentSkillId));

            // 如果该分类下没有技能，跳过
            if (totalSkills == 0) {
                continue;
            }

            // 查询该分类下所有根技能的ID列表
            List<Skill> categorySkills = skillMapper.selectList(
                new LambdaQueryWrapper<Skill>()
                    .eq(Skill::getCategoryId, category.getId())
                    .isNull(Skill::getParentSkillId)
                    .select(Skill::getId));
            Set<Long> categorySkillIds = categorySkills.stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

            // 计算该分类下已完成的技能数量
            long completedCount = categorySkillIds.stream()
                .filter(completedSkillIds::contains)
                .count();

            // 计算完成率
            double progress = Math.round(completedCount * 100.0 / totalSkills * 10.0) / 10.0;

            result.add(LearningDashboardVO.CategoryProgress.builder()
                .categoryName(category.getName())
                .progress(progress)
                .build());
        }

        return result;
    }
}
