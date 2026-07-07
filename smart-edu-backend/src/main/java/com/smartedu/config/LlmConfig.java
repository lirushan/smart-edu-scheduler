package com.smartedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 大模型配置 — 支持 OpenAI / DeepSeek 等兼容 API
 *
 * 配置项绑定 application.yml 中的 llm.* 前缀
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    /** 大模型提供商: openai | deepseek */
    private String provider = "openai";

    /** API Key，通过环境变量 LLM_API_KEY 注入，为空时自动降级到 Mock */
    private String apiKey;

    /** API 基础 URL */
    private String baseUrl = "https://api.openai.com/v1";

    /** 模型名称 */
    private String model = "gpt-4o-mini";

    /** 温度参数 (0.0~2.0)，越低越确定 */
    private double temperature = 0.1;

    /** 最大输出 Token 数 */
    private int maxTokens = 200;

    /** HTTP 超时时间（秒） */
    private int timeoutSeconds = 5;

    /** 失败重试次数 */
    private int maxRetries = 2;

    /** 评分提示词模板，支持占位符 {question} {referenceAnswer} {studentAnswer} {maxScore} */
    private String scoringPrompt;

    /**
     * 构建评分提示词，替换模板占位符
     *
     * @param question        题目内容
     * @param referenceAnswer 参考答案
     * @param studentAnswer   学生答案
     * @param maxScore        满分
     * @return 填充后的提示词
     */
    public String buildScoringPrompt(String question, String referenceAnswer,
                                     String studentAnswer, int maxScore) {
        if (scoringPrompt == null || scoringPrompt.isBlank()) {
            return "";
        }
        return scoringPrompt
                .replace("{question}", question != null ? question : "")
                .replace("{referenceAnswer}", referenceAnswer != null ? referenceAnswer : "")
                .replace("{studentAnswer}", studentAnswer != null ? studentAnswer : "")
                .replace("{maxScore}", String.valueOf(maxScore));
    }

    /**
     * 判断 LLM API 是否可用（API Key 已配置）
     */
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }
}
