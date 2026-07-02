package org.sdb.aiban.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Dify API 客户端
 * 负责与 Dify chat-messages 接口通信，处理 SSE 流式响应
 */
@Slf4j
@Component
public class DifyClient {

    @Value("${dify.api-url}")
    private String difyApiUrl;

    @Value("${dify.api-key}")
    private String difyApiKey;

    @Value("${dify.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${dify.read-timeout:120000}")
    private int readTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 向 Dify 发送流式请求，逐行解析 SSE 事件并通过回调返回
     *
     * @param query            用户消息
     * @param conversationId   Dify 会话ID（首条消息传 null）
     * @param userId           用户ID（用于 Dify 端标识）
     * @param onEvent          回调：(eventType, jsonData)
     */
    public void streamChat(String query, String conversationId, String userId,
                           BiConsumer<String, String> onEvent) throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", new HashMap<>());
        body.put("query", query);
        body.put("response_mode", "streaming");
        body.put("user", userId);
        if (conversationId != null && !conversationId.isEmpty()) {
            body.put("conversation_id", conversationId);
        }

        String jsonBody = objectMapper.writeValueAsString(body);
        log.info("[Dify] Request: query={}, conversationId={}, user={}", query, conversationId, userId);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(difyApiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + difyApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofMillis(readTimeout))
                .build();

        HttpResponse<InputStream> response = client.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            log.error("[Dify] HTTP error {}: {}", response.statusCode(), errorBody);
            throw new RuntimeException("Dify API returned " + response.statusCode());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith(":")) {
                    continue; // 跳过空行和注释
                }
                if (line.startsWith("data: ")) {
                    String json = line.substring(6).trim();
                    try {
                        JsonNode node = objectMapper.readTree(json);
                        String event = node.has("event") ? node.get("event").asText() : "unknown";
                        onEvent.accept(event, json);
                    } catch (Exception e) {
                        log.warn("[Dify] Failed to parse SSE data: {}", json, e);
                    }
                }
            }
        }

        log.info("[Dify] Stream completed");
    }
}
