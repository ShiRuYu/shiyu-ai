package com.shiyu.ai.dal.repository.auth;

import com.shiyu.ai.dal.dataobject.auth.UserDO;
import com.shiyu.ai.dal.mapper.auth.UserMapper;
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
