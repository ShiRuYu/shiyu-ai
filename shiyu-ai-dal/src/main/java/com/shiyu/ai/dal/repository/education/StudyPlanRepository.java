package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.StudyPlanBO;
import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;
import com.shiyu.ai.dal.mapper.education.StudyPlanMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanRepository {

    @Resource
    private StudyPlanMapper studyPlanMapper;

    public StudyPlanBO selectById(Long id) {
        return MapstructUtils.convert(studyPlanMapper.selectOneById(id), StudyPlanBO.class);
    }

    public List<StudyPlanBO> selectByStudentId(Long studentId) {
        return MapstructUtils.convert(studyPlanMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .orderBy("create_time", false)
        ), StudyPlanBO.class);
    }

    public List<StudyPlanBO> selectActiveByStudent(Long studentId) {
        return MapstructUtils.convert(studyPlanMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("status", "ACTIVE")
        ), StudyPlanBO.class);
    }

    public int insert(StudyPlanBO entity) {
        StudyPlanDO dataObj = MapstructUtils.convert(entity, StudyPlanDO.class);
        return studyPlanMapper.insert(dataObj);
    }

    public int update(StudyPlanBO entity) {
        StudyPlanDO dataObj = MapstructUtils.convert(entity, StudyPlanDO.class);
        return studyPlanMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return studyPlanMapper.deleteById(id);
    }

}
