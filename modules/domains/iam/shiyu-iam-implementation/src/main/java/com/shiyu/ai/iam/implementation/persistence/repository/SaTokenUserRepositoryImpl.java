package com.shiyu.ai.iam.implementation.persistence.repository;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * 负责 Sa Token 用户 的持久化查询、保存和删除，并维护数据访问边界。
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
