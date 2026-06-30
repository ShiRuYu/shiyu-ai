package com.shiyu.ai.education.analytics.impl;

import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;
import com.shiyu.ai.education.analytics.AnalyticsService;
import com.shiyu.ai.education.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final StudyRecordRepository studyRecordRepository;

    @Override
    public List<StudyRecordDO> listRecordsByStudent(Long studentId) {
        return studyRecordRepository.selectByStudentId(studentId);
    }

    @Override
    public List<StudyRecordDO> listRecordsByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return studyRecordRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyRecordDO createRecord(StudyRecordDO record) {
        studyRecordRepository.insert(record);
        return record;
    }
}
