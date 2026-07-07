package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.dataobject.education.SubjectDO;
import com.shiyu.ai.dal.repository.education.SubjectRepository;
import com.shiyu.ai.education.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectDO getById(Long id) {
        return subjectRepository.selectById(id);
    }

    @Override
    public SubjectDO getByCode(String code) {
        return subjectRepository.selectByCode(code);
    }

    @Override
    public PageData<SubjectDO> page(int pageNum, int pageSize) {
        return subjectRepository.selectPage(pageNum, pageSize);
    }

    public List<SubjectDO> listAll() {
        return subjectRepository.selectAll();
    }

    @Override
    public List<SubjectDO> listByGradeLevel(String gradeLevel) {
        return subjectRepository.selectByGradeLevel(gradeLevel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubjectDO create(SubjectDO subject) {
        subjectRepository.insert(subject);
        return subject;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SubjectDO subject) {
        subjectRepository.update(subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        subjectRepository.deleteById(id);
    }
}
