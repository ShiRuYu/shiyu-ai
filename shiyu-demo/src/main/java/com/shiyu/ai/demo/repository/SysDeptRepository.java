package com.shiyu.ai.demo.repository;

import com.shiyu.ai.demo.mapper.SysDeptMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

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
