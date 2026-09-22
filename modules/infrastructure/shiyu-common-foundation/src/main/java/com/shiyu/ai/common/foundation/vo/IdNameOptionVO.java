package com.shiyu.ai.common.foundation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 Id Name Option 操作向调用方返回的传输数据。
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
