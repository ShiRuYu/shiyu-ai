package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;

/**
 * 负责 Sa Token 用户 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface SaTokenUserRepository {
    /**
     * 查询 Sa Token 用户 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 Sa Token 用户 相关操作生成的结果数据。
     */
    UserBO selectById(Long userId);

    /**
     * 更新或设置 Sa Token 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param user 当前操作涉及的用户标识。
     */
    void updateExtInfo(UserBO user);
}
