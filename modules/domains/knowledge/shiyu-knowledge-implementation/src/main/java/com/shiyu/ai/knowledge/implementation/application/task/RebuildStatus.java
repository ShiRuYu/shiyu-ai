package com.shiyu.ai.knowledge.implementation.application.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表示 Rebuild 相关流程中的状态、关系或执行数据。
 */
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
     * 执行 Rebuild 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param taskId 用于定位task的标识。
     * @param status 用于完成本次业务处理的 status 参数。
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
