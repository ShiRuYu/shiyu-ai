package com.shiyu.ai.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时区选项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimezoneOptionVO {
    
    /**
     * 显示标签，例如：Asia/Shanghai (GMT+8)
     */
    private String label;
    
    /**
     * 时区标识符，例如：Asia/Shanghai
     */
    private String value;
}
