package com.example.demo.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configurable
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(64,
                128,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(65536),
                new ThreadPoolExecutor.AbortPolicy());
       }
}
