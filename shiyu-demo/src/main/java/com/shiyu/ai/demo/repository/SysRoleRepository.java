package com.shiyu.ai.demo.repository;

import com.shiyu.ai.demo.mapper.SysRoleMapper;
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
