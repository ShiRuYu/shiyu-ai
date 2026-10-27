package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

/**
 * 负责 Idempotency 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface IdempotencyRepository {
    /**
     * 查询 Idempotency 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param operation 用于完成本次业务处理的 operation 参数。
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<String> find(TenantId tenantId, long ownerUserId, String operation, String key);

    /**
     * 查询指定租户、用户、操作和幂等键对应的请求指纹。
     *
     * @param tenantId 记录所属租户标识
     * @param ownerUserId 生成或幂等记录所属用户标识
     * @param operation 幂等记录的操作名称
     * @param key 调用方提供的幂等键
     *
     * @return 已保存的指纹；未保存指纹时为空
     */
    default Optional<String> findFingerprint(
            TenantId tenantId, long ownerUserId, String operation, String key) {
        return Optional.empty();
    }

    /**
     * 尝试为租户、用户和操作类型下的幂等键建立资源关联，并保存请求指纹。
     *
     * @param tenantId 幂等记录所属租户
     * @param ownerUserId 发起请求的用户标识
     * @param operation 区分请求用途的操作名称
     * @param key 调用方提供的幂等键
     * @param fingerprint 用于判断重试请求内容是否一致的指纹，可为空
     * @param resourceId 首次请求创建或预留的资源标识
     * @return 成功创建关联时为 true，幂等键已被占用时为 false
     */
    boolean claim(
            TenantId tenantId,
            long ownerUserId,
            String operation,
            String key,
            String fingerprint,
            String resourceId);

    /**
     * 为幂等键预留资源关联，不附带请求内容指纹。
     *
     * @param tenantId 记录所属租户标识
     * @param ownerUserId 生成或幂等记录所属用户标识
     * @param operation 幂等记录的操作名称
     * @param key 调用方提供的幂等键
     * @param resourceId 占用记录应匹配的资源标识
     *
     * @return 成功创建关联时为 true；该键已被占用时为 false
     */
    default boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId) {
        return claim(tenantId, ownerUserId, operation, key, null, resourceId);
    }

    /**
     * 仅在资源标识仍匹配时删除幂等键的占用记录。
     *
     * @param tenantId 记录所属租户标识
     * @param ownerUserId 生成或幂等记录所属用户标识
     * @param operation 幂等记录的操作名称
     * @param key 调用方提供的幂等键
     * @param resourceId 占用记录应匹配的资源标识
     *
     * @return 删除的记录数
     */
    int deleteClaim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId);

    /**
     * 以字段长度和 UTF-8 内容计算请求指纹，区分空值、空字符串及字段边界。
     *
     * @param values 参与指纹计算的有序字段，允许字段值为空
     *
     * @return SHA-256 摘要的十六进制字符串
     */
    static String fingerprint(String... values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String value : values) {
                byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
                int length = value == null ? -1 : bytes.length;
                digest.update((byte) (length >>> 24));
                digest.update((byte) (length >>> 16));
                digest.update((byte) (length >>> 8));
                digest.update((byte) length);
                digest.update(bytes);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
