package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.education.service.ExamService;
import com.shiyu.ai.dal.repository.education.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;

    @Override
    public PageData<ExamDO> page(int pageNum, int pageSize) {
        return examRepository.selectPage(pageNum, pageSize);
    }

    public List<ExamDO> listAll() {
        return examRepository.selectAll();
    }

    @Override
    public ExamDO getById(Long id) {
        return examRepository.selectById(id);
    }

    @Override
    public List<ExamDO> listBySubjectCode(String subjectCode) {
        return examRepository.selectBySubjectCode(subjectCode);
    }

    @Override
    public List<ExamDO> listByTeacherId(Long teacherId) {
        return examRepository.selectByTeacherId(teacherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamDO create(ExamDO exam) {
        examRepository.insert(exam);
        return exam;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ExamDO exam) {
        examRepository.update(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        examRepository.deleteById(id);
    }
}
