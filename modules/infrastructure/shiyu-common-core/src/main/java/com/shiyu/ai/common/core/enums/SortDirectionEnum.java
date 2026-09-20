package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 定义 Sort Direction Enum 可用的枚举值及其业务语义。
 */
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
     * 执行 Sort Direction Enum 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Sort Direction Enum 相关操作生成的结果数据。
     */
    public static SortDirectionEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(direction -> direction.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(SortDirectionEnum.DESC); // 默认为 DESC
    }
}
