package com.shiyu.ai.agent.implementation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 智能体 Persistence 基础设施或应用能力的配置项及装配规则。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.agent.implementation.persistence.mapper")
public class AgentPersistenceConfiguration {}
