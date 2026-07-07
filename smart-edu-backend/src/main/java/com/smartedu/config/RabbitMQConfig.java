package com.smartedu.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置：交换机、队列、绑定 + 死信机制
 */
@Configuration
public class RabbitMQConfig {

    // ===== 交换机 =====
    public static final String ENROLLMENT_EXCHANGE = "enrollment.exchange";
    public static final String EXAM_EXCHANGE = "exam.exchange";
    public static final String SCORE_EXCHANGE = "score.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // ===== 队列 =====
    public static final String ENROLLMENT_SYNC_QUEUE = "enrollment.sync.queue";
    public static final String EXAM_SUBMIT_QUEUE = "exam.submit.queue";
    public static final String SCORE_CALC_QUEUE = "score.calc.queue";
    public static final String NOTIFICATION_PUSH_QUEUE = "notification.push.queue";
    public static final String DLX_QUEUE = "dlx.queue";

    // ===== 路由键 =====
    public static final String ENROLLMENT_SYNC_KEY = "enrollment.sync";
    public static final String EXAM_SUBMIT_KEY = "exam.submit";
    public static final String SCORE_CALC_KEY = "score.calc";
    public static final String NOTIFICATION_PUSH_KEY = "notification.push";

    // ===== Message Converter =====
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(20);
        return factory;
    }

    // ===== 死信交换机 =====
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with("#");
    }

    // ===== 选课交换机 + 队列 =====
    @Bean
    public TopicExchange enrollmentExchange() {
        return new TopicExchange(ENROLLMENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue enrollmentSyncQueue() {
        return QueueBuilder.durable(ENROLLMENT_SYNC_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("enrollment.sync.dlx")
                .build();
    }

    @Bean
    public Binding enrollmentSyncBinding() {
        return BindingBuilder.bind(enrollmentSyncQueue()).to(enrollmentExchange()).with(ENROLLMENT_SYNC_KEY);
    }

    // ===== 考试交换机 + 队列 =====
    @Bean
    public TopicExchange examExchange() {
        return new TopicExchange(EXAM_EXCHANGE, true, false);
    }

    @Bean
    public Queue examSubmitQueue() {
        return QueueBuilder.durable(EXAM_SUBMIT_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("exam.submit.dlx")
                .build();
    }

    @Bean
    public Binding examSubmitBinding() {
        return BindingBuilder.bind(examSubmitQueue()).to(examExchange()).with(EXAM_SUBMIT_KEY);
    }

    // ===== 成绩交换机 + 队列 =====
    @Bean
    public TopicExchange scoreExchange() {
        return new TopicExchange(SCORE_EXCHANGE, true, false);
    }

    @Bean
    public Queue scoreCalcQueue() {
        return QueueBuilder.durable(SCORE_CALC_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("score.calc.dlx")
                .build();
    }

    @Bean
    public Binding scoreCalcBinding() {
        return BindingBuilder.bind(scoreCalcQueue()).to(scoreExchange()).with(SCORE_CALC_KEY);
    }

    // ===== 通知交换机 + 队列 =====
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationPushQueue() {
        return QueueBuilder.durable(NOTIFICATION_PUSH_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("notification.push.dlx")
                .build();
    }

    @Bean
    public Binding notificationPushBinding() {
        return BindingBuilder.bind(notificationPushQueue()).to(notificationExchange()).with(NOTIFICATION_PUSH_KEY);
    }
}
