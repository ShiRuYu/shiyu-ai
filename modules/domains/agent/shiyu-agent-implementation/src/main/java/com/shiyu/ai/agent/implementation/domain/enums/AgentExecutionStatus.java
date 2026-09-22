package com.shiyu.ai.agent.implementation.domain.enums;

import com.shiyu.ai.common.foundation.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 定义 智能体 Execution 可用的枚举值及其业务语义。
 */
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
     * 执行 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 智能体 Execution 相关操作生成的结果数据。
     */
    public static AgentExecutionStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
