package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.SendMessageRequest;
import org.sdb.aiban.dto.response.ChatMessageVO;
import org.sdb.aiban.dto.response.ChatSessionVO;
import org.sdb.aiban.service.ChatService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "AI辅导", description = "AI对话会话与消息管理")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "创建新会话")
    @PostMapping("/session")
    public Result<ChatSessionVO> createSession(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chatService.createSession(userId));
    }

    @Operation(summary = "会话列表（分页）")
    @GetMapping("/sessions")
    public Result<PageResult<ChatSessionVO>> listSessions(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chatService.listSessions(userId, page, size));
    }

    @Operation(summary = "删除会话（软删除）")
    @DeleteMapping("/session/{id}")
    public Result<Void> deleteSession(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        chatService.deleteSession(userId, id);
        return Result.success();
    }

    @Operation(summary = "发送消息（SSE流式返回AI回复）")
    @PostMapping("/session/{id}/stream")
    public SseEmitter streamChat(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return chatService.streamChat(userId, id, request.getContent());
    }

    @Operation(summary = "消息历史（分页）")
    @GetMapping("/session/{id}/messages")
    public Result<PageResult<ChatMessageVO>> listMessages(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chatService.listMessages(userId, id, page, size));
    }
}