package com.shiyu.ai.education.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学习计划状态枚举（对应 DB edu_study_plan.status）
 */
@Getter
@AllArgsConstructor
public enum StudyPlanStatus implements IntEnum {

    ACTIVE(0, "进行中"),
    COMPLETED(1, "已完成"),
    ABANDONED(2, "已放弃");

    private final Integer code;
    private final String desc;

    public static StudyPlanStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
