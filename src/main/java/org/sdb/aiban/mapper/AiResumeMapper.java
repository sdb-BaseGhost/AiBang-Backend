package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.sdb.aiban.entity.AiResume;

import java.time.LocalDateTime;

@Mapper
public interface AiResumeMapper extends BaseMapper<AiResume> {

    /**
     * 统计指定时间范围内的 Token 总消耗
     */
    @Select("SELECT COALESCE(SUM(total_tokens), 0) FROM ai_resume WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    Long selectDailyTokens(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
