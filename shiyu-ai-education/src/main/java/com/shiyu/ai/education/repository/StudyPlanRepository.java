package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;
import com.shiyu.ai.dal.mapper.education.StudyPlanMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyPlanRepository {

    @Resource
    private StudyPlanMapper studyPlanMapper;

    public StudyPlanDO selectById(Long id) {
        return studyPlanMapper.selectOneById(id);
    }

    public List<StudyPlanDO> selectByStudentId(Long studentId) {
        return studyPlanMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId).orderBy("create_time", false));
    }

    public List<StudyPlanDO> selectActiveByStudent(Long studentId) {
        return studyPlanMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId).eq("status", "ACTIVE"));
    }

    public int insert(StudyPlanDO plan) {
        return studyPlanMapper.insert(plan);
    }

    public int update(StudyPlanDO plan) {
        return studyPlanMapper.update(plan);
    }

    public int deleteById(Long id) {
        return studyPlanMapper.deleteById(id);
    }
}
