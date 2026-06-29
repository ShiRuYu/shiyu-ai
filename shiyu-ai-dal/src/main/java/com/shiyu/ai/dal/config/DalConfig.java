package com.shiyu.ai.dal.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.shiyu.ai.dal.mapper")
public class DalConfig {
}
