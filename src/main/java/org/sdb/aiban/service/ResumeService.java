package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.response.ResumeVO;
import org.sdb.aiban.entity.AiResume;
import org.sdb.aiban.mapper.AiResumeMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final AiResumeMapper resumeMapper;
    private final ResumeDifyClient resumeDifyClient;

    /**
     * 上传简历（保存文本内容）
     */
    public ResumeVO uploadResume(Long userId, String title, String content) {
        AiResume resume = new AiResume();
        resume.setUserId(userId);
        resume.setTitle(title);
        resume.setOriginalContent(content);
        resume.setStatus("UPLOADED");
        resumeMapper.insert(resume);

        return convertToVO(resume);
    }

    /**
     * 获取简历列表
     */
    public PageResult<ResumeVO> listResumes(Long userId, int page, int size) {
        Page<AiResume> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AiResume> wrapper = new LambdaQueryWrapper<AiResume>()
                .eq(AiResume::getUserId, userId)
                .orderByDesc(AiResume::getCreateTime);

        Page<AiResume> result = resumeMapper.selectPage(pageParam, wrapper);

        PageResult<ResumeVO> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        return pageResult;
    }

    /**
     * 获取简历详情
     */
    public ResumeVO getResumeDetail(Long userId, Long resumeId) {
        AiResume resume = getResumeAndCheckAuth(userId, resumeId);
        return convertToVO(resume);
    }

    /**
     * 删除简历
     */
    public void deleteResume(Long userId, Long resumeId) {
        getResumeAndCheckAuth(userId, resumeId);
        resumeMapper.deleteById(resumeId);
    }

    /**
     * 异步优化简历（调用Dify）
     */
    @Async
    public void optimizeResume(Long userId, Long resumeId) {
        AiResume resume = getResumeAndCheckAuth(userId, resumeId);

        // 更新状态为分析中
        resume.setStatus("ANALYZING");
        resumeMapper.updateById(resume);

        try {
            // 调用Dify接口
            ResumeDifyClient.DifyWorkflowResult result = resumeDifyClient.runWorkflow(
                    resume.getOriginalContent(),
                    String.valueOf(userId)
            );

            // 更新结果
            if ("succeeded".equals(result.getStatus())) {
                resume.setStatus("ANALYZED");
                resume.setOptimizedContent(result.getOptimizedResume());
                resume.setTaskId(result.getTaskId());
                resume.setWorkflowRunId(result.getWorkflowRunId());
                resume.setElapsedTime(result.getElapsedTime());
                resume.setTotalTokens(result.getTotalTokens());
            } else {
                resume.setStatus("ANALYZE_FAILED");
                resume.setErrorMessage(result.getError() != null ? result.getError() : "分析失败");
            }
        } catch (Exception e) {
            log.error("[Resume] Optimize failed: resumeId={}", resumeId, e);
            resume.setStatus("ANALYZE_FAILED");
            resume.setErrorMessage("服务器繁忙，请稍后再试");
        }

        resumeMapper.updateById(resume);
        log.info("[Resume] Optimize completed: resumeId={}, status={}", resumeId, resume.getStatus());
    }

    private AiResume getResumeAndCheckAuth(Long userId, Long resumeId) {
        AiResume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_FOUND);
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RESUME_FORBIDDEN);
        }
        return resume;
    }

    private ResumeVO convertToVO(AiResume resume) {
        ResumeVO vo = new ResumeVO();
        vo.setResumeId(resume.getId());
        vo.setTitle(resume.getTitle());
        vo.setStatus(resume.getStatus());
        vo.setOptimizedContent(resume.getOptimizedContent());
        vo.setElapsedTime(resume.getElapsedTime());
        vo.setTotalTokens(resume.getTotalTokens());
        vo.setCreateTime(resume.getCreateTime());
        vo.setUpdateTime(resume.getUpdateTime());
        return vo;
    }
}
