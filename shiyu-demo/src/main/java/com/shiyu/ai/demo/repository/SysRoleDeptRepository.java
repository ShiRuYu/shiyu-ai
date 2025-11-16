package com.shiyu.ai.demo.repository;

import com.shiyu.ai.demo.mapper.SysRoleDeptMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 角色与部门关联表数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysRoleDeptRepository {

    @Resource
    private SysRoleDeptMapper sysRoleDeptMapper;

}
