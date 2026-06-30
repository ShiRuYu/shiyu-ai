package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.StudentDO;
import com.shiyu.ai.dal.mapper.education.StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudentRepository {

    @Resource
    private StudentMapper studentMapper;

    public StudentDO selectById(Long id) {
        return studentMapper.selectOneById(id);
    }

    public StudentDO selectByUserId(Long userId) {
        return studentMapper.selectOneByQuery(QueryWrapper.create().eq("user_id", userId));
    }

    public int insert(StudentDO student) {
        return studentMapper.insert(student);
    }

    public int update(StudentDO student) {
        return studentMapper.update(student);
    }
}
