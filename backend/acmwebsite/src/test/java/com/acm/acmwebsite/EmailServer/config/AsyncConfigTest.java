package com.acm.acmwebsite.EmailServer.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Test
    void mailExecutor_shouldHaveCorrectCorePoolSize() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.mailExecutor();
        assertThat(executor.getCorePoolSize()).isEqualTo(5);
    }

    @Test
    void mailExecutor_shouldHaveCorrectMaxPoolSize() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.mailExecutor();
        assertThat(executor.getMaxPoolSize()).isEqualTo(20);
    }

    @Test
    void mailExecutor_shouldHaveCorrectQueueCapacity() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.mailExecutor();
        assertThat(executor.getQueueCapacity()).isEqualTo(500);
    }

    @Test
    void mailExecutor_shouldHaveCorrectThreadNamePrefix() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.mailExecutor();
        assertThat(executor.getThreadNamePrefix()).isEqualTo("MailWorker-");
    }

    @Test
    void mailExecutor_shouldBeInitialized() {
        Executor executor = asyncConfig.mailExecutor();
        assertThat(executor).isNotNull();
    }
}