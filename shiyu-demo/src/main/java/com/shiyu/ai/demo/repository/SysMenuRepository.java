package com.shiyu.ai.demo.repository;

import com.shiyu.ai.demo.mapper.SysMenuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 菜单数据仓储层
 *
 * @author shiyu-ai
 */
@Component
public class SysMenuRepository {

    @Resource
    private SysMenuMapper sysMenuMapper;

}
