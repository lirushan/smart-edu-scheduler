package com.smartedu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * 虚拟线程配置（Java 21+）
 *
 * application.yml 中 spring.threads.virtual.enabled=true 已启用全局虚拟线程，
 * 此处额外配置用于需要显式控制线程池的场景。
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * 虚拟线程执行器（用于 @Async 标注的方法）
     */
    @Bean(name = "virtualThreadExecutor")
    public java.util.concurrent.Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
