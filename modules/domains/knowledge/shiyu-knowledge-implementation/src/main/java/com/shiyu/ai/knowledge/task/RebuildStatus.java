package com.shiyu.ai.knowledge.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 索引重建任务状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RebuildStatus {
    private String taskId;
    private String status;  // PENDING, RUNNING, COMPLETED, FAILED
    private int progress;   // 0-100
    private int total;
    private int indexed;
    private String error;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int retryCount;

    public RebuildStatus(String taskId, String status) {
        this.taskId = taskId;
        this.status = status;
        this.progress = 0;
        this.total = 0;
        this.indexed = 0;
        this.retryCount = 0;
    }
}
