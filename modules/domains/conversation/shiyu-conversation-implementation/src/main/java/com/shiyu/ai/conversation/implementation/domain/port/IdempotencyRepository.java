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

    default Optional<String> findFingerprint(
            TenantId tenantId, long ownerUserId, String operation, String key) {
        return Optional.empty();
    }

    /**
     * 执行 Idempotency 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param operation 用于完成本次业务处理的 operation 参数。
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param resourceId 用于定位resource的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean claim(
            TenantId tenantId,
            long ownerUserId,
            String operation,
            String key,
            String fingerprint,
            String resourceId);

    default boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId) {
        return claim(tenantId, ownerUserId, operation, key, null, resourceId);
    }

    int deleteClaim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId);

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
