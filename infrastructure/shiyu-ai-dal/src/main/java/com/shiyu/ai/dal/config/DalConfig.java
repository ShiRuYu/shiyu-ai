package com.shiyu.ai.dal.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
    "com.shiyu.ai.dal.agent.mapper",
    "com.shiyu.ai.dal.auth.mapper",
    "com.shiyu.ai.dal.common.mapper",
    "com.shiyu.ai.dal.education.mapper",
    "com.shiyu.ai.dal.knowledge.mapper",
    "com.shiyu.ai.dal.memory.mapper",
    "com.shiyu.ai.dal.model.mapper",
    "com.shiyu.ai.dal.record.mapper",
    "com.shiyu.ai.dal.usage.mapper"
})
public class DalConfig {
}
