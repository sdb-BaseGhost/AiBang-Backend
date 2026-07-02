package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_message")
public class AiChatMessage extends BaseEntity {

    /** 会话ID */
    @TableField("session_id")
    private Long sessionId;

    /** 角色：user / assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** Dify消息ID */
    @TableField("dify_message_id")
    private String difyMessageId;

    /** 消耗token数 */
    @TableField("token_count")
    private Integer tokenCount;
}
