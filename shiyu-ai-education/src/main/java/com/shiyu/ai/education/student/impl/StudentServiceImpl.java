package com.shiyu.ai.education.student.impl;

import com.shiyu.ai.dal.dataobject.education.StudentDO;
import com.shiyu.ai.education.repository.StudentRepository;
import com.shiyu.ai.education.student.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public void update(StudentDO student) {
        studentRepository.update(student);
    }
}
