package com.shiyu.ai.education.implementation.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 受限的学生辅导结果，包含生成时使用的数据来源和记录数量。 */
public record StudentTutorResponse(
        String content,
        Long knowledgeId,
        String knowledgeName,
        String period,
        int recordCount,
        Double averageScore,
        boolean insufficientData,
        List<SourceRecord> sources) {
    /** 纳入报告统计的一条学习记录。 */
    public record SourceRecord(Long recordId, String recordType, Long questionId,
            Double score, LocalDateTime occurredAt) {}
}
