package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sdb.aiban.entity.AiChatMessage;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
