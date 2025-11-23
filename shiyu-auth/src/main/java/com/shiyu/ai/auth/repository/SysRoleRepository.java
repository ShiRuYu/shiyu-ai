package com.shiyu.ai.auth.repository;

import com.shiyu.ai.auth.mapper.SysRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 角色数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysRoleRepository {

    @Resource
    private SysRoleMapper sysRoleMapper;

}
