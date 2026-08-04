package com.shiyu.ai.common.core.utils;

import java.util.HashMap;
import java.util.Map;

public class FlattenUtil {

    /**
     * 将多层 JSON Map 扁平化为 key=value
     * 例如 app.name -> MyApp
     */
    public static Map<String, Object> flatten(Map<String, Object> map) {
        return flattenMap("", map);
    }

    private static Map<String, Object> flattenMap(String prefix, Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map<?, ?> nested) {
                // nested 是 Map<?, ?>，仍然不能保证 <String, Object> ，需要过滤
                result.putAll(flattenMap(fullKey, castToStringObjectMap(nested)));
            } else {
                result.put(fullKey, value);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToStringObjectMap(Map<?, ?> map) {
        // 如果 key 不是 String，抛异常更符合预期
        for (Object key : map.keySet()) {
            if (!(key instanceof String)) {
                throw new IllegalArgumentException("Key must be String: " + key);
            }
        }
        return (Map<String, Object>) map;
    }

}
