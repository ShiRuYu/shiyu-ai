package com.shiyu.ai.agent.implementation.domain.enums;

import com.shiyu.ai.common.core.enums.IntEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** Agent 版本状态枚举 */
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
     * {@code fromCode} 执行当前类型定义的业务操作。
     *
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static AgentVersionStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
