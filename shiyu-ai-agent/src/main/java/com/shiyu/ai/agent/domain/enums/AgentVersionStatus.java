package com.shiyu.ai.agent.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Agent 版本状态枚举
 */
@Getter
@AllArgsConstructor
public enum AgentVersionStatus implements IntEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    private final Integer code;
    private final String desc;

    public static AgentVersionStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
