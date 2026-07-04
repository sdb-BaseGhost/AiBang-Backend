package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.sdb.aiban.entity.AiChatMessage;

import java.time.LocalDateTime;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {

    /**
     * 统计指定时间范围内的 Token 总消耗
     */
    @Select("SELECT COALESCE(SUM(token_count), 0) FROM ai_chat_message WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    Long selectDailyTokens(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
