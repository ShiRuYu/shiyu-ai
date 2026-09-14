package com.shiyu.ai.education.implementation.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 学习计划状态枚举（对应 DB edu_study_plan.status） */
@Getter
@AllArgsConstructor
public enum StudyPlanStatus implements IntEnum {
    ACTIVE(0, "进行中"),
    COMPLETED(1, "已完成"),
    ABANDONED(2, "已放弃");

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
    public static StudyPlanStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
