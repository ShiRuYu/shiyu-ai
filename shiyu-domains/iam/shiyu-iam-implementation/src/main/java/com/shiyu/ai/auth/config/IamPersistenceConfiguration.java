package com.shiyu.ai.auth.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers only the IAM-owned MyBatis mappers. */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.auth.persistence.mapper")
public class IamPersistenceConfiguration {
}
