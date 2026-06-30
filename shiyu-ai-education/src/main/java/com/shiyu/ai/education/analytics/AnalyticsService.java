package com.shiyu.ai.education.analytics;

import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;

import java.util.List;

public interface AnalyticsService {

    List<StudyRecordDO> listRecordsByStudent(Long studentId);

    List<StudyRecordDO> listRecordsByStudentAndKnowledge(Long studentId, Long knowledgeId);

    StudyRecordDO createRecord(StudyRecordDO record);
}
