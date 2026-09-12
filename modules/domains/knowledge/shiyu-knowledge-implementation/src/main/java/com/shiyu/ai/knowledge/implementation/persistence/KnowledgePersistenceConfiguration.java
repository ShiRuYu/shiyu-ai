package com.shiyu.ai.knowledge.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;


/**
 * KnowledgePersistenceConfiguration 配置组件，负责注册和配置知识领域相关基础设施。
 */
@Configuration
@MapperScan("com.shiyu.ai.knowledge.implementation.persistence.mapper")
public class KnowledgePersistenceConfiguration {}
