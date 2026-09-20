package com.shiyu.ai.iam.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 Timezone Option 操作向调用方返回的传输数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimezoneOptionVO {

    /** 显示标签，例如：Asia/Shanghai (GMT+8) */
    private String label;

    /** 时区标识符，例如：Asia/Shanghai */
    private String value;
}
