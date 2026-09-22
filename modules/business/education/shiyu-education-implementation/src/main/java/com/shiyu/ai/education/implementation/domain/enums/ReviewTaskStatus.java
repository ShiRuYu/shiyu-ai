package com.shiyu.ai.education.implementation.domain.enums;

import com.shiyu.ai.common.foundation.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 定义 复习 Task 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum ReviewTaskStatus implements IntEnum {
    PENDING(0, "待复习"),
    IN_REVIEW(1, "复习中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "未通过"),
    OVERDUE(4, "已过期");

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final Integer code;
    /**
     * desc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String desc;

    /**
     * 执行 复习 Task 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 复习 Task 相关操作生成的结果数据。
     */
    public static ReviewTaskStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
