package com.smartedu.handler;

import com.rabbitmq.client.Channel;
import com.smartedu.config.RabbitMQConfig;
import com.smartedu.entity.CrsOffering;
import com.smartedu.mapper.CrsOfferingMapper;
import com.smartedu.mapper.RegEnrollmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MQ 消费者：异步同步 Redis 名额到 DB enrolled_count
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentQuotaSyncHandler {

    private final CrsOfferingMapper offeringMapper;
    private final RegEnrollmentMapper enrollmentMapper;

    @RabbitListener(queues = RabbitMQConfig.ENROLLMENT_SYNC_QUEUE)
    public void handle(Long offeringId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            CrsOffering offering = offeringMapper.selectById(offeringId);
            if (offering != null) {
                // 从选课记录重新计算 enrolled_count 并更新
                int enrolledCount = countEnrolled(offeringId);
                offering.setEnrolledCount(enrolledCount);
                offeringMapper.updateById(offering);
                log.info("已同步课程名额: offeringId={}, enrolledCount={}", offeringId, enrolledCount);
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("同步课程名额失败: offeringId={}", offeringId, e);
            try {
                channel.basicNack(tag, false, false); // 不重新入队，进入 DLX
            } catch (IOException ex) {
                log.error("nack 失败", ex);
            }
        }
    }

    private int countEnrolled(Long offeringId) {
        return Math.toIntExact(enrollmentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.smartedu.entity.RegEnrollment>()
                        .eq(com.smartedu.entity.RegEnrollment::getOfferingId, offeringId)
                        .eq(com.smartedu.entity.RegEnrollment::getStatus, 0)));
    }
}
