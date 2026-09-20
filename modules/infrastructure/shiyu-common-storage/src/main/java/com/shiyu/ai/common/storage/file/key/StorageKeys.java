package com.shiyu.ai.common.storage.file.key;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
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
 * 实现 Storage Keys 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class StorageKeys {

    private StorageKeys() {}

    /**
     * 创建或保存 Storage Keys 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @return 返回 Storage Keys 相关操作生成的结果数据。
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
     * 执行 Storage Keys 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Storage Keys 相关操作生成的结果数据。
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
