package com.shiyu.ai.knowledge.implementation.application.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 索引重建任务状态 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RebuildStatus {
    /**
     * 任务标识，表示当前对象中的对应属性。
     */
    private String taskId;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String status; // PENDING, RUNNING, COMPLETED, FAILED
    /**
     * progress 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int progress; // 0-100
    /**
     * total 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int total;
    /**
     * indexed 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int indexed;
    /**
     * 错误，表示当前对象中的对应属性。
     */
    private String error;
    /**
     * startTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime startTime;
    /**
     * endTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime endTime;
    /**
     * retryCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int retryCount;

    /**
     * {@code RebuildStatus} 创建并初始化当前类型实例。
     *
     * @param taskId 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     */
    public RebuildStatus(String taskId, String status) {
        this.taskId = taskId;
        this.status = status;
        this.progress = 0;
        this.total = 0;
        this.indexed = 0;
        this.retryCount = 0;
    }
}
