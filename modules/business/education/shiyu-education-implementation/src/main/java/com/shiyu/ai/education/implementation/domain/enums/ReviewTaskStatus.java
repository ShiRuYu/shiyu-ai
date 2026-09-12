package com.shiyu.ai.education.implementation.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 复习任务状态枚举（对应 DB edu_review_task.status） */
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
     * {@code fromCode} 执行当前类型定义的业务操作。
     *
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static ReviewTaskStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
