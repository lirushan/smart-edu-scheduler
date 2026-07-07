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
 * MQ 消费者：异步推送通知（站内信 + 可选邮件）
 * V1.0 简化实现 — 日志记录通知事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushHandler {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_PUSH_QUEUE)
    public void handle(Map<String, Object> payload, Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            String type = (String) payload.getOrDefault("type", "unknown");
            Long userId = ((Number) payload.get("userId")).longValue();
            String message = (String) payload.getOrDefault("message", "");
            log.info("通知推送: type={}, userId={}, message={}", type, userId, message);
            // V1.0: 仅日志记录
            // V2.0: 接入站内信表 + 邮件服务
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("通知推送失败: payload={}", payload, e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("nack 失败", ex);
            }
        }
    }
}
