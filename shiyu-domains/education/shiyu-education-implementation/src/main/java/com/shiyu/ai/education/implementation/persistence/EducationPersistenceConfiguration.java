package com.shiyu.ai.education.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers only the Education bounded context's persistence adapters. */
@Configuration
@MapperScan("com.shiyu.ai.education.implementation.persistence.mapper")
public class EducationPersistenceConfiguration {
}
