package com.shiyu.ai.common.core.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code IdNameOptionVO} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdNameOptionVO {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /** 平台编码 */
    private String code;

    /** 业务值（模型名称等） */
    private String value;
}
