package com.shiyu.ai.common.core.enums;

/**
 * 通用整数枚举接口
 * <p>
 * 用于统一整型枚举的 code + desc 模式，
 * 配合 @AutoEnumMapper 实现枚举 ↔ 业务描述的自动映射。
 */
public interface IntEnum {

    /**
     * 获取枚举整数值（对应数据库存储的值）
     */
    Integer getCode();

    /**
     * 获取枚举描述（用于前端展示）
     */
    String getDesc();
}
