package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import io.github.linpeilie.annotations.AutoMapper;
import java.time.LocalDateTime;

@AutoMapper(target = StudyRecordBO.class)
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

