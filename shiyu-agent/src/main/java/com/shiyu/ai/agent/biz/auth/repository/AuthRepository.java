package com.shiyu.ai.agent.biz.auth.repository;

import com.shiyu.ai.agent.dal.mapper.auth.AuthCodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证数据仓储层
 */
@Component
public class AuthRepository {

    @Resource
    private AuthCodeMapper authCodeMapper;

    /**
     * 根据用户名查询权限码列表
     */
    public List<String> selectCodesByUsername(String username) {
        return authCodeMapper.selectCodesByUsername(username);
    }
}
