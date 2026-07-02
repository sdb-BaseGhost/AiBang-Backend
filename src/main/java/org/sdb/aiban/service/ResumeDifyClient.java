package org.sdb.aiban.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ResumeDifyClient {

    @Value("${dify.resume-api-url:https://api.dify.ai/v1/workflows/run}")
    private String difyApiUrl;

    @Value("${dify.resume-api-key}")
    private String difyApiKey;

    @Value("${dify.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${dify.read-timeout:120000}")
    private int readTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 Dify workflows/run 接口，同步等待结果
     *
     * @param resumeContent 简历文本内容
     * @param userId        用户ID
     * @return Dify响应结果
     */
    public DifyWorkflowResult runWorkflow(String resumeContent, String userId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> inputs = new HashMap<>();
        inputs.put("resume_content", resumeContent);
        body.put("inputs", inputs);
        body.put("response_mode", "blocking");
        body.put("user", userId);

        String jsonBody = objectMapper.writeValueAsString(body);
        log.info("[ResumeDify] Request: user={}, contentLength={}", userId, resumeContent.length());

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

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            log.error("[ResumeDify] HTTP error {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Dify API returned " + response.statusCode());
        }

        JsonNode node = objectMapper.readTree(response.body());
        JsonNode data = node.get("data");

        DifyWorkflowResult result = new DifyWorkflowResult();
        result.setTaskId(node.has("task_id") ? node.get("task_id").asText() : null);
        result.setWorkflowRunId(node.has("workflow_run_id") ? node.get("workflow_run_id").asText() : null);

        if (data != null) {
            result.setStatus(data.has("status") ? data.get("status").asText() : "unknown");
            if (data.has("outputs")) {
                JsonNode outputs = data.get("outputs");
                result.setOptimizedResume(outputs.has("optimized_resume") ? outputs.get("optimized_resume").asText() : null);
            }
            result.setElapsedTime(data.has("elapsed_time") ? data.get("elapsed_time").asDouble() : 0);
            result.setTotalTokens(data.has("total_tokens") ? data.get("total_tokens").asInt() : 0);
            result.setError(data.has("error") ? data.get("error").asText() : null);
        }

        log.info("[ResumeDify] Response: status={}, tokens={}", result.getStatus(), result.getTotalTokens());
        return result;
    }

    @Data
    public static class DifyWorkflowResult {
        private String taskId;
        private String workflowRunId;
        private String status;
        private String optimizedResume;
        private Double elapsedTime;
        private Integer totalTokens;
        private String error;
    }
}
