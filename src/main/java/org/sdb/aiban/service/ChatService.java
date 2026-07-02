package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.dto.response.ChatMessageVO;
import org.sdb.aiban.dto.response.ChatSessionVO;
import org.sdb.aiban.entity.AiChatMessage;
import org.sdb.aiban.entity.AiChatSession;
import org.sdb.aiban.mapper.AiChatMessageMapper;
import org.sdb.aiban.mapper.AiChatSessionMapper;
import org.sdb.aiban.common.util.MarkdownUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final DifyClient difyClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("sseExecutor")
    private final ExecutorService sseExecutor;

    public ChatSessionVO createSession(Long userId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle("");
        session.setStatus(0);
        sessionMapper.insert(session);

        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setMessageCount(0);
        vo.setCreateTime(session.getCreateTime());
        return vo;
    }
    public PageResult<ChatSessionVO> listSessions(Long userId, int page, int size) {
        Page<AiChatSession> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId)
                .eq(AiChatSession::getStatus, 0)
                .orderByDesc(AiChatSession::getUpdateTime);

        Page<AiChatSession> result = sessionMapper.selectPage(pageParam, wrapper);

        List<ChatSessionVO> voList = result.getRecords().stream().map(s -> {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setCreateTime(s.getCreateTime());

            AiChatMessage lastMsg = messageMapper.selectOne(
                    new LambdaQueryWrapper<AiChatMessage>()
                            .eq(AiChatMessage::getSessionId, s.getId())
                            .orderByDesc(AiChatMessage::getCreateTime)
                            .last("LIMIT 1"));
            if (lastMsg != null) {
                vo.setLastMessage(lastMsg.getContent().length() > 50
                        ? lastMsg.getContent().substring(0, 50) + "..."
                        : lastMsg.getContent());
                vo.setLastMessageTime(lastMsg.getCreateTime());
            }

            Long count = messageMapper.selectCount(
                    new LambdaQueryWrapper<AiChatMessage>()
                            .eq(AiChatMessage::getSessionId, s.getId()));
            vo.setMessageCount(count.intValue());

            return vo;
        }).collect(Collectors.toList());

        PageResult<ChatSessionVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        return pageResult;
    }

    public void deleteSession(Long userId, Long sessionId) {
        getSessionAndCheckAuth(userId, sessionId);
        sessionMapper.deleteById(sessionId);
    }
    // ==================== 流式消息 ====================

    public SseEmitter streamChat(Long userId, Long sessionId, String content) {
        AiChatSession session = getSessionAndCheckAuth(userId, sessionId);

        // 1. 立即存储用户消息
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(content);
        userMsg.setTokenCount(0);
        messageMapper.insert(userMsg);

        // 生成会话标题（首条消息取前20字）
        if (session.getTitle() == null || session.getTitle().isEmpty()) {
            String title = content.length() > 20 ? content.substring(0, 20) : content;
            session.setTitle(title);
            sessionMapper.updateById(session);
        }

        // 2. 创建 SSE 发射器（超时 3 分钟）
        SseEmitter emitter = new SseEmitter(180000L);

        // 3. 异步调用 Dify
        sseExecutor.submit(() -> {
            StringBuilder fullAnswer = new StringBuilder();
            String[] difyMessageId = {null};
            String[] difyConversationId = {null};
            int[] tokenCount = {0};

            try {
                difyClient.streamChat(content, session.getDifyConversationId(),
                        String.valueOf(userId), (event, json) -> {
                            try {
                                if ("message".equals(event)) {
                                    JsonNode node = objectMapper.readTree(json);
                                    String answer = node.has("answer") ? node.get("answer").asText() : "";
                                    fullAnswer.append(answer);
                                    if (node.has("message_id")) {
                                        difyMessageId[0] = node.get("message_id").asText();
                                    }
                                    Map<String, Object> sseData = new HashMap<>();
                                    sseData.put("event", "message");
                                    sseData.put("answer", answer);
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(objectMapper.writeValueAsString(sseData)));
                                } else if ("message_end".equals(event)) {
                                    JsonNode node = objectMapper.readTree(json);
                                    if (node.has("conversation_id")) {
                                        difyConversationId[0] = node.get("conversation_id").asText();
                                    }
                                    if (node.has("message_id")) {
                                        difyMessageId[0] = node.get("message_id").asText();
                                    }
                                    if (node.has("metadata") && node.get("metadata").has("usage")) {
                                        JsonNode usage = node.get("metadata").get("usage");
                                        if (usage.has("total_tokens")) {
                                            tokenCount[0] = usage.get("total_tokens").asInt();
                                        }
                                    }
                                    saveAssistantMessage(session, fullAnswer.toString(),
                                            difyMessageId[0], difyConversationId[0], tokenCount[0]);
                                    Map<String, Object> endData = new HashMap<>();
                                    endData.put("event", "message_end");
                                    endData.put("conversation_id", difyConversationId[0]);
                                    endData.put("message_id", difyMessageId[0]);
                                    emitter.send(SseEmitter.event()
                                            .name("message_end")
                                            .data(objectMapper.writeValueAsString(endData)));
                                    emitter.complete();
                                } else if ("error".equals(event)) {
                                    log.error("[Chat] Dify error event: {}", json);
                                    sendErrorEvent(emitter, "服务器繁忙，请稍后再试");
                                }
                            } catch (IOException e) {
                                log.warn("[Chat] Failed to send SSE event: {}", e.getMessage());
                            } catch (Exception e) {
                                log.error("[Chat] Error processing Dify event", e);
                            }
                        });
            } catch (Exception e) {
                log.error("[Chat] Dify stream error", e);
                try {
                    sendErrorEvent(emitter, "服务器繁忙，请稍后再试");
                } catch (IOException ex) {
                    // client disconnected
                }
            }
        });
        emitter.onTimeout(() -> {
            log.warn("[Chat] SSE timeout for session {}", sessionId);
            emitter.complete();
        });
        emitter.onCompletion(() -> {
            log.info("[Chat] SSE completed for session {}", sessionId);
        });
        emitter.onError(e -> {
            log.warn("[Chat] SSE error for session {}: {}", sessionId, e.getMessage());
        });

        return emitter;
    }

    // ==================== 消息历史 ====================

    public PageResult<ChatMessageVO> listMessages(Long userId, Long sessionId, int page, int size) {
        getSessionAndCheckAuth(userId, sessionId);

        Page<AiChatMessage> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreateTime);

        Page<AiChatMessage> result = messageMapper.selectPage(pageParam, wrapper);

        List<ChatMessageVO> voList = result.getRecords().stream().map(m -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setMessageId(m.getId());
            vo.setRole(m.getRole());
            vo.setContent(m.getContent());
            vo.setCreateTime(m.getCreateTime());
            // assistant 消息生成 HTML（Markdown 渲染）
            if ("assistant".equals(m.getRole())) {
                vo.setHtmlContent(MarkdownUtil.toStyledHtml(m.getContent()));
            }
            return vo;
        }).collect(Collectors.toList());

        PageResult<ChatMessageVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        return pageResult;
    }

    // ==================== 内部方法 ====================

    private void saveAssistantMessage(AiChatSession session, String content,
                                      String difyMessageId, String difyConversationId,
                                      int tokenCount) {
        AiChatMessage assistantMsg = new AiChatMessage();
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(content);
        assistantMsg.setDifyMessageId(difyMessageId);
        assistantMsg.setTokenCount(tokenCount);
        messageMapper.insert(assistantMsg);

        if (difyConversationId != null &&
                (session.getDifyConversationId() == null || session.getDifyConversationId().isEmpty())) {
            session.setDifyConversationId(difyConversationId);
            sessionMapper.updateById(session);
        }

        log.info("[Chat] Saved assistant message: session={}, tokens={}", session.getId(), tokenCount);
    }

    private AiChatSession getSessionAndCheckAuth(Long userId, Long sessionId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.CHAT_SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CHAT_SESSION_FORBIDDEN);
        }
        if (session.getStatus() == 1) {
            throw new BusinessException(ResultCode.CHAT_SESSION_NOT_FOUND);
        }
        return session;
    }

    private void sendErrorEvent(SseEmitter emitter, String message) throws IOException {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("event", "error");
        errorData.put("message", message);
        emitter.send(SseEmitter.event()
                .name("error")
                .data(objectMapper.writeValueAsString(errorData)));
        emitter.complete();
    }
}