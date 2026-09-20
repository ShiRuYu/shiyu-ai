package com.shiyu.ai.governance.implementation.usage.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 用量 Persistence 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
@MapperScan("com.shiyu.ai.governance.implementation.usage.persistence.mapper")
public class UsagePersistenceConfiguration {}
