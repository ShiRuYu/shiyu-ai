package com.shiyu.ai.education.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 复习任务状态枚举（对应 DB edu_review_task.status）
 */
@Getter
@AllArgsConstructor
public enum ReviewTaskStatus implements IntEnum {

    PENDING(0, "待复习"),
    IN_REVIEW(1, "复习中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "未通过"),
    OVERDUE(4, "已过期");

    private final Integer code;
    private final String desc;

    public static ReviewTaskStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
