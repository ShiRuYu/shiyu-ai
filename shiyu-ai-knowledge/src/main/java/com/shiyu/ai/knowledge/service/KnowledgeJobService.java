package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;

import java.time.LocalDateTime;

public interface KnowledgeJobService {

    PageData<JobView> page(int pageNum, int pageSize, Long spaceId, String status);

    JobView get(Long id);

    void cancel(Long id);

    void retry(Long id);

    record JobView(Long id, String jobKey, String jobType, Long spaceId,
                   Long documentId, Long versionId, String status, String stage,
                   Integer progress, Integer attempts, Integer maxAttempts,
                   String errorMessage, LocalDateTime heartbeatTime,
                   LocalDateTime startedTime, LocalDateTime finishedTime,
                   LocalDateTime createTime) {
    }
}
