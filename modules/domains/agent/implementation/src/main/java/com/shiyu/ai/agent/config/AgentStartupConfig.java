package com.shiyu.ai.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Agent startup hook.
 *
 * <p>Agent definitions are owned by the Agent bounded context and seeded by
 * its schema. The application must not create sample agents at startup: that
 * made boot non-idempotent, coupled Agent to every optional capability, and
 * hid persistence failures behind broad exception handling.</p>
 */
@Slf4j
@Component
public final class AgentStartupConfig implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        log.info("Agent 使用惰性加载，定义和版本由领域 seed 数据初始化");
    }
}
