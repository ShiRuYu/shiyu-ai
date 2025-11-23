package com.shiyu.ai.auth.repository;

import com.shiyu.ai.auth.mapper.SysDeptMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 部门数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysDeptRepository {

    @Resource
    private SysDeptMapper sysDeptMapper;

}
