package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装 Graph Validation 操作向调用方返回的传输数据。
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
     * 执行 Graph Validation 相关业务数据，并返回处理结果。
     *
     * @return 返回 Graph Validation 相关操作生成的结果数据。
     */
    public static GraphValidationVO success() {
        return GraphValidationVO.builder().valid(true).build();
    }

    /**
     * 执行 Graph Validation 相关业务数据，并返回处理结果。
     *
     * @param warnings 用于完成本次业务处理的 warnings 参数。
     * @return 返回 Graph Validation 相关操作生成的结果数据。
     */
    public static GraphValidationVO success(List<String> warnings) {
        return GraphValidationVO.builder()
                .valid(true)
                .errors(List.of())
                .warnings(warnings == null ? List.of() : warnings)
                .build();
    }

    /**
     * 执行 Graph Validation 相关业务数据，并返回处理结果。
     *
     * @param errors 用于完成本次业务处理的 errors 参数。
     * @param warnings 用于完成本次业务处理的 warnings 参数。
     * @return 返回 Graph Validation 相关操作生成的结果数据。
     */
    public static GraphValidationVO fail(List<String> errors, List<String> warnings) {
        return GraphValidationVO.builder().valid(false).errors(errors).warnings(warnings).build();
    }
}
