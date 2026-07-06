package com.shiyu.ai.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 时区枚举
 */
@Getter
@AllArgsConstructor
public enum TimezoneEnum {
    
    /**
     * 纽约时区 (GMT-5)
     */
    AMERICA_NEW_YORK("America/New_York", "America/New_York (GMT-5)", -5),
    
    /**
     * 伦敦时区 (GMT0)
     */
    EUROPE_LONDON("Europe/London", "Europe/London (GMT0)", 0),
    
    /**
     * 上海时区 (GMT+8)
     */
    ASIA_SHANGHAI("Asia/Shanghai", "Asia/Shanghai (GMT+8)", 8),
    
    /**
     * 东京时区 (GMT+9)
     */
    ASIA_TOKYO("Asia/Tokyo", "Asia/Tokyo (GMT+9)", 9),
    
    /**
     * 首尔时区 (GMT+9)
     */
    ASIA_SEOUL("Asia/Seoul", "Asia/Seoul (GMT+9)", 9);
    
    /**
     * 时区标识符
     */
    private final String value;
    
    /**
     * 显示标签
     */
    private final String label;
    
    /**
     * 时区偏移量（相对于UTC）
     */
    private final Integer offset;
    
    /**
     * 根据value获取时区枚举
     * @param value 时区标识符
     * @return 时区枚举
     */
    public static TimezoneEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (TimezoneEnum timezone : values()) {
            if (timezone.getValue().equals(value)) {
                return timezone;
            }
        }
        return null;
    }
    
    /**
     * 判断时区是否有效
     * @param value 时区标识符
     * @return 是否有效
     */
    public static boolean isValid(String value) {
        return getByValue(value) != null;
    }
}
