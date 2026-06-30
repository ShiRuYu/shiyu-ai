package com.shiyu.ai.education.course.impl;

import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.education.course.CourseService;
import com.shiyu.ai.education.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseDO getById(Long id) {
        return courseRepository.selectById(id);
    }

    @Override
    public List<CourseDO> listBySubjectCode(String subjectCode) {
        return courseRepository.selectBySubjectCode(subjectCode);
    }

    @Override
    public List<CourseDO> listByGrade(Integer grade) {
        return courseRepository.selectByGrade(grade);
    }

    @Override
    public List<CourseDO> listAll() {
        return courseRepository.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseDO create(CourseDO course) {
        courseRepository.insert(course);
        return course;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CourseDO course) {
        courseRepository.update(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}
