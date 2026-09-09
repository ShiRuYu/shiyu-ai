package com.shiyu.ai.iam.implementation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers only the IAM-owned MyBatis mappers. */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.iam.implementation.persistence.mapper")
public class IamPersistenceConfiguration {
}

