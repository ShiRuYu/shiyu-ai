package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.CourseKnowledgeBO;
import com.shiyu.ai.dal.education.dataobject.CourseKnowledgeDO;
import com.shiyu.ai.dal.education.mapper.CourseKnowledgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseKnowledgeRepository {

    @Resource
    private CourseKnowledgeMapper courseKnowledgeMapper;

    public List<CourseKnowledgeBO> selectByCourseId(Long courseId) {
        return MapstructUtils.convert(courseKnowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq("course_id", courseId)), CourseKnowledgeBO.class);
    }
}
