package com.shiyu.ai.common.core.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdNameOptionVO {

    private Long id;

    private String name;

    /**
     * 平台编码
     */
    private String code;

    /**
     * 业务值（模型名称等）
     */
    private String value;
}
