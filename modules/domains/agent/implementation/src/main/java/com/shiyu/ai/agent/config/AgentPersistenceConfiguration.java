package com.shiyu.ai.agent.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis adapter assembly owned by the Agent bounded context.
 *
 * <p>The central DAL must not scan Agent tables. Keeping the scan beside the
 * Agent persistence adapters makes the ownership boundary explicit and keeps
 * other bounded contexts from accidentally registering these mappers.</p>
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.agent.persistence.mapper")
public class AgentPersistenceConfiguration {
}
