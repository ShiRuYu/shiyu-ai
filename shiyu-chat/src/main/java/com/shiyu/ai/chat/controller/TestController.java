package com.shiyu.ai.chat.controller;

import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Resource
    private ThreadPoolManager threadPoolManager;

    @GetMapping
    public String test() {
        ExecutorService executor = threadPoolManager.getExecutor(PoolType.DEFAULT);
        executor.execute(() -> {
            log.info("test1");
        });
        return "test";
    }
}
