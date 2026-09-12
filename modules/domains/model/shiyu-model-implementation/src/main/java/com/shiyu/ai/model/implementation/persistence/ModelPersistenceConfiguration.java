package com.shiyu.ai.model.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * ModelPersistenceConfiguration 配置组件，负责注册和配置模型领域相关基础设施。
 */
@Configuration
@MapperScan("com.shiyu.ai.model.implementation.persistence.mapper")
public class ModelPersistenceConfiguration {}
