package com.shiyu.ai.common.storage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

final class StorageKeys {

    private StorageKeys() {
    }

    static String create(String namespace, String originalName) {
        String safeName = normalizeName(originalName);
        String encodedName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(safeName.getBytes(StandardCharsets.UTF_8));
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        return "%s%d/%02d/%02d/%s~%s".formatted(
                namespace,
                date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                UUID.randomUUID(), encodedName);
    }

    static String originalName(String key) {
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
