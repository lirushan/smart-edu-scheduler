package com.smartedu.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.smartedu.config.RabbitMQConfig;
import com.smartedu.entity.ExamQuestion;
import com.smartedu.entity.ExamRecord;
import com.smartedu.mapper.ExamQuestionMapper;
import com.smartedu.mapper.ExamRecordMapper;
import com.smartedu.service.AiScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * MQ 消费者：异步处理交卷后的填空题 AI 评分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamSubmitHandler {

    private final ExamQuestionMapper questionMapper;
    private final ExamRecordMapper recordMapper;
    private final AiScoringService aiScoringService;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConfig.EXAM_SUBMIT_QUEUE)
    public void handle(Map<String, Object> payload, Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            Long examId = ((Number) payload.get("examId")).longValue();
            Long studentId = ((Number) payload.get("studentId")).longValue();
            Long recordId = ((Number) payload.get("recordId")).longValue();

            ExamRecord record = recordMapper.selectById(recordId);
            if (record == null) {
                channel.basicAck(tag, false);
                return;
            }

            // 获取试题（含答案）
            List<ExamQuestion> questions = questionMapper.selectByExamIdWithAnswer(examId);

            // 只处理填空题 (type=4)
            BigDecimal subjectiveScore = BigDecimal.ZERO;
            StringBuilder feedbackBuilder = new StringBuilder();

            for (ExamQuestion q : questions) {
                if (q.getQuestionType() != 4) continue;

                // 从学生答案中查找该题的答案
                String studentAnswer = extractAnswer(record.getAnswers(), q.getId());
                AiScoringService.AiScoringResult result = aiScoringService.scoreFillBlank(
                        q.getContent(), q.getAnswer(), studentAnswer, BigDecimal.ONE);

                subjectiveScore = subjectiveScore.add(result.score());
                if (result.feedback() != null && !result.feedback().isEmpty()) {
                    feedbackBuilder.append("题").append(q.getId()).append(": ")
                            .append(result.feedback()).append("\n");
                }
            }

            // 更新总分
            record.setTotalScore(record.getObjectiveScore().add(subjectiveScore));
            record.setAiFeedback(feedbackBuilder.toString());
            recordMapper.updateById(record);

            log.info("异步评分完成: recordId={}, totalScore={}", recordId, record.getTotalScore());
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("异步评分失败: payload={}", payload, e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("nack 失败", ex);
            }
        }
    }

    private String extractAnswer(String answersJson, Long questionId) {
        try {
            List<Map<String, Object>> answers = objectMapper.readValue(answersJson, List.class);
            for (Map<String, Object> ans : answers) {
                Object qid = ans.get("questionId");
                if (qid != null && ((Number) qid).longValue() == questionId) {
                    return String.valueOf(ans.get("answer"));
                }
            }
        } catch (Exception e) {
            log.warn("解析答案JSON失败: {}", answersJson);
        }
        return "";
    }
}
