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
     * 内存中的反向索引丢失后，查询已持久化随机令牌的所属用户。
     *
     * @param tokenValue 不含 Sa-Token 键前缀的不透明令牌值
     * @return 持有该令牌的用户标识；未找到时返回 null
     */
    Long findUserIdByToken(String tokenValue);

    /**
     * 仅在用户扩展信息仍等于预期旧值时写入新值，避免并发更新相互覆盖。
     *
     * @param user 携带用户标识及新扩展信息的对象
     * @param expectedExtInfo 更新前应匹配的扩展信息
     * @return 条件更新成功时为 true，旧值已变化或用户不存在时为 false
     */
    boolean updateExtInfo(UserBO user, String expectedExtInfo);

    /** 原子移除一个有效令牌及其会话，并返回所属用户。 */
    Long consumeToken(String tokenValue);

}
