package com.shiyu.ai.agent.implementation.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** Agent 执行状态枚举（对应 DB agent_execution.status） */
@Getter
@AllArgsConstructor
public enum AgentExecutionStatus implements IntEnum {
    RUNNING(0, "运行中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    PAUSED(3, "已暂停"),
    CANCELLED(4, "已取消");

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
    public static AgentExecutionStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
