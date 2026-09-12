package com.shiyu.ai.iam.implementation.persistence.repository;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * {@code SaTokenUserRepositoryImpl} 实现平台模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class SaTokenUserRepositoryImpl
        implements com.shiyu.ai.iam.implementation.port.repository.SaTokenUserRepository {

    /**
     * 用户映射器，表示当前对象中的对应属性。
     */
    @Resource private UserMapper userMapper;

    public UserBO selectById(Long userId) {
        return MapstructUtils.convert(userMapper.selectOneById(userId), UserBO.class);
    }

    public void updateExtInfo(UserBO user) {
        userMapper.update(MapstructUtils.convert(user, UserDO.class));
    }
}
