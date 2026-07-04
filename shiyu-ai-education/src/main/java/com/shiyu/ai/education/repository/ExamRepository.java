package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.dal.mapper.education.ExamMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamRepository {

    @Resource
    private ExamMapper examMapper;

    public List<ExamDO> selectAll() {
        return examMapper.selectListByQuery(
                QueryWrapper.create().orderBy("created_at", false));
    }

    public ExamDO selectById(Long id) {
        return examMapper.selectOneById(id);
    }

    public List<ExamDO> selectBySubjectCode(String subjectCode) {
        return examMapper.selectListByQuery(
                QueryWrapper.create().eq("subject_code", subjectCode).orderBy("created_at", false));
    }

    public List<ExamDO> selectByTeacherId(Long teacherId) {
        return examMapper.selectListByQuery(
                QueryWrapper.create().eq("teacher_id", teacherId).orderBy("created_at", false));
    }

    public int insert(ExamDO exam) {
        return examMapper.insert(exam);
    }

    public int update(ExamDO exam) {
        return examMapper.update(exam);
    }

    public int deleteById(Long id) {
        return examMapper.deleteById(id);
    }
}
