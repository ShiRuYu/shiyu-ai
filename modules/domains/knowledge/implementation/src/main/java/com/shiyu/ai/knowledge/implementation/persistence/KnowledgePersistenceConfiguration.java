package com.shiyu.ai.knowledge.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers persistence adapters owned by the Knowledge bounded context. */
@Configuration
@MapperScan("com.shiyu.ai.knowledge.implementation.persistence.mapper")
public class KnowledgePersistenceConfiguration {
}
