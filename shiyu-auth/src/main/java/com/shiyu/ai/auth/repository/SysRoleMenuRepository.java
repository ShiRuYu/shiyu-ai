package com.shiyu.ai.auth.repository;

import com.shiyu.ai.auth.mapper.SysRoleMenuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 角色与菜单关联表数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysRoleMenuRepository {

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

}
