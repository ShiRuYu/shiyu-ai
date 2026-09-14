package com.shiyu.ai.agent.implementation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * AgentPersistenceConfiguration 配置组件，负责注册和配置智能体领域相关基础设施。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.agent.implementation.persistence.mapper")
public class AgentPersistenceConfiguration {}
