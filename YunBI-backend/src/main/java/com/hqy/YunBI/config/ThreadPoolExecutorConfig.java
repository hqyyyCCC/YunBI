package com.hqy.YunBI.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
@Configuration
public class ThreadPoolExecutorConfig {
    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {


        int corePoolSize = 2;
        int maximumPoolSize = 4;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.MINUTES;
        // ArrayBlockingQueue 是存放在内存中
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(4);
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count);
                count++;
                return thread;
            }
        };

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);

        return threadPoolExecutor;
    }
}
