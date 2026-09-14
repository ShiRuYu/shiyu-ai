package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code GraphValidationVO} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphValidationVO {

    /**
     * valid 属性，保存当前对象中的业务数据或协作依赖。
     */
    private boolean valid;

    /**
     * errors 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<String> errors;

    /**
     * warnings 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<String> warnings;

    /**
     * {@code success} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static GraphValidationVO success() {
        return GraphValidationVO.builder().valid(true).build();
    }

    /**
     * {@code success} 执行当前类型定义的业务操作。
     *
     * @param warnings 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static GraphValidationVO success(List<String> warnings) {
        return GraphValidationVO.builder()
                .valid(true)
                .errors(List.of())
                .warnings(warnings == null ? List.of() : warnings)
                .build();
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param errors 参数值，用于执行当前操作。
     * @param warnings 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static GraphValidationVO fail(List<String> errors, List<String> warnings) {
        return GraphValidationVO.builder().valid(false).errors(errors).warnings(warnings).build();
    }
}
