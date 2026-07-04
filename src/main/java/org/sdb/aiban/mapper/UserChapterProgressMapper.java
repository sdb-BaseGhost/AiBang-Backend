package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sdb.aiban.dto.response.LearningRecordVO;
import org.sdb.aiban.entity.UserChapterProgress;

@Mapper
public interface UserChapterProgressMapper extends BaseMapper<UserChapterProgress> {

    /**
     * 分页查询已完成的学习记录（关联用户、章节、技能）
     */
    IPage<LearningRecordVO> selectCompletedRecords(Page<LearningRecordVO> page,
                                                    @Param("userId") Long userId);
}
