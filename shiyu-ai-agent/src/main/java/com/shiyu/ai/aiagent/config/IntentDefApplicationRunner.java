package com.shiyu.ai.aiagent.config;

import com.shiyu.ai.aiagent.repository.IntentDefRepository;
import com.shiyu.ai.aiagent.bo.IntentDefBO;
import com.shiyu.ai.aiagent.node.intent.IntentDefinitionFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动时从 DB 加载意图定义到 IntentDefinitionFactory
 */
@Slf4j
@Component
public class IntentDefApplicationRunner implements ApplicationRunner {

    @Resource
    private IntentDefRepository intentDefRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<IntentDefBO> boList = intentDefRepository.selectByAgentId("default");
            if (boList == null || boList.isEmpty()) {
                log.warn("DB 中未找到意图定义数据（agentId=default），使用空表");
                // 即使没有数据也要调用 reloadFromDb 以清空 static 块可能遗留的数据
                IntentDefinitionFactory.reloadFromDb(List.of());
            } else {
                IntentDefinitionFactory.reloadFromDb(boList);
                log.info("从 DB 加载了 {} 条意图定义到 IntentDefinitionFactory", boList.size());
            }
        } catch (Exception e) {
            log.error("从 DB 加载意图定义失败，将使用空表", e);
            IntentDefinitionFactory.reloadFromDb(List.of());
        }
    }
}
