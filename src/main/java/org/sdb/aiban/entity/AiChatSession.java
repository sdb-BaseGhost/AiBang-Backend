package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_session")
public class AiChatSession extends BaseEntity {

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 会话标题 */
    private String title;

    /** Dify会话ID */
    @TableField("dify_conversation_id")
    private String difyConversationId;

    /** 状态：0-进行中 1-已归档 */
    private Integer status;
}
