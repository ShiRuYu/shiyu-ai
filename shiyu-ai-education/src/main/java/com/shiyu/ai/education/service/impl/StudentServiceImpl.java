package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.dataobject.education.StudentDO;
import com.shiyu.ai.dal.repository.education.StudentRepository;
import com.shiyu.ai.education.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public StudentDO getById(Long id) {
        return studentRepository.selectById(id);
    }

    @Override
    public StudentDO getByUserId(Long userId) {
        return studentRepository.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentDO create(StudentDO student) {
        studentRepository.insert(student);
        return student;
    }

    @Override
    public PageData<StudentDO> page(int pageNum, int pageSize) {
        return studentRepository.selectPage(pageNum, pageSize);
    }

    public List<StudentDO> listAll() {
        return studentRepository.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StudentDO student) {
        studentRepository.update(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
}
