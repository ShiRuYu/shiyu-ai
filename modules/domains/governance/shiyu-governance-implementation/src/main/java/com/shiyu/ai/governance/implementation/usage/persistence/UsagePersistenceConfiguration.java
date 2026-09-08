package com.shiyu.ai.governance.implementation.usage.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers only Governance-owned persistence mappers. */
@Configuration
@MapperScan("com.shiyu.ai.governance.implementation.usage.persistence.mapper")
public class UsagePersistenceConfiguration {
}
