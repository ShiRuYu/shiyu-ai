package com.shiyu.ai.iam.implementation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * IamPersistenceConfiguration 配置组件，负责注册和配置身份与访问领域相关基础设施。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.shiyu.ai.iam.implementation.persistence.mapper")
public class IamPersistenceConfiguration {}
