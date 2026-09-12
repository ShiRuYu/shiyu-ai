package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 排序方向枚举 */
@Getter
@AllArgsConstructor
public enum SortDirectionEnum {
    ASC("asc", "ASC"), // 升序
    DESC("desc", "DESC"); // 降序

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * sqlKeyword 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String sqlKeyword;

    /**
     * {@code fromCode} 执行当前类型定义的业务操作。
     *
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static SortDirectionEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(direction -> direction.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(SortDirectionEnum.DESC); // 默认为 DESC
    }
}
