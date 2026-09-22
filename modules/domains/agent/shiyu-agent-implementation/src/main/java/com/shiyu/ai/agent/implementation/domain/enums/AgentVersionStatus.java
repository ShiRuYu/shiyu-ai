package com.shiyu.ai.agent.implementation.domain.enums;

import com.shiyu.ai.common.foundation.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 定义 智能体 Version 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum AgentVersionStatus implements IntEnum {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final Integer code;
    /**
     * desc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String desc;

    /**
     * 执行 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    public static AgentVersionStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
