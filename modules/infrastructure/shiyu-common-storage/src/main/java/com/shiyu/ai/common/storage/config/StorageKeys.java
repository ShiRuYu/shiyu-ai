package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

/**
 * {@code StorageKeys} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class StorageKeys {

    private StorageKeys() {}

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param namespace 参数值，用于执行当前操作。
     * @param originalName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String create(String namespace, String originalName) {
        String safeName = normalizeName(originalName);
        String encodedName =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(safeName.getBytes(StandardCharsets.UTF_8));
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        return "%s%d/%02d/%02d/%s~%s"
                .formatted(
                        namespace,
                        date.getYear(),
                        date.getMonthValue(),
                        date.getDayOfMonth(),
                        UUID.randomUUID(),
                        encodedName);
    }

    /**
     * {@code originalName} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String originalName(String key) {
        int separator = key.lastIndexOf('~');
        if (separator < 0 || separator == key.length() - 1) {
            return key.substring(key.lastIndexOf('/') + 1);
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(key.substring(separator + 1));
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return key.substring(key.lastIndexOf('/') + 1);
        }
    }

    private static String normalizeName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "unnamed";
        }
        String normalized = originalName.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        return normalized.isEmpty() ? "unnamed" : normalized;
    }
}
