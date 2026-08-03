package com.shiyu.ai.agent.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 节点执行状态枚举（对应 DB node_execution.status）
 */
@Getter
@AllArgsConstructor
public enum NodeExecutionStatus implements IntEnum {

    PENDING(0, "待处理"),
    RUNNING(1, "运行中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败");

    private final Integer code;
    private final String desc;

    public static NodeExecutionStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
