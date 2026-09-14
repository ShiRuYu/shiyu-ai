package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;

/**
 * SaTokenUserRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface SaTokenUserRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param userId 用户标识。
     *
     * @return 操作结果。
     */
    UserBO selectById(Long userId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param user 方法参数。
     */
    void updateExtInfo(UserBO user);
}
