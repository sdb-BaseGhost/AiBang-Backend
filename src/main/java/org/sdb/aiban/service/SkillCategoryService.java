package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.CreateSkillCategoryRequest;
import org.sdb.aiban.dto.request.UpdateSkillCategoryRequest;
import org.sdb.aiban.dto.response.SkillCategoryVO;
import org.sdb.aiban.entity.SkillCategory;
import org.sdb.aiban.mapper.SkillCategoryMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SkillCategoryService {

    @Autowired
    private SkillCategoryMapper skillCategoryMapper;

    /**
     * 获取技能分类列表
     */
    public List<SkillCategoryVO> getCategories() {
        List<SkillCategory> categories = skillCategoryMapper.selectList(
            new LambdaQueryWrapper<SkillCategory>()
                .orderByDesc(SkillCategory::getSortOrder)
                .orderByAsc(SkillCategory::getId)
        );
        return categories.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    /**
     * 创建技能分类
     */
    @Transactional
    public SkillCategoryVO createCategory(CreateSkillCategoryRequest request) {
        SkillCategory category = new SkillCategory();
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setDescription(request.getDescription());
        skillCategoryMapper.insert(category);
        return convertToVO(category);
    }

    /**
     * 更新技能分类
     */
    @Transactional
    public SkillCategoryVO updateCategory(Long categoryId, UpdateSkillCategoryRequest request) {
        SkillCategory category = skillCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能分类不存在");
        }
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        skillCategoryMapper.updateById(category);
        return convertToVO(category);
    }

    /**
     * 删除技能分类
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        SkillCategory category = skillCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能分类不存在");
        }
        // TODO: 检查分类下是否有技能，如果有则不能删除
        skillCategoryMapper.deleteById(categoryId);
    }

    /**
     * 转换为 VO
     */
    private SkillCategoryVO convertToVO(SkillCategory category) {
        return SkillCategoryVO.builder()
            .categoryId(category.getId())
            .name(category.getName())
            .icon(category.getIcon())
            .sortOrder(category.getSortOrder())
            .skillCount(0) // TODO: 统计技能数量
            .totalUsers(0) // TODO: 统计用户数量
            .avgCompletionRate(0.0) // TODO: 计算平均完成率
            .build();
    }
}