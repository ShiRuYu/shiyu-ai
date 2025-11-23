package com.shiyu.ai.auth.repository;

import com.shiyu.ai.auth.mapper.SysPostMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 岗位信息数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysPostRepository {

    @Resource
    private SysPostMapper sysPostMapper;

}
