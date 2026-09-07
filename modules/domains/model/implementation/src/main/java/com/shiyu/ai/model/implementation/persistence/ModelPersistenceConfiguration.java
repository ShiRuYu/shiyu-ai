package com.shiyu.ai.model.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** MyBatis wiring owned by the Model bounded context. */
@Configuration
@MapperScan("com.shiyu.ai.model.implementation.persistence.mapper")
public class ModelPersistenceConfiguration {
}
