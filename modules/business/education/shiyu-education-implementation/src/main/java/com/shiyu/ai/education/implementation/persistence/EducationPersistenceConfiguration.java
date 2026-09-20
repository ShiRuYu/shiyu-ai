package com.shiyu.ai.education.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 教育 Persistence 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
@MapperScan("com.shiyu.ai.education.implementation.persistence.mapper")
public class EducationPersistenceConfiguration {}
