package com.shiyu.ai.education.dto;

import java.time.LocalDateTime;

public record StudyRecordResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String recordType,
        Long questionId,
        Double score,
        Double accuracy,
        Integer durationSec,
        LocalDateTime createTime
) {}
