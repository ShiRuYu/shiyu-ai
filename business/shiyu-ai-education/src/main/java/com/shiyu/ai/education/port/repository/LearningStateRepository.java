package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.LearningStateBO;
import java.util.List;

public interface LearningStateRepository {
    LearningStateBO selectByStudentAndKnowledge(Long studentId, Long knowledgeId);
    int upsert(LearningStateBO state);
}
