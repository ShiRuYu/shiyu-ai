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
            if (value instanceof Map) {
                result.putAll(flattenMap(fullKey, (Map<String, Object>) value));
            } else {
                result.put(fullKey, value);
            }
        });
        return result;
    }
}
