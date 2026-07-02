package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.CreateChapterRequest;
import org.sdb.aiban.dto.request.CreateExerciseRequest;
import org.sdb.aiban.dto.request.UpdateChapterRequest;
import org.sdb.aiban.dto.response.ChapterVO;
import org.sdb.aiban.entity.ChapterExercise;
import org.sdb.aiban.entity.Skill;
import org.sdb.aiban.entity.SkillChapter;
import org.sdb.aiban.mapper.ChapterExerciseMapper;
import org.sdb.aiban.mapper.SkillChapterMapper;
import org.sdb.aiban.mapper.SkillMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterAdminService {

    private final SkillChapterMapper chapterMapper;
    private final ChapterExerciseMapper exerciseMapper;
    private final SkillMapper skillMapper;

    /**
     * 获取技能的章节列表（管理版）
     */
    public List<ChapterVO> getChaptersBySkillId(Long skillId) {
        // 验证技能是否存在
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能不存在");
        }

        LambdaQueryWrapper<SkillChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillChapter::getSkillId, skillId)
               .orderByAsc(SkillChapter::getSortOrder);

        List<SkillChapter> chapters = chapterMapper.selectList(wrapper);
        return chapters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建章节
     */
    @Transactional
    public ChapterVO createChapter(CreateChapterRequest request) {
        // 验证技能是否存在
        Skill skill = skillMapper.selectById(request.getSkillId());
        if (skill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能不存在");
        }

        SkillChapter chapter = new SkillChapter();
        chapter.setSkillId(request.getSkillId());
        chapter.setTitle(request.getTitle());
        chapter.setContent(request.getContent());
        chapter.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        chapter.setStatus("PUBLISHED");

        chapterMapper.insert(chapter);
        return convertToVO(chapter);
    }

    /**
     * 更新章节
     */
    @Transactional
    public ChapterVO updateChapter(Long chapterId, UpdateChapterRequest request) {
        SkillChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }

        if (request.getTitle() != null) {
            chapter.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            chapter.setContent(request.getContent());
        }
        if (request.getSortOrder() != null) {
            chapter.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            chapter.setStatus(request.getStatus());
        }

        chapterMapper.updateById(chapter);
        return convertToVO(chapter);
    }

    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(Long chapterId) {
        SkillChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }

        // 删除章节下的所有练习题
        LambdaQueryWrapper<ChapterExercise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChapterExercise::getChapterId, chapterId);
        exerciseMapper.delete(wrapper);

        // 删除章节
        chapterMapper.deleteById(chapterId);
    }

    /**
     * 添加练习题
     */
    @Transactional
    public void addExercise(CreateExerciseRequest request) {
        // 验证章节是否存在
        SkillChapter chapter = chapterMapper.selectById(request.getChapterId());
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }

        // 验证答案索引是否有效
        if (request.getAnswer() < 0 || request.getAnswer() >= request.getOptions().size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "答案索引无效");
        }

        ChapterExercise exercise = new ChapterExercise();
        exercise.setChapterId(request.getChapterId());
        exercise.setQuestion(request.getQuestion());
        exercise.setOptions(convertListToJson(request.getOptions()));
        exercise.setAnswer(request.getAnswer());
        exercise.setExplanation(request.getExplanation());
        exercise.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        exerciseMapper.insert(exercise);
    }

    /**
     * 删除练习题
     */
    @Transactional
    public void deleteExercise(Long exerciseId) {
        ChapterExercise exercise = exerciseMapper.selectById(exerciseId);
        if (exercise == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "练习题不存在");
        }
        exerciseMapper.deleteById(exerciseId);
    }

    /**
     * 转换为 VO
     */
    private ChapterVO convertToVO(SkillChapter chapter) {
        // 统计练习题数量
        Long exerciseCount = exerciseMapper.selectCount(
                new LambdaQueryWrapper<ChapterExercise>()
                        .eq(ChapterExercise::getChapterId, chapter.getId()));

        return ChapterVO.builder()
                .chapterId(chapter.getId())
                .skillId(chapter.getSkillId())
                .title(chapter.getTitle())
                .content(chapter.getContent())
                .sortOrder(chapter.getSortOrder())
                .status(chapter.getStatus())
                .exerciseCount(exerciseCount.intValue())
                .createTime(chapter.getCreateTime())
                .build();
    }

    /**
     * List 转 JSON 字符串
     */
    private String convertListToJson(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}
