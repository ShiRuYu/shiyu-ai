package com.shiyu.ai.iam.implementation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 Iam Persistence 基础设施或应用能力的配置项及装配规则。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.iam.implementation.persistence.mapper")
public class IamPersistenceConfiguration {}
