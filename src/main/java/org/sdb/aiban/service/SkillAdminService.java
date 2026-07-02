package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.CreateSkillRequest;
import org.sdb.aiban.dto.request.SkillQueryRequest;
import org.sdb.aiban.dto.request.UpdateSkillRequest;
import org.sdb.aiban.dto.response.SkillListVO;
import org.sdb.aiban.dto.response.SkillStatsVO;
import org.sdb.aiban.entity.Skill;
import org.sdb.aiban.entity.SkillCategory;
import org.sdb.aiban.mapper.SkillCategoryMapper;
import org.sdb.aiban.mapper.SkillMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SkillAdminService {

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private SkillCategoryMapper skillCategoryMapper;

    private static final Map<String, String> LEVEL_LABELS = Map.of(
        "BEGINNER", "入门",
        "INTERMEDIATE", "进阶",
        "ADVANCED", "高级",
        "EXPERT", "专家"
    );

    /**
     * 获取技能列表（管理版）
     */
    public Page<SkillListVO> getSkillList(SkillQueryRequest request) {
        Page<Skill> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();

        if (request.getCategoryId() != null) {
            wrapper.eq(Skill::getCategoryId, request.getCategoryId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Skill::getName, request.getKeyword());
        }
        if (request.getLevel() != null && !request.getLevel().isEmpty()) {
            wrapper.eq(Skill::getLevel, request.getLevel());
        }

        wrapper.orderByAsc(Skill::getId);

        Page<Skill> skillPage = skillMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<SkillListVO> voPage = new Page<>(skillPage.getCurrent(), skillPage.getSize(), skillPage.getTotal());
        List<SkillListVO> voList = skillPage.getRecords().stream()
            .map(this::convertToListVO)
            .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 创建技能
     */
    @Transactional
    public SkillListVO createSkill(CreateSkillRequest request) {
        // 验证分类是否存在
        SkillCategory category = skillCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能分类不存在");
        }

        // 验证父技能是否存在
        if (request.getParentSkillId() != null) {
            Skill parentSkill = skillMapper.selectById(request.getParentSkillId());
            if (parentSkill == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "父技能不存在");
            }
        }

        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategoryId(request.getCategoryId());
        skill.setLevel(request.getLevel());
        skill.setDescription(request.getDescription());
        skill.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        skill.setParentSkillId(request.getParentSkillId());

        skillMapper.insert(skill);
        return convertToListVO(skill);
    }

    /**
     * 更新技能
     */
    @Transactional
    public SkillListVO updateSkill(Long skillId, UpdateSkillRequest request) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能不存在");
        }

        if (request.getName() != null) {
            skill.setName(request.getName());
        }
        if (request.getCategoryId() != null) {
            // 验证分类是否存在
            SkillCategory category = skillCategoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "技能分类不存在");
            }
            skill.setCategoryId(request.getCategoryId());
        }
        if (request.getLevel() != null) {
            skill.setLevel(request.getLevel());
        }
        if (request.getDescription() != null) {
            skill.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            skill.setSortOrder(request.getSortOrder());
        }
        if (request.getParentSkillId() != null) {
            // 验证父技能是否存在
            Skill parentSkill = skillMapper.selectById(request.getParentSkillId());
            if (parentSkill == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "父技能不存在");
            }
            skill.setParentSkillId(request.getParentSkillId());
        }

        skillMapper.updateById(skill);
        return convertToListVO(skill);
    }

    /**
     * 删除技能
     */
    @Transactional
    public void deleteSkill(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能不存在");
        }
        // TODO: 检查是否有子技能，如果有则不能删除
        // TODO: 检查是否有用户进度，如果有则需要处理
        skillMapper.deleteById(skillId);
    }

    /**
     * 获取技能统计
     */
    public SkillStatsVO getSkillStats() {
        // 统计分类数量
        Long totalCategories = skillCategoryMapper.selectCount(
            new LambdaQueryWrapper<SkillCategory>());

        // 统计技能数量
        Long totalSkills = skillMapper.selectCount(
            new LambdaQueryWrapper<Skill>());

        // TODO: 计算平均完成率
        Double avgCompletionRate = 0.0;

        // TODO: 获取完成率最高和最低的技能
        List<SkillStatsVO.TopSkill> topCompletedSkills = List.of();
        List<SkillStatsVO.TopSkill> leastCompletedSkills = List.of();

        // TODO: 获取分类统计
        List<SkillStatsVO.CategoryStat> categoryStats = List.of();

        return SkillStatsVO.builder()
            .totalCategories(totalCategories.intValue())
            .totalSkills(totalSkills.intValue())
            .avgCompletionRate(avgCompletionRate)
            .topCompletedSkills(topCompletedSkills)
            .leastCompletedSkills(leastCompletedSkills)
            .categoryStats(categoryStats)
            .build();
    }

    /**
     * 转换为列表 VO
     */
    private SkillListVO convertToListVO(Skill skill) {
        // 获取分类名称
        String categoryName = "";
        if (skill.getCategoryId() != null) {
            SkillCategory category = skillCategoryMapper.selectById(skill.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        return SkillListVO.builder()
            .skillId(skill.getId())
            .name(skill.getName())
            .categoryId(skill.getCategoryId())
            .categoryName(categoryName)
            .level(skill.getLevel())
            .levelLabel(LEVEL_LABELS.getOrDefault(skill.getLevel(), skill.getLevel()))
            .description(skill.getDescription())
            .userCount(0) // TODO: 统计用户数量
            .completionRate(0.0) // TODO: 计算完成率
            .createTime(skill.getCreateTime())
            .build();
    }
}