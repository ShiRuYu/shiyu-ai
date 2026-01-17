package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;

/**
 * 排序方向枚举
 */
@Getter
@AllArgsConstructor
public enum SortDirectionEnum {
    ASC("asc", "ASC"),      // 升序
    DESC("desc", "DESC");   // 降序

    private final String code;
    private final String sqlKeyword;

    public static SortDirectionEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(direction -> direction.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(SortDirectionEnum.DESC); // 默认为 DESC
    }
}