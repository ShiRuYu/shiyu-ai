package com.shiyu.ai.auth.repository;

import com.shiyu.ai.auth.mapper.SysUserPostMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 用户与岗位关联表数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysUserPostRepository {

    @Resource
    private SysUserPostMapper sysUserPostMapper;

}
