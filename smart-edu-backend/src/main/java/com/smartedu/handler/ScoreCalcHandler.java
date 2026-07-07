package com.smartedu.handler;

import com.rabbitmq.client.Channel;
import com.smartedu.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MQ 消费者：异步计算 GPA + 排名
 * V1.0 简化实现 — 成绩发布后异步计算
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreCalcHandler {

    @RabbitListener(queues = RabbitMQConfig.SCORE_CALC_QUEUE)
    public void handle(Map<String, Object> payload, Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            Long offeringId = ((Number) payload.get("offeringId")).longValue();
            log.info("开始异步计算成绩排名: offeringId={}", offeringId);
            // V1.0: 简单排名已在 ScoreService 中通过 raw_score DESC 排序实现
            // V2.0: 可扩展为更复杂的 GPA 计算
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("成绩计算失败: payload={}", payload, e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("nack 失败", ex);
            }
        }
    }
}
