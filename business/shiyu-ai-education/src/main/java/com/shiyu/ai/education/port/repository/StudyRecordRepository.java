package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import java.util.List;

public interface StudyRecordRepository {
    List<StudyRecordBO> selectByStudent(Long studentId);
    List<StudyRecordBO> selectByStudentAndKnowledge(Long studentId, Long knowledgeId);
    int insert(StudyRecordBO record);
}
