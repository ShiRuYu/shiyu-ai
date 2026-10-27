package com.shiyu.ai.iam.implementation.infrastructure.auth;

/** 原子撤销持久化令牌，并在成功时返回所属用户。 */
public interface AtomicTokenConsumer {
    /**
     * 原子移除仍有效的令牌及其会话，并返回所属用户。
     *
     * @param tokenValue 不含存储键前缀的令牌值
     *
     * @return 令牌所属用户标识；令牌不存在或已失效时为 null
     */
    Long consumeToken(String tokenValue);
}
