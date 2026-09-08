package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.auth.persistence.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SaTokenUserRepositoryImpl implements com.shiyu.ai.auth.port.repository.SaTokenUserRepository {

    @Resource
    private UserMapper userMapper;

    public UserBO selectById(Long userId) {
        return MapstructUtils.convert(userMapper.selectOneById(userId), UserBO.class);
    }

    public void updateExtInfo(UserBO user) {
        userMapper.update(MapstructUtils.convert(user, UserDO.class));
    }
}

