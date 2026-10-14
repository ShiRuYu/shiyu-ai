package com.shiyu.ai.education.implementation.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/** A bounded student tutor result with the source and record count used to generate it. */
public record StudentTutorResponse(
        String content,
        Long knowledgeId,
        String knowledgeName,
        String period,
        int recordCount,
        Double averageScore,
        boolean insufficientData,
        List<SourceRecord> sources) {
    /** One learning record included in the report's statistics. */
    public record SourceRecord(Long recordId, String recordType, Long questionId,
            Double score, LocalDateTime occurredAt) {}
}
