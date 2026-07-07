package com.smartedu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智教通 - 学生课程报名与排课管理系统
 *
 * @author smartedu
 * @since 1.0.0
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SmartEduApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartEduApplication.class, args);
    }
}
