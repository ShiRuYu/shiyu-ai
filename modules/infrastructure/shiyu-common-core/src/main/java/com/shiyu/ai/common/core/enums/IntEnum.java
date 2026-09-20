package com.shiyu.ai.common.core.enums;

/**
 * 定义 Int Enum 相关的协作契约和调用边界。
 */
public interface IntEnum {

    /** 获取枚举整数值（对应数据库存储的值） */
    Integer getCode();

    /** 获取枚举描述（用于前端展示） */
    String getDesc();
}
