package com.shiyu.ai.record.implementation.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** MyBatis wiring owned by the Record bounded context. */
@Configuration
@MapperScan("com.shiyu.ai.record.implementation.persistence.mapper")
public class RecordPersistenceConfiguration {
}
