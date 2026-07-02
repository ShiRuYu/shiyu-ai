package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.CourseKnowledgeDO;
import com.shiyu.ai.dal.mapper.education.CourseKnowledgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseKnowledgeRepository {

    @Resource
    private CourseKnowledgeMapper courseKnowledgeMapper;

    public List<CourseKnowledgeDO> selectByCourseId(Long courseId) {
        return courseKnowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq("course_id", courseId));
    }
}
