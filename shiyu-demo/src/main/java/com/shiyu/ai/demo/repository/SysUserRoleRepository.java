package com.shiyu.ai.demo.repository;

import com.shiyu.ai.demo.mapper.SysUserRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 用户与角色关联表数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysUserRoleRepository {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

}
