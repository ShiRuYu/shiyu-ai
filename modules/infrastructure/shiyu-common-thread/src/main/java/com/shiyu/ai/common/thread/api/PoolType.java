
package com.shiyu.ai.common.thread.api;

/**
 * 线程池类型枚举
 * 定义不同类型的线程池，适用于不同的业务场景
 */
public enum PoolType {

    /**
     * 默认线程池，适用于一般业务场景
     */
    DEFAULT("default", "默认线程池"),

    /**
     * CPU密集型任务线程池，线程数与CPU核心数相同
     */
    CPU_INTENSIVE("cpuIntensive", "CPU密集型任务线程池"),

    /**
     * IO密集型任务线程池，线程数通常是CPU核心数的2倍
     */
    IO_INTENSIVE("ioIntensive", "IO密集型任务线程池"),

    /**
     * 调度任务线程池，用于定时任务和延时任务
     */
    SCHEDULED("scheduled", "调度任务线程池"),

    /**
     * 虚拟线程池，使用Java 21+的虚拟线程技术
     */
    VIRTUAL("virtual", "虚拟线程池"),

    /**
     * 优先级任务线程池，支持任务优先级
     */
    PRIORITY("priority", "优先级任务线程池"),

    /**
     * 自定义线程池，由用户自定义配置
     */
    CUSTOM("custom", "自定义线程池");

    private final String code;
    private final String description;

    PoolType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取线程池类型
     * 
     * @param code 线程池类型代码
     * @return 线程池类型
     */
    public static PoolType fromCode(String code) {
        for (PoolType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return DEFAULT;
    }
}
