package com.shiyu.ai.dal.memory.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 情景记忆执行状态枚举（对应 DB episodic_memory.status）
 */
@Getter
@AllArgsConstructor
public enum EpisodicMemoryStatus implements IntEnum {

    RUNNING(0, "运行中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final Integer code;
    private final String desc;

    public static EpisodicMemoryStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
