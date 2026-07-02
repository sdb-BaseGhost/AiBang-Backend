package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.request.SubmitExerciseRequest;
import org.sdb.aiban.dto.response.ChapterVO;
import org.sdb.aiban.dto.response.ExerciseResultVO;
import org.sdb.aiban.dto.response.ExerciseVO;
import org.sdb.aiban.entity.*;
import org.sdb.aiban.mapper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final SkillChapterMapper chapterMapper;
    private final ChapterExerciseMapper exerciseMapper;
    private final UserChapterProgressMapper chapterProgressMapper;
    private final UserSkillProgressMapper skillProgressMapper;
    private final ObjectMapper objectMapper;

    public List<ChapterVO> getChaptersWithProgress(Long userId, Long skillId) {
        LambdaQueryWrapper<SkillChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillChapter::getSkillId, skillId)
               .eq(SkillChapter::getStatus, "PUBLISHED")
               .orderByAsc(SkillChapter::getSortOrder);
        List<SkillChapter> chapters = chapterMapper.selectList(wrapper);

        LambdaQueryWrapper<UserChapterProgress> progressWrapper = new LambdaQueryWrapper<>();
        progressWrapper.eq(UserChapterProgress::getUserId, userId);
        List<UserChapterProgress> progresses = chapterProgressMapper.selectList(progressWrapper);
        Map<Long, UserChapterProgress> progressMap = progresses.stream()
                .collect(Collectors.toMap(UserChapterProgress::getChapterId, p -> p));

        return chapters.stream().map(chapter -> {
            ChapterVO vo = convertToVO(chapter);
            UserChapterProgress progress = progressMap.get(chapter.getId());
            vo.setUserStatus(progress != null ? progress.getStatus() : "NOT_STARTED");
            return vo;
        }).collect(Collectors.toList());
    }

    public ChapterVO getChapterDetail(Long userId, Long chapterId) {
        SkillChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }
        ChapterVO vo = convertToVO(chapter);
        UserChapterProgress progress = getUserChapterProgress(userId, chapterId);
        vo.setUserStatus(progress != null ? progress.getStatus() : "NOT_STARTED");
        return vo;
    }

    public List<ExerciseVO> getExercises(Long userId, Long chapterId) {
        SkillChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }
        LambdaQueryWrapper<ChapterExercise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChapterExercise::getChapterId, chapterId).orderByAsc(ChapterExercise::getSortOrder);
        List<ChapterExercise> exercises = exerciseMapper.selectList(wrapper);
        return exercises.stream().map(exercise -> ExerciseVO.builder()
                .exerciseId(exercise.getId())
                .question(exercise.getQuestion())
                .options(parseOptions(exercise.getOptions()))
                .sortOrder(exercise.getSortOrder())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public ExerciseResultVO submitExercise(Long userId, SubmitExerciseRequest request) {
        SkillChapter chapter = chapterMapper.selectById(request.getChapterId());
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }
        LambdaQueryWrapper<ChapterExercise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChapterExercise::getChapterId, request.getChapterId());
        List<ChapterExercise> exercises = exerciseMapper.selectList(wrapper);
        Map<Long, ChapterExercise> exerciseMap = exercises.stream()
                .collect(Collectors.toMap(ChapterExercise::getId, e -> e));

        int correctCount = 0;
        List<ExerciseResultVO.ExerciseDetail> details = new ArrayList<>();
        for (SubmitExerciseRequest.ExerciseAnswer userAnswer : request.getAnswers()) {
            ChapterExercise exercise = exerciseMap.get(userAnswer.getExerciseId());
            if (exercise == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "练习题不存在");
            }
            boolean isCorrect = exercise.getAnswer().equals(userAnswer.getAnswer());
            if (isCorrect) correctCount++;
            details.add(ExerciseResultVO.ExerciseDetail.builder()
                    .exerciseId(exercise.getId())
                    .userAnswer(userAnswer.getAnswer())
                    .correctAnswer(exercise.getAnswer())
                    .isCorrect(isCorrect)
                    .explanation(exercise.getExplanation())
                    .build());
        }

        boolean allCorrect = correctCount == exercises.size();
        int skillProgress = 0;
        if (allCorrect) {
            updateChapterProgress(userId, request.getChapterId());
            skillProgress = calculateSkillProgress(chapter.getSkillId(), userId);
            updateSkillProgress(userId, chapter.getSkillId(), skillProgress);
        }

        return ExerciseResultVO.builder()
                .allCorrect(allCorrect)
                .correctCount(correctCount)
                .totalCount(exercises.size())
                .skillProgress(skillProgress)
                .details(details)
                .build();
    }

    private void updateChapterProgress(Long userId, Long chapterId) {
        UserChapterProgress progress = getUserChapterProgress(userId, chapterId);
        if (progress == null) {
            progress = new UserChapterProgress();
            progress.setUserId(userId);
            progress.setChapterId(chapterId);
            progress.setScore(100);
            progress.setStatus("COMPLETED");
            chapterProgressMapper.insert(progress);
        } else if (!"COMPLETED".equals(progress.getStatus())) {
            progress.setScore(100);
            progress.setStatus("COMPLETED");
            chapterProgressMapper.updateById(progress);
        }
    }

    private int calculateSkillProgress(Long skillId, Long userId) {
        Long totalChapters = chapterMapper.selectCount(
                new LambdaQueryWrapper<SkillChapter>()
                        .eq(SkillChapter::getSkillId, skillId)
                        .eq(SkillChapter::getStatus, "PUBLISHED"));
        if (totalChapters == 0) return 0;

        List<SkillChapter> chapters = chapterMapper.selectList(
                new LambdaQueryWrapper<SkillChapter>()
                        .eq(SkillChapter::getSkillId, skillId)
                        .eq(SkillChapter::getStatus, "PUBLISHED"));
        List<Long> chapterIds = chapters.stream().map(SkillChapter::getId).collect(Collectors.toList());

        Long completedChapters = chapterProgressMapper.selectCount(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .in(UserChapterProgress::getChapterId, chapterIds)
                        .eq(UserChapterProgress::getStatus, "COMPLETED"));

        return (int) (completedChapters * 100 / totalChapters);
    }

    private void updateSkillProgress(Long userId, Long skillId, int progress) {
        UserSkillProgress skillProgress = skillProgressMapper.selectOne(
                new LambdaQueryWrapper<UserSkillProgress>()
                        .eq(UserSkillProgress::getUserId, userId)
                        .eq(UserSkillProgress::getSkillId, skillId));
        if (skillProgress == null) {
            skillProgress = new UserSkillProgress();
            skillProgress.setUserId(userId);
            skillProgress.setSkillId(skillId);
            skillProgress.setProgress(progress);
            skillProgress.setStatus(progress >= 100 ? "COMPLETED" : "IN_PROGRESS");
            skillProgressMapper.insert(skillProgress);
        } else {
            skillProgress.setProgress(progress);
            skillProgress.setStatus(progress >= 100 ? "COMPLETED" : "IN_PROGRESS");
            skillProgressMapper.updateById(skillProgress);
        }
    }

    private UserChapterProgress getUserChapterProgress(Long userId, Long chapterId) {
        return chapterProgressMapper.selectOne(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .eq(UserChapterProgress::getChapterId, chapterId));
    }

    private ChapterVO convertToVO(SkillChapter chapter) {
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

    private List<String> parseOptions(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Parse options JSON failed", e);
            return new ArrayList<>();
        }
    }
}
