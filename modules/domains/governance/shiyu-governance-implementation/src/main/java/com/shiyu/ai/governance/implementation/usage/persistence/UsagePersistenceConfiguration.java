package com.shiyu.ai.governance.implementation.usage.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * UsagePersistenceConfiguration 配置组件，负责注册和配置治理领域相关基础设施。
 */
@Configuration
@MapperScan("com.shiyu.ai.governance.implementation.usage.persistence.mapper")
public class UsagePersistenceConfiguration {}
