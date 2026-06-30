package com.shiyu.ai.education.plan.impl;

import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;
import com.shiyu.ai.education.plan.StudyPlanService;
import com.shiyu.ai.education.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;

    @Override
    public StudyPlanDO getById(Long id) {
        return studyPlanRepository.selectById(id);
    }

    @Override
    public List<StudyPlanDO> listByStudentId(Long studentId) {
        return studyPlanRepository.selectByStudentId(studentId);
    }

    @Override
    public List<StudyPlanDO> listActiveByStudent(Long studentId) {
        return studyPlanRepository.selectActiveByStudent(studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanDO create(StudyPlanDO plan) {
        studyPlanRepository.insert(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StudyPlanDO plan) {
        studyPlanRepository.update(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        studyPlanRepository.deleteById(id);
    }
}
