package com.shiyu.ai.auth.repository;

import com.shiyu.ai.auth.mapper.SysTenantMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 租户数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysTenantRepository {

    @Resource
    private SysTenantMapper sysTenantMapper;

}
