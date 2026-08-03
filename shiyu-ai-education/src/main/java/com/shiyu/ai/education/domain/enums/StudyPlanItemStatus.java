package com.shiyu.ai.education.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学习计划项状态枚举（对应 DB edu_study_plan_item.status）
 */
@Getter
@AllArgsConstructor
public enum StudyPlanItemStatus implements IntEnum {

    PENDING(0, "待处理"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    SKIPPED(3, "已跳过");

    private final Integer code;
    private final String desc;

    public static StudyPlanItemStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
