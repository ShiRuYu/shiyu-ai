package com.shiyu.ai.education.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * EducationPersistenceConfiguration 配置组件，负责注册和配置教育领域相关基础设施。
 */
@Configuration
@MapperScan("com.shiyu.ai.education.implementation.persistence.mapper")
public class EducationPersistenceConfiguration {}
