package com.shiyu.ai.agent.implementation.checkpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * 校验图状态能否安全地经 JSON 往返，并保存 Long 标量的明确类型信息。
 * 任意 Java 对象和引用环不参与自动恢复。
 */
final class CheckpointStateCodec {
    private CheckpointStateCodec() {}

    /**
     * 为可安全往返的图状态收集 Long 值路径。
     *
     * @param state 要持久化的公开图状态
     * @return JSON Pointer 路径到标量类型的映射
     * @throws IllegalArgumentException 状态含无法无损恢复的值时
     */
    static Map<String, String> typeHints(Map<String, Object> state) {
        Map<String, String> hints = new HashMap<>();
        inspect(state, "", hints, new IdentityHashMap<>());
        return hints;
    }

    /**
     * 按持久化类型信息恢复 Long 标量，同时拒绝损坏的路径或类型。
     *
     * @param state JSON 解析后的图状态
     * @param hints 服务端写入的 Long 类型路径
     * @return 类型修复后的图状态
     */
    static Map<String, Object> restore(Map<String, Object> state, Map<String, String> hints) {
        Set<String> used = new HashSet<>();
        Object restored = restoreValue(state, "", hints, used);
        if (used.size() != hints.size()) {
            throw new IllegalStateException("图检查点类型路径无效");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) restored;
        return result;
    }

    private static void inspect(Object value, String path, Map<String, String> hints,
            IdentityHashMap<Object, Boolean> seen) {
        if (value == null || value instanceof String || value instanceof Boolean
                || value instanceof Integer) return;
        if (value instanceof Long) {
            hints.put(path, "Long");
            return;
        }
        if (value instanceof Double number && Double.isFinite(number)) return;
        if (seen.put(value, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("图状态含重复对象引用或循环引用");
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("图状态含非字符串键");
                }
                inspect(entry.getValue(), path + "/" + escape(key), hints, seen);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                inspect(list.get(i), path + "/" + i, hints, seen);
            }
            return;
        }
        throw new IllegalArgumentException("图状态含无法无损恢复的类型: "
                + value.getClass().getSimpleName());
    }

    private static Object restoreValue(Object value, String path,
            Map<String, String> hints, Set<String> used) {
        if (hints.containsKey(path)) {
            if (!"Long".equals(hints.get(path))) {
                throw new IllegalStateException("图检查点标量类型无效");
            }
            used.add(path);
            if (value instanceof Integer || value instanceof Long) {
                return ((Number) value).longValue();
            }
            if (value instanceof String number) {
                try {
                    long parsed = Long.parseLong(number);
                    if (Long.toString(parsed).equals(number)) return parsed;
                } catch (NumberFormatException invalid) {
                    throw new IllegalStateException("图检查点 Long 值无效", invalid);
                }
            }
            throw new IllegalStateException("图检查点 Long 值无效");
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> restored = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                if (!(key instanceof String name)) {
                    throw new IllegalStateException("图检查点状态键无效");
                }
                restored.put(name, restoreValue(item, path + "/" + escape(name), hints, used));
            });
            return restored;
        }
        if (value instanceof List<?> list) {
            List<Object> restored = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                restored.add(restoreValue(list.get(i), path + "/" + i, hints, used));
            }
            return restored;
        }
        return value;
    }

    private static String escape(String value) {
        return value.replace("~", "~0").replace("/", "~1");
    }
}
