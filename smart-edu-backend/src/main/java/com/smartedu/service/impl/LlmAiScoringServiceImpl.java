package com.smartedu.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartedu.config.LlmConfig;
import com.smartedu.service.AiScoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * LLM AI 评分实现 — 调用 OpenAI 兼容 API 进行填空题语义评分
 *
 * 特性：
 * - 支持 OpenAI / DeepSeek 等兼容 API
 * - 失败自动重试（最多 2 次，指数退避 500ms/1000ms）
 * - API Key 未配置或调用失败时自动降级到 MockAiScoringServiceImpl
 * - 记录每次调用的耗时和 Token 用量
 */
@Slf4j
@Primary
@Service
public class LlmAiScoringServiceImpl implements AiScoringService {

    private final LlmConfig llmConfig;
    private final MockAiScoringServiceImpl mockService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public LlmAiScoringServiceImpl(LlmConfig llmConfig,
                                   MockAiScoringServiceImpl mockService,
                                   ObjectMapper objectMapper) {
        this.llmConfig = llmConfig;
        this.mockService = mockService;
        this.objectMapper = objectMapper;
        this.restClient = buildRestClient();
    }

    /**
     * 构建 RestClient，配置超时和认证头
     */
    private RestClient buildRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = (int) Duration.ofSeconds(llmConfig.getTimeoutSeconds()).toMillis();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        return RestClient.builder()
                .baseUrl(llmConfig.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + llmConfig.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public AiScoringResult scoreFillBlank(String questionContent, String referenceAnswer,
                                          String studentAnswer, BigDecimal maxScore) {
        // 降级：LLM 不可用时回退到 Mock
        if (!llmConfig.isAvailable()) {
            log.debug("LLM API Key 未配置，使用 Mock 评分");
            return mockService.scoreFillBlank(questionContent, referenceAnswer, studentAnswer, maxScore);
        }

        // 构建提示词
        String prompt = llmConfig.buildScoringPrompt(questionContent, referenceAnswer,
                studentAnswer, maxScore.intValue());
        if (prompt.isBlank()) {
            log.warn("Scoring prompt 模板为空，降级到 Mock");
            return mockService.scoreFillBlank(questionContent, referenceAnswer, studentAnswer, maxScore);
        }

        // 构建请求体
        Map<String, Object> requestBody = buildRequestBody(prompt);

        // 带重试的 LLM 调用
        int maxRetries = llmConfig.getMaxRetries();
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                long startTime = System.currentTimeMillis();
                String response = callLlmApi(requestBody);
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("LLM 评分调用成功: model={}, elapsed={}ms", llmConfig.getModel(), elapsed);

                AiScoringResult result = parseResponse(response, maxScore);
                if (result != null) {
                    return result;
                }
                // 解析失败 → 重试
                log.warn("LLM 响应解析失败 (attempt {}/{}), 尝试重试",
                        attempt + 1, maxRetries + 1);
            } catch (Exception e) {
                log.warn("LLM 调用异常 (attempt {}/{}): {}",
                        attempt + 1, maxRetries + 1, e.getMessage());
            }

            // 指数退避：500ms, 1000ms
            if (attempt < maxRetries) {
                long backoffMs = (long) (500 * Math.pow(2, attempt));
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 全部重试失败 → 降级到 Mock
        log.error("LLM 调用全部失败 ({} 次尝试)，降级到 Mock 评分", maxRetries + 1);
        return mockService.scoreFillBlank(questionContent, referenceAnswer, studentAnswer, maxScore);
    }

    /**
     * 构建 OpenAI 兼容 API 请求体
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", llmConfig.getModel(),
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "你是一位专业的课程评分教师。请严格按照用户要求，只返回JSON格式结果。"),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", llmConfig.getTemperature(),
                "max_tokens", llmConfig.getMaxTokens()
        );
    }

    /**
     * 调用 LLM API
     */
    private String callLlmApi(Map<String, Object> requestBody) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        return restClient.post()
                .uri("/chat/completions")
                .body(jsonBody)
                .retrieve()
                .body(String.class);
    }

    /**
     * 解析 LLM 响应，提取 score 和 reason
     *
     * @return AiScoringResult，解析失败返回 null
     */
    private AiScoringResult parseResponse(String responseBody, BigDecimal maxScore) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 记录 Token 用量
            JsonNode usage = root.get("usage");
            if (usage != null) {
                log.info("LLM Token 用量: prompt={}, completion={}, total={}",
                        usage.path("prompt_tokens").asInt(0),
                        usage.path("completion_tokens").asInt(0),
                        usage.path("total_tokens").asInt(0));
            }

            // 提取 choices[0].message.content
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.warn("LLM 响应缺少 choices 字段");
                return null;
            }

            String content = choices.get(0).path("message").path("content").asText();
            if (content.isBlank()) {
                log.warn("LLM 响应 content 为空");
                return null;
            }
            log.debug("LLM 响应内容: {}", content);

            // 提取 JSON（可能被 markdown 代码块包裹）
            String jsonStr = extractJsonFromContent(content);
            JsonNode resultNode = objectMapper.readTree(jsonStr);

            BigDecimal score = resultNode.has("score")
                    ? new BigDecimal(resultNode.path("score").asText())
                    : BigDecimal.ZERO;
            String reason = resultNode.has("reason")
                    ? resultNode.path("reason").asText()
                    : "";

            // 分数边界检查
            if (score.compareTo(maxScore) > 0) {
                score = maxScore;
            }
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            }
            score = score.setScale(1, RoundingMode.HALF_UP);

            return new AiScoringResult(score, buildFeedbackText(score, maxScore), reason);

        } catch (Exception e) {
            log.error("解析 LLM 响应失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 LLM 返回内容中提取 JSON 字符串
     * 处理可能的 markdown 代码块包裹
     */
    private String extractJsonFromContent(String content) {
        if (content == null) {
            return "{}";
        }
        content = content.trim();

        // 移除 markdown 代码块 ```json ... ``` 或 ``` ... ```
        if (content.startsWith("```")) {
            int codeStart = content.indexOf('\n');
            int codeEnd = content.lastIndexOf("```");
            if (codeStart >= 0 && codeEnd > codeStart) {
                content = content.substring(codeStart + 1, codeEnd).trim();
            }
        }

        // 查找第一个 { 和最后一个 }
        int braceStart = content.indexOf('{');
        int braceEnd = content.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return content.substring(braceStart, braceEnd + 1);
        }

        return content;
    }

    /**
     * 根据分数生成中文反馈文本
     */
    private String buildFeedbackText(BigDecimal score, BigDecimal maxScore) {
        if (score.compareTo(maxScore) >= 0) {
            return "回答正确（AI 评分）";
        } else if (score.compareTo(BigDecimal.ZERO) > 0) {
            return "部分正确（AI 评分: " + score.stripTrailingZeros().toPlainString()
                    + "/" + maxScore.stripTrailingZeros().toPlainString() + "）";
        }
        return "回答错误（AI 评分）";
    }
}
