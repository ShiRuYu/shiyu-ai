package com.shiyu.ai.dal.auth.repository;

import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SaTokenUserRepository {

    @Resource
    private UserMapper userMapper;

    public UserDO selectById(Long userId) {
        return userMapper.selectOneById(userId);
    }

    public void updateExtInfo(UserDO user) {
        userMapper.update(user);
    }
}
